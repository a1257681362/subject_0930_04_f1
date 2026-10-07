package com.evops.pipe.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evops.pipe.PipeConstants;
import com.evops.pipe.dto.HazardRegisterRequest;
import com.evops.pipe.dto.ReviewRequest;
import com.evops.pipe.entity.Hazard;
import com.evops.pipe.entity.InspectOrder;
import com.evops.pipe.entity.InspectRecord;
import com.evops.pipe.mapper.HazardMapper;
import com.evops.pipe.mapper.InspectOrderMapper;
import com.evops.pipe.mapper.InspectRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 隐患登记与复核。
 * 复核通过：该工单隐患全部复核通过且现场记录完成时，工单自动闭环；
 * 复核退回：工单退回“已派检”，需重新现场记录。
 * （抢修结算后续迭代，不在本期范围。）
 */
@Service
@RequiredArgsConstructor
public class HazardService {

    private final HazardMapper hazardMapper;
    private final InspectRecordMapper recordMapper;
    private final InspectOrderMapper orderMapper;
    private final InspectOrderService orderService;

    /**
     * 隐患登记：必须来源于标记了隐患的现场记录。
     */
    @Transactional(rollbackFor = Exception.class)
    public Hazard register(HazardRegisterRequest req) {
        InspectOrder order = orderService.mustGet(req.getOrderId());
        if (PipeConstants.ORDER_CLOSED.equals(order.getStatus())) {
            throw new IllegalStateException("工单已闭环，不能登记隐患");
        }
        InspectRecord record = recordMapper.selectById(req.getRecordId());
        if (record == null || !record.getOrderId().equals(req.getOrderId())) {
            throw new IllegalArgumentException("现场记录与工单不匹配");
        }
        if (!Boolean.TRUE.equals(record.getHasHazard())) {
            throw new IllegalStateException("该现场记录未标记隐患，不能登记");
        }
        Hazard hazard = new Hazard();
        hazard.setHazardNo(InspectOrderService.genNo("HZ"));
        hazard.setOrderId(req.getOrderId());
        hazard.setRecordId(req.getRecordId());
        hazard.setWellId(record.getWellId());
        hazard.setSegmentId(record.getSegmentId());
        hazard.setHazardDesc(req.getHazardDesc());
        hazard.setHazardLevel(StringUtils.hasText(req.getHazardLevel()) ? req.getHazardLevel() : "GENERAL");
        hazard.setStatus(PipeConstants.HAZARD_REGISTERED);
        hazardMapper.insert(hazard);
        return hazard;
    }

    /**
     * 隐患复核。
     */
    @Transactional(rollbackFor = Exception.class)
    public Hazard review(Long id, ReviewRequest req) {
        Hazard hazard = mustGet(id);
        if (!PipeConstants.HAZARD_REGISTERED.equals(hazard.getStatus())) {
            throw new IllegalStateException("隐患已复核，请勿重复操作: " + hazard.getHazardNo());
        }
        hazard.setReviewedBy(req.getReviewedBy());
        hazard.setReviewedAt(LocalDateTime.now());
        hazard.setReviewComment(req.getReviewComment());
        InspectOrder order = orderService.mustGet(hazard.getOrderId());
        if (Boolean.TRUE.equals(req.getPass())) {
            hazard.setStatus(PipeConstants.HAZARD_REVIEW_PASS);
            hazardMapper.updateById(hazard);
            // 该工单隐患全部复核通过且现场记录完成 → 工单闭环
            Long remaining = hazardMapper.selectCount(new LambdaQueryWrapper<Hazard>()
                    .eq(Hazard::getOrderId, hazard.getOrderId())
                    .eq(Hazard::getStatus, PipeConstants.HAZARD_REGISTERED));
            if (remaining != null && remaining == 0
                    && PipeConstants.ORDER_FIELD_RECORDED.equals(order.getStatus())) {
                order.setStatus(PipeConstants.ORDER_CLOSED);
                orderMapper.updateById(order);
            }
        } else {
            hazard.setStatus(PipeConstants.HAZARD_REVIEW_REJECT);
            hazardMapper.updateById(hazard);
            // 复核退回 → 工单退回待重检
            order.setStatus(PipeConstants.ORDER_DISPATCHED);
            orderMapper.updateById(order);
        }
        return hazard;
    }

    public Hazard mustGet(Long id) {
        Hazard hazard = hazardMapper.selectById(id);
        if (hazard == null) {
            throw new IllegalArgumentException("隐患不存在: " + id);
        }
        return hazard;
    }

    public List<Hazard> listByOrder(Long orderId) {
        LambdaQueryWrapper<Hazard> wrapper = new LambdaQueryWrapper<>();
        if (orderId != null) {
            wrapper.eq(Hazard::getOrderId, orderId);
        }
        return hazardMapper.selectList(wrapper.orderByAsc(Hazard::getId));
    }
}
