package com.evops.pipe.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 巡检路线明细：每站关联一个井室或一段管段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_inspect_order_route")
public class InspectOrderRoute extends BaseEntity {
    private Long orderId;
    private Long wellId;
    private Long segmentId;
    /** 路线顺序，从 1 开始 */
    private Integer seqNo;
}
