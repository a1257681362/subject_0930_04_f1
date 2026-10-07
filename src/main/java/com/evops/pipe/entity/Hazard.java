package com.evops.pipe.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 隐患登记。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_hazard")
public class Hazard extends BaseEntity {
    /** 隐患编号（唯一，系统生成） */
    private String hazardNo;
    private Long orderId;
    private Long recordId;
    private Long wellId;
    private Long segmentId;
    /** 隐患描述 */
    private String hazardDesc;
    /** 隐患等级：GENERAL / MAJOR */
    private String hazardLevel;
    /** REGISTERED / REVIEW_PASS / REVIEW_REJECT */
    private String status;
    private String reviewComment;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
}
