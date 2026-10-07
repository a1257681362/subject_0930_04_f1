package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 路线明细点：挂接管段(PIPE)或井室(MANHOLE) */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_route_item")
public class RouteItem extends BaseEntity {
    private Long routeId;
    private String targetType;
    private Long targetId;
    private Integer seqNo;
}
