package com.evops.controller;

import com.evops.common.ApiResponse;
import com.evops.entity.FieldRecord;
import com.evops.service.FieldRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/field-records")
public class FieldRecordController {

    private final FieldRecordService fieldRecordService;

    public FieldRecordController(FieldRecordService fieldRecordService) {
        this.fieldRecordService = fieldRecordService;
    }

    /** 现场记录：工单开始后，对路线上的管段/井室记录情况 */
    @PostMapping
    public ApiResponse<FieldRecord> record(@RequestBody RecordRequest request) {
        return ApiResponse.ok(fieldRecordService.record(
                request.getOrderId(),
                request.getTargetType(),
                request.getTargetId(),
                request.getConditionStatus(),
                request.getContent(),
                Boolean.TRUE.equals(request.getHasIssue())));
    }

    @GetMapping("/by-order/{orderId}")
    public ApiResponse<List<FieldRecord>> listByOrder(@PathVariable Long orderId) {
        return ApiResponse.ok(fieldRecordService.listByOrder(orderId));
    }

    public static class RecordRequest {
        private Long orderId;
        private String targetType;
        private Long targetId;
        private String conditionStatus;
        private String content;
        private Boolean hasIssue;

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getTargetType() { return targetType; }
        public void setTargetType(String targetType) { this.targetType = targetType; }
        public Long getTargetId() { return targetId; }
        public void setTargetId(Long targetId) { this.targetId = targetId; }
        public String getConditionStatus() { return conditionStatus; }
        public void setConditionStatus(String conditionStatus) { this.conditionStatus = conditionStatus; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Boolean getHasIssue() { return hasIssue; }
        public void setHasIssue(Boolean hasIssue) { this.hasIssue = hasIssue; }
    }
}
