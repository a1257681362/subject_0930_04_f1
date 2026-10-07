package com.evops.pipe.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 现场记录请求。
 */
@Data
public class RecordRequest {
    /** 记录对象：井室或管段至少填一个，且必须在工单路线内 */
    private Long wellId;
    private Long segmentId;
    @NotBlank(message = "发现问题描述不能为空")
    private String findingDesc;
    /** 是否发现隐患 */
    private Boolean hasHazard;
    @NotBlank(message = "记录人不能为空")
    private String recordedBy;
}
