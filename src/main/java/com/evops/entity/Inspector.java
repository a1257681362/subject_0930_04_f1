package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_inspector")
public class Inspector extends BaseEntity {
    private String employeeNo;
    private String name;
    private String phone;
    private Boolean enabled;
}
