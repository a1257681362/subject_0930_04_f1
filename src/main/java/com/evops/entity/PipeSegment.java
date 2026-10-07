package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 管段档案：材质、口径(mm)、埋深(m)、所属道路 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_pipe_segment")
public class PipeSegment extends BaseEntity {
    private String segmentNo;
    private String material;
    private Integer diameterMm;
    private BigDecimal burialDepthM;
    private String roadName;
    /** DRAFT / CONFIRMED */
    private String status;
    private LocalDateTime confirmedAt;
    private String remarks;
}
