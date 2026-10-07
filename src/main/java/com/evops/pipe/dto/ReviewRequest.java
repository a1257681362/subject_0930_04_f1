package com.evops.pipe.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 隐患复核请求。
 */
@Data
public class ReviewRequest {
    /** true=复核通过，false=复核退回 */
    @NotNull(message = "复核结论不能为空")
    private Boolean pass;
    private String reviewComment;
    @NotBlank(message = "复核人不能为空")
    private String reviewedBy;
}
