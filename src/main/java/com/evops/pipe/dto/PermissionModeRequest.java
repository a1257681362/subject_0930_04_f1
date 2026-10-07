package com.evops.pipe.dto;

import lombok.Data;

/**
 * 管网权限模式变更请求。
 */
@Data
public class PermissionModeRequest {
    /** 变更原因 */
    private String reason;
}
