package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 系统配置键值，例如管网权限收紧开关 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_config")
public class SysConfig extends BaseEntity {
    private String configKey;
    private String configValue;
    private String description;
}
