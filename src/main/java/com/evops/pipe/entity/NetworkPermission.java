package com.evops.pipe.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管网权限模式（单行表）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_network_permission")
public class NetworkPermission extends BaseEntity {
    /** NORMAL / TIGHTENED */
    private String mode;
    /** 模式变更原因 */
    private String reason;
}
