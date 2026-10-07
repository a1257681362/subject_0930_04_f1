package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 现场巡检记录 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_field_record")
public class FieldRecord extends BaseEntity {
    private Long orderId;
    private String targetType;
    private Long targetId;
    private Long inspectorId;
    private LocalDateTime recordTime;
    private String conditionStatus;
    private String content;
    private Boolean hasIssue;
}
