package com.evops.controller;

import com.evops.common.ApiResponse;
import com.evops.entity.PipeSegment;
import com.evops.service.PipeSegmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pipes")
public class PipeSegmentController {

    private final PipeSegmentService pipeSegmentService;

    public PipeSegmentController(PipeSegmentService pipeSegmentService) {
        this.pipeSegmentService = pipeSegmentService;
    }

    /** 建档：管段编号必须唯一 */
    @PostMapping
    public ApiResponse<PipeSegment> create(@RequestBody PipeSegment segment) {
        return ApiResponse.ok(pipeSegmentService.create(segment));
    }

    @PutMapping("/{id}")
    public ApiResponse<PipeSegment> update(@PathVariable Long id,
                                           @RequestBody PipeSegment segment) {
        segment.setId(id);
        return ApiResponse.ok(pipeSegmentService.update(segment));
    }

    /** 确认建档事实 */
    @PostMapping("/{id}/confirm")
    public ApiResponse<PipeSegment> confirm(@PathVariable Long id) {
        return ApiResponse.ok(pipeSegmentService.confirm(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<PipeSegment> get(@PathVariable Long id) {
        return ApiResponse.ok(pipeSegmentService.get(id));
    }

    @GetMapping
    public ApiResponse<List<PipeSegment>> list(@RequestParam(required = false) String roadName) {
        return ApiResponse.ok(pipeSegmentService.list(roadName));
    }
}
