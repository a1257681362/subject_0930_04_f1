package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 巡检工单：关联人员与路线 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_inspection_order")
public class InspectionOrder extends BaseEntity {
    private String orderNo;
    private Long routeId;
    private Long assigneeId;
    private LocalDateTime plannedTime;
    /** ASSIGNED / IN_PROGRESS / COMPLETED */
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String remarks;
}
