package com.evops.pipe.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 管段建档/修改请求。
 */
@Data
public class SegmentSaveRequest {
    @NotBlank(message = "管段编号不能为空")
    private String segmentNo;
    @NotBlank(message = "材质不能为空")
    private String material;
    @NotNull(message = "口径不能为空")
    private Integer diameterMm;
    @NotNull(message = "埋深不能为空")
    private BigDecimal buryDepthM;
    @NotBlank(message = "所属道路不能为空")
    private String roadName;
}
