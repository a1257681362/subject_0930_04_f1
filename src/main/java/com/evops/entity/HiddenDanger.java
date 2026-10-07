package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 隐患台账：登记 -> 整改 -> 复核闭环 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_hidden_danger")
public class HiddenDanger extends BaseEntity {
    private String hazardNo;
    /** FIELD（现场记录转入）/ MANUAL（人工登记） */
    private String sourceType;
    private Long orderId;
    private Long fieldRecordId;
    private String targetType;
    private Long targetId;
    private String description;
    /** LOW / MEDIUM / HIGH */
    private String severity;
    /** REGISTERED / RECTIFIED / REVIEWED / REJECTED */
    private String status;
    private Long registrarId;
    private LocalDateTime registeredAt;
    private String rectificationNote;
    private Long rectifiedBy;
    private LocalDateTime rectifiedAt;
    private String reviewConclusion;
    private Long reviewBy;
    private LocalDateTime reviewAt;
}
