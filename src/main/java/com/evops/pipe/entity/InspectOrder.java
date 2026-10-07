package com.evops.pipe.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 巡检工单。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_inspect_order")
public class InspectOrder extends BaseEntity {
    /** 工单编号（唯一，系统生成） */
    private String orderNo;
    /** 巡检人员 */
    private String inspectorName;
    /** 路线描述 */
    private String routeDesc;
    /** DISPATCHED / FIELD_RECORDED / CLOSED */
    private String status;
}
