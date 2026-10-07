package com.evops.controller;

import com.evops.common.ApiResponse;
import com.evops.entity.HiddenDanger;
import com.evops.service.HiddenDangerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hazards")
public class HiddenDangerController {

    private final HiddenDangerService hazardService;

    public HiddenDangerController(HiddenDangerService hazardService) {
        this.hazardService = hazardService;
    }

    /** 人工登记隐患 */
    @PostMapping("/register")
    public ApiResponse<HiddenDanger> register(@RequestBody RegisterRequest request) {
        return ApiResponse.ok(hazardService.registerManually(
                request.getTargetType(), request.getTargetId(),
                request.getDescription(), request.getSeverity()));
    }

    /** 现场问题记录转隐患登记 */
    @PostMapping("/from-field-record/{fieldRecordId}")
    public ApiResponse<HiddenDanger> registerFromField(@PathVariable Long fieldRecordId,
                                                       @RequestParam(required = false) String severity) {
        return ApiResponse.ok(hazardService.registerFromFieldRecord(fieldRecordId, severity));
    }

    /** 整改完成提交复核 */
    @PostMapping("/{id}/rectify")
    public ApiResponse<HiddenDanger> rectify(@PathVariable Long id,
                                             @RequestBody RectifyRequest request) {
        return ApiResponse.ok(hazardService.rectify(
                id, request.getOperatorId(), request.getRectificationNote()));
    }

    /** 复核：passed=true 闭环，passed=false 驳回重新整改 */
    @PostMapping("/{id}/review")
    public ApiResponse<HiddenDanger> review(@PathVariable Long id,
                                            @RequestBody ReviewRequest request) {
        return ApiResponse.ok(hazardService.review(
                id, request.getReviewerId(),
                Boolean.TRUE.equals(request.getPassed()), request.getConclusion()));
    }

    @GetMapping("/{id}")
    public ApiResponse<HiddenDanger> get(@PathVariable Long id) {
        return ApiResponse.ok(hazardService.get(id));
    }

    @GetMapping
    public ApiResponse<List<HiddenDanger>> list(@RequestParam(required = false) String status,
                                                @RequestParam(required = false) String targetType) {
        return ApiResponse.ok(hazardService.list(status, targetType));
    }

    public static class RegisterRequest {
        private String targetType;
        private Long targetId;
        private String description;
        private String severity;

        public String getTargetType() { return targetType; }
        public void setTargetType(String targetType) { this.targetType = targetType; }
        public Long getTargetId() { return targetId; }
        public void setTargetId(Long targetId) { this.targetId = targetId; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }

    public static class RectifyRequest {
        private Long operatorId;
        private String rectificationNote;

        public Long getOperatorId() { return operatorId; }
        public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
        public String getRectificationNote() { return rectificationNote; }
        public void setRectificationNote(String rectificationNote) { this.rectificationNote = rectificationNote; }
    }

    public static class ReviewRequest {
        private Long reviewerId;
        private Boolean passed;
        private String conclusion;

        public Long getReviewerId() { return reviewerId; }
        public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }
        public Boolean getPassed() { return passed; }
        public void setPassed(Boolean passed) { this.passed = passed; }
        public String getConclusion() { return conclusion; }
        public void setConclusion(String conclusion) { this.conclusion = conclusion; }
    }
}
