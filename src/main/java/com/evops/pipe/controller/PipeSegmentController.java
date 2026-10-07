package com.evops.pipe.controller;

import com.evops.common.ApiResponse;
import com.evops.pipe.dto.SegmentSaveRequest;
import com.evops.pipe.entity.PipeSegment;
import com.evops.pipe.service.PipeSegmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 管段档案。
 */
@RestController
@RequestMapping("/api/pipe/segments")
@RequiredArgsConstructor
public class PipeSegmentController {

    private final PipeSegmentService segmentService;

    /** 建档 */
    @PostMapping
    public ApiResponse<PipeSegment> create(@Valid @RequestBody SegmentSaveRequest req) {
        return ApiResponse.ok(segmentService.create(req));
    }

    /** 修改（编号不可变更） */
    @PutMapping("/{id}")
    public ApiResponse<PipeSegment> update(@PathVariable Long id, @Valid @RequestBody SegmentSaveRequest req) {
        return ApiResponse.ok(segmentService.update(id, req));
    }

    @GetMapping
    public ApiResponse<List<PipeSegment>> list() {
        return ApiResponse.ok(segmentService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<PipeSegment> get(@PathVariable Long id) {
        return ApiResponse.ok(segmentService.mustGet(id));
    }
}
