package com.evops.pipe.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 派检请求：关联巡检人员与路线。
 */
@Data
public class DispatchRequest {
    @NotBlank(message = "巡检人员不能为空")
    private String inspectorName;
    /** 路线描述，如“人民路沿线井室” */
    private String routeDesc;
    @NotEmpty(message = "派检路线不能为空")
    @Valid
    private List<RouteItem> route;

    /**
     * 路线一站：关联一个井室或一段管段。
     */
    @Data
    public static class RouteItem {
        private Long wellId;
        private Long segmentId;
    }
}
