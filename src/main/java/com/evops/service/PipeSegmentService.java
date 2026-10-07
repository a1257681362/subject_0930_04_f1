package com.evops.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evops.common.BizConstants;
import com.evops.common.BusinessException;
import com.evops.entity.PipeSegment;
import com.evops.mapper.PipeSegmentMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管段档案。
 * 管网权限收紧时，管段档案整体停止推进：禁止新建、变更与确认。
 */
@Service
public class PipeSegmentService {

    private final PipeSegmentMapper pipeSegmentMapper;
    private final SysConfigService sysConfigService;

    public PipeSegmentService(PipeSegmentMapper pipeSegmentMapper,
                              SysConfigService sysConfigService) {
        this.pipeSegmentMapper = pipeSegmentMapper;
        this.sysConfigService = sysConfigService;
    }

    public PipeSegment create(PipeSegment segment) {
        ensureNotRestricted("管网权限已收紧，管段档案停止推进，禁止新建管段");
        validate(segment);
        if (segment.getSegmentNo() == null || segment.getSegmentNo().trim().isEmpty()) {
            throw new BusinessException("管段编号不能为空");
        }
        ensureNoUnique(segment.getSegmentNo());
        segment.setId(null);
        segment.setStatus(BizConstants.ARCHIVE_DRAFT);
        segment.setConfirmedAt(null);
        pipeSegmentMapper.insert(segment);
        return segment;
    }

    public PipeSegment update(PipeSegment segment) {
        ensureNotRestricted("管网权限已收紧，管段档案停止推进，禁止变更管段");
        if (segment.getId() == null) {
            throw new BusinessException("管段ID不能为空");
        }
        PipeSegment exists = pipeSegmentMapper.selectById(segment.getId());
        if (exists == null) {
            throw new BusinessException("管段不存在: " + segment.getId());
        }
        validate(segment);
        // 编号唯一且不可改：以库内既有编号为准，防止改成重复编号
        segment.setSegmentNo(exists.getSegmentNo());
        segment.setStatus(exists.getStatus());
        segment.setConfirmedAt(exists.getConfirmedAt());
        pipeSegmentMapper.updateById(segment);
        return pipeSegmentMapper.selectById(segment.getId());
    }

    /** 确认建档事实。收紧状态下禁止确认（档案停止推进）。 */
    public PipeSegment confirm(Long id) {
        ensureNotRestricted("管网权限已收紧，管段档案停止推进，禁止确认管段");
        PipeSegment exists = mustGet(id);
        if (BizConstants.ARCHIVE_CONFIRMED.equals(exists.getStatus())) {
            return exists;
        }
        exists.setStatus(BizConstants.ARCHIVE_CONFIRMED);
        exists.setConfirmedAt(LocalDateTime.now());
        pipeSegmentMapper.updateById(exists);
        return exists;
    }

    public PipeSegment get(Long id) {
        return mustGet(id);
    }

    public PipeSegment getByNo(String segmentNo) {
        PipeSegment segment = pipeSegmentMapper.selectOne(
                new QueryWrapper<PipeSegment>().eq("segment_no", segmentNo));
        if (segment == null) {
            throw new BusinessException("管段不存在: " + segmentNo);
        }
        return segment;
    }

    public List<PipeSegment> list(String roadName) {
        QueryWrapper<PipeSegment> wrapper = new QueryWrapper<>();
        if (roadName != null && !roadName.trim().isEmpty()) {
            wrapper.eq("road_name", roadName);
        }
        wrapper.orderByDesc("id");
        return pipeSegmentMapper.selectList(wrapper);
    }

    private void ensureNotRestricted(String message) {
        if (sysConfigService.isPermissionRestricted()) {
            throw new BusinessException(message);
        }
    }

    private void ensureNoUnique(String segmentNo) {
        Long count = pipeSegmentMapper.selectCount(
                new QueryWrapper<PipeSegment>().eq("segment_no", segmentNo));
        if (count != null && count > 0) {
            throw new BusinessException("管段编号已存在: " + segmentNo);
        }
    }

    private PipeSegment mustGet(Long id) {
        PipeSegment segment = pipeSegmentMapper.selectById(id);
        if (segment == null) {
            throw new BusinessException("管段不存在: " + id);
        }
        return segment;
    }

    private void validate(PipeSegment segment) {
        if (segment.getDiameterMm() != null && segment.getDiameterMm() <= 0) {
            throw new BusinessException("口径必须为正整数(mm)");
        }
        if (segment.getBurialDepthM() != null
                && segment.getBurialDepthM().signum() < 0) {
            throw new BusinessException("埋深不能为负数(m)");
        }
    }
}
