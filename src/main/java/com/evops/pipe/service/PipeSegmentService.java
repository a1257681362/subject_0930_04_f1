package com.evops.pipe.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evops.pipe.PipeConstants;
import com.evops.pipe.dto.SegmentSaveRequest;
import com.evops.pipe.entity.PipeSegment;
import com.evops.pipe.mapper.PipeSegmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管段档案：建档、修改、查询。管段编号全局唯一，建档后不可变更。
 */
@Service
@RequiredArgsConstructor
public class PipeSegmentService {

    private final PipeSegmentMapper segmentMapper;
    private final NetworkPermissionService permissionService;

    /**
     * 建档。
     */
    public PipeSegment create(SegmentSaveRequest req) {
        permissionService.requireSegmentWritable();
        Long count = segmentMapper.selectCount(
                new LambdaQueryWrapper<PipeSegment>().eq(PipeSegment::getSegmentNo, req.getSegmentNo()));
        if (count != null && count > 0) {
            throw new IllegalArgumentException("管段编号已存在: " + req.getSegmentNo());
        }
        PipeSegment segment = new PipeSegment();
        segment.setSegmentNo(req.getSegmentNo());
        segment.setMaterial(req.getMaterial());
        segment.setDiameterMm(req.getDiameterMm());
        segment.setBuryDepthM(req.getBuryDepthM());
        segment.setRoadName(req.getRoadName());
        segment.setStatus(PipeConstants.SEGMENT_FILED);
        segmentMapper.insert(segment);
        return segment;
    }

    /**
     * 修改档案（编号不可变更）。
     */
    public PipeSegment update(Long id, SegmentSaveRequest req) {
        permissionService.requireSegmentWritable();
        PipeSegment segment = mustGet(id);
        if (req.getSegmentNo() != null && !req.getSegmentNo().equals(segment.getSegmentNo())) {
            throw new IllegalArgumentException("管段编号建档后不可变更");
        }
        segment.setMaterial(req.getMaterial());
        segment.setDiameterMm(req.getDiameterMm());
        segment.setBuryDepthM(req.getBuryDepthM());
        segment.setRoadName(req.getRoadName());
        segmentMapper.updateById(segment);
        return segment;
    }

    public PipeSegment mustGet(Long id) {
        PipeSegment segment = segmentMapper.selectById(id);
        if (segment == null) {
            throw new IllegalArgumentException("管段不存在: " + id);
        }
        return segment;
    }

    public List<PipeSegment> list() {
        return segmentMapper.selectList(new LambdaQueryWrapper<PipeSegment>().orderByAsc(PipeSegment::getId));
    }
}
