package com.evops.pipe.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 现场记录：巡检中发现的问题。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_inspect_record")
public class InspectRecord extends BaseEntity {
    private Long orderId;
    private Long wellId;
    private Long segmentId;
    /** 发现问题描述 */
    private String findingDesc;
    /** 是否发现隐患 */
    private Boolean hasHazard;
    /** 记录人 */
    private String recordedBy;
}
