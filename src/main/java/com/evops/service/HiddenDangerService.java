package com.evops.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evops.common.BizConstants;
import com.evops.common.BusinessException;
import com.evops.entity.FieldRecord;
import com.evops.entity.HiddenDanger;
import com.evops.mapper.FieldRecordMapper;
import com.evops.mapper.HiddenDangerMapper;
import com.evops.service.support.NoGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 隐患台账闭环：
 * 登记(REGISTERED) -> 整改(RECTIFIED) -> 复核通过(REVIEWED 闭环)
 *                              复核驳回(REJECTED) -> 重新整改 -> 再复核
 */
@Service
public class HiddenDangerService {

    private final HiddenDangerMapper hazardMapper;
    private final FieldRecordMapper fieldRecordMapper;
    private final NoGenerator noGenerator;

    public HiddenDangerService(HiddenDangerMapper hazardMapper,
                               FieldRecordMapper fieldRecordMapper,
                               NoGenerator noGenerator) {
        this.hazardMapper = hazardMapper;
        this.fieldRecordMapper = fieldRecordMapper;
        this.noGenerator = noGenerator;
    }

    /** 人工登记隐患 */
    @Transactional
    public HiddenDanger registerManually(String targetType, Long targetId,
                                         String description, String severity) {
        validateTarget(targetType, targetId);
        HiddenDanger danger = buildBase(targetType, targetId, description, severity);
        danger.setSourceType(BizConstants.SOURCE_MANUAL);
        hazardMapper.insert(danger);
        return danger;
    }

    /** 由现场问题记录转隐患登记 */
    @Transactional
    public HiddenDanger registerFromFieldRecord(Long fieldRecordId, String severity) {
        FieldRecord record = fieldRecordMapper.selectById(fieldRecordId);
        if (record == null) {
            throw new BusinessException("现场记录不存在: " + fieldRecordId);
        }
        if (!Boolean.TRUE.equals(record.getHasIssue())) {
            throw new BusinessException("该现场记录未标记问题，不能转隐患登记");
        }
        HiddenDanger danger = buildBase(record.getTargetType(), record.getTargetId(),
                record.getContent(), severity);
        danger.setSourceType(BizConstants.SOURCE_FIELD);
        danger.setOrderId(record.getOrderId());
        danger.setFieldRecordId(fieldRecordId);
        hazardMapper.insert(danger);
        return danger;
    }

    /** 整改完成，提交复核 */
    @Transactional
    public HiddenDanger rectify(Long hazardId, Long operatorId, String rectificationNote) {
        HiddenDanger danger = mustGet(hazardId);
        if (BizConstants.HAZARD_REVIEWED.equals(danger.getStatus())) {
            throw new BusinessException("隐患已复核闭环，不能重复整改");
        }
        if (rectificationNote == null || rectificationNote.trim().isEmpty()) {
            throw new BusinessException("整改说明不能为空");
        }
        danger.setStatus(BizConstants.HAZARD_RECTIFIED);
        danger.setRectificationNote(rectificationNote);
        danger.setRectifiedBy(operatorId);
        danger.setRectifiedAt(LocalDateTime.now());
        hazardMapper.updateById(danger);
        return danger;
    }

    /** 复核：通过即闭环，驳回则退回重新整改 */
    @Transactional
    public HiddenDanger review(Long hazardId, Long reviewerId, boolean passed, String conclusion) {
        HiddenDanger danger = mustGet(hazardId);
        if (!BizConstants.HAZARD_RECTIFIED.equals(danger.getStatus())) {
            throw new BusinessException("仅已整改待复核的隐患可以复核");
        }
        if (conclusion == null || conclusion.trim().isEmpty()) {
            throw new BusinessException("复核结论不能为空");
        }
        danger.setReviewConclusion(conclusion);
        danger.setReviewBy(reviewerId);
        danger.setReviewAt(LocalDateTime.now());
        danger.setStatus(passed ? BizConstants.HAZARD_REVIEWED : BizConstants.HAZARD_REJECTED);
        hazardMapper.updateById(danger);
        return danger;
    }

    public HiddenDanger get(Long hazardId) {
        return mustGet(hazardId);
    }

    public List<HiddenDanger> list(String status, String targetType) {
        QueryWrapper<HiddenDanger> wrapper = new QueryWrapper<>();
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq("status", status);
        }
        if (targetType != null && !targetType.trim().isEmpty()) {
            wrapper.eq("target_type", targetType);
        }
        wrapper.orderByDesc("id");
        return hazardMapper.selectList(wrapper);
    }

    private HiddenDanger buildBase(String targetType, Long targetId,
                                   String description, String severity) {
        validateTarget(targetType, targetId);
        if (description == null || description.trim().isEmpty()) {
            throw new BusinessException("隐患描述不能为空");
        }
        HiddenDanger danger = new HiddenDanger();
        danger.setHazardNo(noGenerator.next("YH"));
        danger.setTargetType(targetType);
        danger.setTargetId(targetId);
        danger.setDescription(description);
        danger.setSeverity(normalizeSeverity(severity));
        danger.setStatus(BizConstants.HAZARD_REGISTERED);
        danger.setRegisteredAt(LocalDateTime.now());
        return danger;
    }

    private void validateTarget(String targetType, Long targetId) {
        if (!BizConstants.TARGET_PIPE.equals(targetType)
                && !BizConstants.TARGET_MANHOLE.equals(targetType)) {
            throw new BusinessException("隐患对象类型只能为 PIPE / MANHOLE");
        }
        if (targetId == null) {
            throw new BusinessException("隐患对象ID不能为空");
        }
    }

    private String normalizeSeverity(String severity) {
        if (severity == null || severity.trim().isEmpty()) {
            return "MEDIUM";
        }
        if (!"LOW".equals(severity) && !"MEDIUM".equals(severity) && !"HIGH".equals(severity)) {
            throw new BusinessException("隐患级别只能为 LOW / MEDIUM / HIGH");
        }
        return severity;
    }

    private HiddenDanger mustGet(Long hazardId) {
        HiddenDanger danger = hazardMapper.selectById(hazardId);
        if (danger == null) {
            throw new BusinessException("隐患不存在: " + hazardId);
        }
        return danger;
    }
}
