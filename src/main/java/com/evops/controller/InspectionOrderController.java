package com.evops.controller;

import com.evops.common.ApiResponse;
import com.evops.entity.InspectionOrder;
import com.evops.service.InspectionOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class InspectionOrderController {

    private final InspectionOrderService orderService;

    public InspectionOrderController(InspectionOrderService orderService) {
        this.orderService = orderService;
    }

    /** 派检 */
    @PostMapping("/dispatch")
    public ApiResponse<InspectionOrder> dispatch(@RequestBody DispatchRequest request) {
        return ApiResponse.ok(orderService.dispatch(
                request.getRouteId(), request.getAssigneeId(), request.getPlannedTime()));
    }

    @PostMapping("/{id}/start")
    public ApiResponse<InspectionOrder> start(@PathVariable Long id) {
        return ApiResponse.ok(orderService.start(id));
    }

    @PostMapping("/{id}/finish")
    public ApiResponse<InspectionOrder> finish(@PathVariable Long id) {
        return ApiResponse.ok(orderService.finish(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<InspectionOrder> get(@PathVariable Long id) {
        return ApiResponse.ok(orderService.get(id));
    }

    @GetMapping
    public ApiResponse<List<InspectionOrder>> list(@RequestParam(required = false) String status,
                                                   @RequestParam(required = false) Long assigneeId) {
        return ApiResponse.ok(orderService.list(status, assigneeId));
    }

    /** 派检请求体 */
    public static class DispatchRequest {
        private Long routeId;
        private Long assigneeId;
        private LocalDateTime plannedTime;

        public Long getRouteId() { return routeId; }
        public void setRouteId(Long routeId) { this.routeId = routeId; }
        public Long getAssigneeId() { return assigneeId; }
        public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
        public LocalDateTime getPlannedTime() { return plannedTime; }
        public void setPlannedTime(LocalDateTime plannedTime) { this.plannedTime = plannedTime; }
    }
}
