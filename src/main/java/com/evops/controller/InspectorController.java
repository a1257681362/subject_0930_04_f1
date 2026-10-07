package com.evops.controller;

import com.evops.common.ApiResponse;
import com.evops.entity.Inspector;
import com.evops.service.InspectorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inspectors")
public class InspectorController {

    private final InspectorService inspectorService;

    public InspectorController(InspectorService inspectorService) {
        this.inspectorService = inspectorService;
    }

    @PostMapping
    public ApiResponse<Inspector> create(@RequestBody Inspector inspector) {
        return ApiResponse.ok(inspectorService.create(inspector));
    }

    @GetMapping
    public ApiResponse<List<Inspector>> list() {
        return ApiResponse.ok(inspectorService.list());
    }
}
