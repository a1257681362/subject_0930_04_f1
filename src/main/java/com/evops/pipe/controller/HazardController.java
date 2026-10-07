package com.evops.pipe.controller;

import com.evops.common.ApiResponse;
import com.evops.pipe.dto.HazardRegisterRequest;
import com.evops.pipe.dto.ReviewRequest;
import com.evops.pipe.entity.Hazard;
import com.evops.pipe.service.HazardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 隐患登记与复核。
 */
@RestController
@RequestMapping("/api/pipe/hazards")
@RequiredArgsConstructor
public class HazardController {

    private final HazardService hazardService;

    /** 隐患登记 */
    @PostMapping
    public ApiResponse<Hazard> register(@Valid @RequestBody HazardRegisterRequest req) {
        return ApiResponse.ok(hazardService.register(req));
    }

    /** 隐患复核（通过/退回） */
    @PostMapping("/{id}/review")
    public ApiResponse<Hazard> review(@PathVariable Long id, @Valid @RequestBody ReviewRequest req) {
        return ApiResponse.ok(hazardService.review(id, req));
    }

    @GetMapping
    public ApiResponse<List<Hazard>> list(@RequestParam(required = false) Long orderId) {
        return ApiResponse.ok(hazardService.listByOrder(orderId));
    }

    @GetMapping("/{id}")
    public ApiResponse<Hazard> get(@PathVariable Long id) {
        return ApiResponse.ok(hazardService.mustGet(id));
    }
}
