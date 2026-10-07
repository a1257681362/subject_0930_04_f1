package com.evops.pipe.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evops.pipe.PipeConstants;
import com.evops.pipe.dto.WellSaveRequest;
import com.evops.pipe.entity.PipeWell;
import com.evops.pipe.entity.PipeWellLocationHist;
import com.evops.pipe.mapper.PipeWellLocationHistMapper;
import com.evops.pipe.mapper.PipeWellMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 井室档案：建档、修改、确认、位置历史。
 * 规则：
 * 1. 井室编号全局唯一，建档后不可变更；
 * 2. 坐标每次变化都追加位置历史，历史位置不能被删除；
 * 3. 井室确认后，已确认事实不可篡改；
 * 4. 权限收紧时，井室档案仅保留已确认事实，禁止变更。
 */
@Service
@RequiredArgsConstructor
public class PipeWellService {

    private final PipeWellMapper wellMapper;
    private final PipeWellLocationHistMapper locationHistMapper;
    private final PipeSegmentService segmentService;
    private final NetworkPermissionService permissionService;

    /**
     * 建档，并写入初始位置历史。
     */
    @Transactional(rollbackFor = Exception.class)
    public PipeWell create(WellSaveRequest req) {
        permissionService.requireWellWritable();
        Long count = wellMapper.selectCount(
                new LambdaQueryWrapper<PipeWell>().eq(PipeWell::getWellNo, req.getWellNo()));
        if (count != null && count > 0) {
            throw new IllegalArgumentException("井室编号已存在: " + req.getWellNo());
        }
        if (req.getSegmentId() != null) {
            segmentService.mustGet(req.getSegmentId());
        }
        PipeWell well = new PipeWell();
        well.setWellNo(req.getWellNo());
        well.setSegmentId(req.getSegmentId());
        well.setCoordLng(req.getCoordLng());
        well.setCoordLat(req.getCoordLat());
        well.setCoverType(req.getCoverType());
        well.setRiskLevel(req.getRiskLevel());
        well.setConfirmStatus(PipeConstants.CONFIRM_UNCONFIRMED);
        wellMapper.insert(well);
        appendLocation(well.getId(), req.getCoordLng(), req.getCoordLat(), "建档初始位置");
        return well;
    }

    /**
     * 修改档案。已确认井室保留确认事实，禁止修改；坐标变化追加位置历史。
     */
    @Transactional(rollbackFor = Exception.class)
    public PipeWell update(Long id, WellSaveRequest req) {
        permissionService.requireWellWritable();
        PipeWell well = mustGet(id);
        if (PipeConstants.CONFIRM_CONFIRMED.equals(well.getConfirmStatus())) {
            throw new IllegalStateException("井室事实已确认，保留确认事实，禁止修改: " + well.getWellNo());
        }
        if (req.getWellNo() != null && !req.getWellNo().equals(well.getWellNo())) {
            throw new IllegalArgumentException("井室编号建档后不可变更");
        }
        if (req.getSegmentId() != null) {
            segmentService.mustGet(req.getSegmentId());
        }
        boolean moved = req.getCoordLng().compareTo(well.getCoordLng()) != 0
                || req.getCoordLat().compareTo(well.getCoordLat()) != 0;
        well.setSegmentId(req.getSegmentId());
        well.setCoordLng(req.getCoordLng());
        well.setCoordLat(req.getCoordLat());
        well.setCoverType(req.getCoverType());
        well.setRiskLevel(req.getRiskLevel());
        wellMapper.updateById(well);
        if (moved) {
            String reason = StringUtils.hasText(req.getChangeReason()) ? req.getChangeReason() : "井室位置修正";
            appendLocation(id, req.getCoordLng(), req.getCoordLat(), reason);
        }
        return well;
    }

    /**
     * 确认井室事实。确认后事实保留、不可篡改。
     */
    public PipeWell confirm(Long id) {
        permissionService.requireWellWritable();
        PipeWell well = mustGet(id);
        well.setConfirmStatus(PipeConstants.CONFIRM_CONFIRMED);
        wellMapper.updateById(well);
        return well;
    }

    /**
     * 位置历史（只增不删）。
     */
    public List<PipeWellLocationHist> locationHistory(Long wellId) {
        mustGet(wellId);
        return locationHistMapper.selectList(new LambdaQueryWrapper<PipeWellLocationHist>()
                .eq(PipeWellLocationHist::getWellId, wellId)
                .orderByAsc(PipeWellLocationHist::getId));
    }

    public PipeWell mustGet(Long id) {
        PipeWell well = wellMapper.selectById(id);
        if (well == null) {
            throw new IllegalArgumentException("井室不存在: " + id);
        }
        return well;
    }

    public List<PipeWell> list() {
        return wellMapper.selectList(new LambdaQueryWrapper<PipeWell>().orderByAsc(PipeWell::getId));
    }

    private void appendLocation(Long wellId, BigDecimal lng, BigDecimal lat, String reason) {
        PipeWellLocationHist hist = new PipeWellLocationHist();
        hist.setWellId(wellId);
        hist.setCoordLng(lng);
        hist.setCoordLat(lat);
        hist.setChangeReason(reason);
        locationHistMapper.insert(hist);
    }
}
