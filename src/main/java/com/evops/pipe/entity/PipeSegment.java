package com.evops.pipe.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 管段档案。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_pipe_segment")
public class PipeSegment extends BaseEntity {
    /** 管段编号（全局唯一，建档后不可变更） */
    private String segmentNo;
    /** 材质，如 球墨铸铁/PE/混凝土/钢管 */
    private String material;
    /** 口径（毫米） */
    private Integer diameterMm;
    /** 埋深（米） */
    private BigDecimal buryDepthM;
    /** 所属道路 */
    private String roadName;
    /** 档案状态 */
    private String status;
}
