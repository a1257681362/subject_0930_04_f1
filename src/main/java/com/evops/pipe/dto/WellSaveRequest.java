package com.evops.pipe.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 井室建档/修改请求。
 */
@Data
public class WellSaveRequest {
    @NotBlank(message = "井室编号不能为空")
    private String wellNo;
    /** 所属管段（可空） */
    private Long segmentId;
    @NotNull(message = "经度不能为空")
    private BigDecimal coordLng;
    @NotNull(message = "纬度不能为空")
    private BigDecimal coordLat;
    @NotBlank(message = "盖板类型不能为空")
    private String coverType;
    @NotBlank(message = "风险等级不能为空")
    private String riskLevel;
    /** 修改坐标时填写，写入位置历史 */
    private String changeReason;
}
