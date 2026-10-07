package com.evops.pipe.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 隐患登记请求。
 */
@Data
public class HazardRegisterRequest {
    @NotNull(message = "工单不能为空")
    private Long orderId;
    @NotNull(message = "现场记录不能为空")
    private Long recordId;
    @NotBlank(message = "隐患描述不能为空")
    private String hazardDesc;
    /** 隐患等级：GENERAL / MAJOR，默认 GENERAL */
    private String hazardLevel;
}
