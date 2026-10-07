package com.evops.pipe.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 井室档案。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_pipe_well")
public class PipeWell extends BaseEntity {
    /** 井室编号（全局唯一，建档后不可变更） */
    private String wellNo;
    /** 所属管段（可空） */
    private Long segmentId;
    /** 当前经度 */
    private BigDecimal coordLng;
    /** 当前纬度 */
    private BigDecimal coordLat;
    /** 盖板类型/材质 */
    private String coverType;
    /** 风险等级：LOW / MEDIUM / HIGH */
    private String riskLevel;
    /** 确认状态：UNCONFIRMED / CONFIRMED */
    private String confirmStatus;
}
