package com.evops.pipe.controller;

import com.evops.common.ApiResponse;
import com.evops.pipe.dto.PermissionModeRequest;
import com.evops.pipe.entity.NetworkPermission;
import com.evops.pipe.service.NetworkPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管网权限模式：收紧 / 恢复。
 */
@RestController
@RequestMapping("/api/pipe/permission")
@RequiredArgsConstructor
public class NetworkPermissionController {

    private final NetworkPermissionService permissionService;

    @GetMapping
    public ApiResponse<NetworkPermission> current() {
        return ApiResponse.ok(permissionService.current());
    }

    /** 收紧权限：管段档案停止推进，井室档案仅保留已确认事实 */
    @PostMapping("/tighten")
    public ApiResponse<NetworkPermission> tighten(@RequestBody(required = false) PermissionModeRequest req) {
        return ApiResponse.ok(permissionService.tighten(req == null ? null : req.getReason()));
    }

    /** 恢复正常 */
    @PostMapping("/restore")
    public ApiResponse<NetworkPermission> restore(@RequestBody(required = false) PermissionModeRequest req) {
        return ApiResponse.ok(permissionService.restore(req == null ? null : req.getReason()));
    }
}
