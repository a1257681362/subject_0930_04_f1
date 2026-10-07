package com.evops.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evops.common.BizConstants;
import com.evops.common.BusinessException;
import com.evops.entity.Manhole;
import com.evops.entity.ManholeLocationHistory;
import com.evops.mapper.ManholeLocationHistoryMapper;
import com.evops.mapper.ManholeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 井室档案。
 * 权限收紧时井室档案不做删除、已确认事实冻结不可改写；
 * 坐标每次变化都追加历史位置，历史只追加、不提供修改与删除。
 */
@Service
public class ManholeService {

    private final ManholeMapper manholeMapper;
    private final ManholeLocationHistoryMapper historyMapper;
    private final SysConfigService sysConfigService;

    public ManholeService(ManholeMapper manholeMapper,
                          ManholeLocationHistoryMapper historyMapper,
                          SysConfigService sysConfigService) {
        this.manholeMapper = manholeMapper;
        this.historyMapper = historyMapper;
        this.sysConfigService = sysConfigService;
    }

    @Transactional
    public Manhole create(Manhole manhole) {
        if (manhole.getManholeNo() == null || manhole.getManholeNo().trim().isEmpty()) {
            throw new BusinessException("井室编号不能为空");
        }
        Long count = manholeMapper.selectCount(
                new QueryWrapper<Manhole>().eq("manhole_no", manhole.getManholeNo()));
        if (count != null && count > 0) {
            throw new BusinessException("井室编号已存在: " + manhole.getManholeNo());
        }
        validateRisk(manhole.getRiskLevel());
        manhole.setId(null);
        manhole.setStatus(BizConstants.ARCHIVE_DRAFT);
        manhole.setConfirmedAt(null);
        manholeMapper.insert(manhole);
        // 建档坐标作为首条历史位置留存
        if (manhole.getLng() != null && manhole.getLat() != null) {
            appendHistory(manhole.getId(), manhole.getLng(), manhole.getLat(), "建档定位");
        }
        return manhole;
    }

    /**
     * 变更井室信息。坐标发生变化时追加历史位置。
     * 权限收紧期间，已确认井室的事实冻结，禁止任何变更。
     */
    @Transactional
    public Manhole update(Manhole manhole, String locationChangeRemark) {
        if (manhole.getId() == null) {
            throw new BusinessException("井室ID不能为空");
        }
        Manhole exists = mustGet(manhole.getId());
        boolean restricted = sysConfigService.isPermissionRestricted();
        if (restricted && BizConstants.ARCHIVE_CONFIRMED.equals(exists.getStatus())) {
            throw new BusinessException("管网权限已收紧，井室已确认事实予以保留，禁止变更");
        }
        validateRisk(manhole.getRiskLevel());
        // 编号唯一且不可改
        manhole.setManholeNo(exists.getManholeNo());
        manhole.setStatus(exists.getStatus());
        manhole.setConfirmedAt(exists.getConfirmedAt());

        boolean coordinateChanged = manhole.getLng() != null && manhole.getLat() != null
                && (!manhole.getLng().equals(exists.getLng())
                    || !manhole.getLat().equals(exists.getLat()));
        manholeMapper.updateById(manhole);
        if (coordinateChanged) {
            appendHistory(manhole.getId(), manhole.getLng(), manhole.getLat(),
                    locationChangeRemark == null ? "坐标修正" : locationChangeRemark);
        }
        return manholeMapper.selectById(manhole.getId());
    }

    /** 确认井室档案事实，确认后坐标/盖板/风险等级即为冻结事实 */
    @Transactional
    public Manhole confirm(Long id) {
        Manhole exists = mustGet(id);
        if (BizConstants.ARCHIVE_CONFIRMED.equals(exists.getStatus())) {
            return exists;
        }
        if (sysConfigService.isPermissionRestricted()) {
            throw new BusinessException("管网权限已收紧，井室仅保留已确认事实，不能新增确认");
        }
        if (exists.getLng() == null || exists.getLat() == null) {
            throw new BusinessException("井室坐标未登记，不能确认");
        }
        exists.setStatus(BizConstants.ARCHIVE_CONFIRMED);
        exists.setConfirmedAt(LocalDateTime.now());
        manholeMapper.updateById(exists);
        return exists;
    }

    public Manhole get(Long id) {
        return mustGet(id);
    }

    public List<Manhole> list(String riskLevel) {
        QueryWrapper<Manhole> wrapper = new QueryWrapper<>();
        if (riskLevel != null && !riskLevel.trim().isEmpty()) {
            wrapper.eq("risk_level", riskLevel);
        }
        wrapper.orderByDesc("id");
        return manholeMapper.selectList(wrapper);
    }

    /** 井室历史位置，按时间正序，只追加不可删除 */
    public List<ManholeLocationHistory> listLocationHistory(Long manholeId) {
        mustGet(manholeId);
        return historyMapper.selectList(new QueryWrapper<ManholeLocationHistory>()
                .eq("manhole_id", manholeId)
                .orderByAsc("recorded_at").orderByAsc("id"));
    }

    private void appendHistory(Long manholeId, BigDecimal lng, BigDecimal lat, String remark) {
        ManholeLocationHistory history = new ManholeLocationHistory();
        history.setManholeId(manholeId);
        history.setLng(lng);
        history.setLat(lat);
        history.setChangeRemark(remark);
        history.setRecordedAt(LocalDateTime.now());
        historyMapper.insert(history);
    }

    private Manhole mustGet(Long id) {
        Manhole manhole = manholeMapper.selectById(id);
        if (manhole == null) {
            throw new BusinessException("井室不存在: " + id);
        }
        return manhole;
    }

    private void validateRisk(String riskLevel) {
        if (riskLevel != null && !riskLevel.trim().isEmpty()
                && !"LOW".equals(riskLevel) && !"MEDIUM".equals(riskLevel) && !"HIGH".equals(riskLevel)) {
            throw new BusinessException("风险等级只能为 LOW / MEDIUM / HIGH");
        }
    }
}
