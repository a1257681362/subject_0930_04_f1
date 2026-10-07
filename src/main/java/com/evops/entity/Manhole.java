package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 井室档案：坐标(经纬度)、盖板、风险等级 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_manhole")
public class Manhole extends BaseEntity {
    private String manholeNo;
    private BigDecimal lng;
    private BigDecimal lat;
    private String coverType;
    /** LOW / MEDIUM / HIGH */
    private String riskLevel;
    /** DRAFT / CONFIRMED */
    private String status;
    private LocalDateTime confirmedAt;
    private String remarks;
}
