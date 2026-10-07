package com.evops.controller;

import com.evops.common.ApiResponse;
import com.evops.service.SysConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/**
 * 管网权限收紧开关。
 * 收紧后：管段档案停止推进（禁止新建/变更/确认）；
 * 井室档案保留已确认事实（已确认井室冻结，历史位置不可删除）。
 */
@RestController
@RequestMapping("/api/system/permission")
public class SysConfigController {

    private final SysConfigService sysConfigService;

    public SysConfigController(SysConfigService sysConfigService) {
        this.sysConfigService = sysConfigService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> status() {
        return ApiResponse.ok(Collections.singletonMap(
                "restricted", sysConfigService.isPermissionRestricted()));
    }

    @PutMapping
    public ApiResponse<Map<String, Object>> setRestricted(@RequestParam boolean restricted) {
        sysConfigService.setPermissionRestricted(restricted);
        return ApiResponse.ok(Collections.singletonMap("restricted", restricted));
    }
}
