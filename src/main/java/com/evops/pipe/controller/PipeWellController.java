package com.evops.pipe.controller;

import com.evops.common.ApiResponse;
import com.evops.pipe.dto.WellSaveRequest;
import com.evops.pipe.entity.PipeWell;
import com.evops.pipe.entity.PipeWellLocationHist;
import com.evops.pipe.service.PipeWellService;
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
 * 井室档案。
 */
@RestController
@RequestMapping("/api/pipe/wells")
@RequiredArgsConstructor
public class PipeWellController {

    private final PipeWellService wellService;

    /** 建档（同时写入初始位置历史） */
    @PostMapping
    public ApiResponse<PipeWell> create(@Valid @RequestBody WellSaveRequest req) {
        return ApiResponse.ok(wellService.create(req));
    }

    /** 修改（已确认井室禁止修改；坐标变化追加位置历史） */
    @PutMapping("/{id}")
    public ApiResponse<PipeWell> update(@PathVariable Long id, @Valid @RequestBody WellSaveRequest req) {
        return ApiResponse.ok(wellService.update(id, req));
    }

    /** 确认井室事实 */
    @PostMapping("/{id}/confirm")
    public ApiResponse<PipeWell> confirm(@PathVariable Long id) {
        return ApiResponse.ok(wellService.confirm(id));
    }

    /** 位置历史（只增不删） */
    @GetMapping("/{id}/locations")
    public ApiResponse<List<PipeWellLocationHist>> locations(@PathVariable Long id) {
        return ApiResponse.ok(wellService.locationHistory(id));
    }

    @GetMapping
    public ApiResponse<List<PipeWell>> list() {
        return ApiResponse.ok(wellService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<PipeWell> get(@PathVariable Long id) {
        return ApiResponse.ok(wellService.mustGet(id));
    }
}
