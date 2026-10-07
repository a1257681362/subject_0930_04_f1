package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 井室历史位置，只追加，不提供修改与删除 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_manhole_location_history")
public class ManholeLocationHistory extends BaseEntity {
    private Long manholeId;
    private BigDecimal lng;
    private BigDecimal lat;
    private String changeRemark;
    private LocalDateTime recordedAt;
}
