package com.evops.pipe.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 井室位置历史。只增不删：系统不提供任何更新/删除入口。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_pipe_well_location_hist")
public class PipeWellLocationHist extends BaseEntity {
    private Long wellId;
    private BigDecimal coordLng;
    private BigDecimal coordLat;
    /** 变更原因，如“建档初始位置”“测绘修正” */
    private String changeReason;
}
