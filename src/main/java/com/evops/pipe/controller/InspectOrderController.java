package com.evops.pipe.controller;

import com.evops.common.ApiResponse;
import com.evops.pipe.dto.DispatchRequest;
import com.evops.pipe.dto.RecordRequest;
import com.evops.pipe.entity.InspectOrder;
import com.evops.pipe.entity.InspectRecord;
import com.evops.pipe.service.InspectOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 巡检工单：派检、现场记录、复核闭环。
 */
@RestController
@RequestMapping("/api/pipe/inspect-orders")
@RequiredArgsConstructor
public class InspectOrderController {

    private final InspectOrderService orderService;

    /** 派检 */
    @PostMapping
    public ApiResponse<InspectOrder> dispatch(@Valid @RequestBody DispatchRequest req) {
        return ApiResponse.ok(orderService.dispatch(req));
    }

    /** 现场记录 */
    @PostMapping("/{id}/records")
    public ApiResponse<InspectRecord> addRecord(@PathVariable Long id, @Valid @RequestBody RecordRequest req) {
        return ApiResponse.ok(orderService.addRecord(id, req));
    }

    /** 复核闭环（无待复核隐患时） */
    @PostMapping("/{id}/close")
    public ApiResponse<InspectOrder> close(@PathVariable Long id) {
        return ApiResponse.ok(orderService.close(id));
    }

    @GetMapping
    public ApiResponse<List<InspectOrder>> list() {
        return ApiResponse.ok(orderService.list());
    }

    /** 工单详情：工单 + 路线 + 现场记录 + 隐患 */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(orderService.detail(id));
    }
}
