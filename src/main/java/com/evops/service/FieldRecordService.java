package com.evops.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evops.common.BizConstants;
import com.evops.common.BusinessException;
import com.evops.entity.FieldRecord;
import com.evops.entity.InspectionOrder;
import com.evops.entity.RouteItem;
import com.evops.mapper.FieldRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** 现场巡检记录：巡检人对工单路线上的管段/井室逐项记录 */
@Service
public class FieldRecordService {

    private final FieldRecordMapper fieldRecordMapper;
    private final InspectionOrderService orderService;
    private final InspectionRouteService routeService;

    public FieldRecordService(FieldRecordMapper fieldRecordMapper,
                              InspectionOrderService orderService,
                              InspectionRouteService routeService) {
        this.fieldRecordMapper = fieldRecordMapper;
        this.orderService = orderService;
        this.routeService = routeService;
    }

    @Transactional
    public FieldRecord record(Long orderId, String targetType, Long targetId,
                              String conditionStatus, String content, boolean hasIssue) {
        InspectionOrder order = orderService.get(orderId);
        if (BizConstants.ORDER_ASSIGNED.equals(order.getStatus())) {
            throw new BusinessException("工单尚未开始巡检，不能记录现场情况");
        }
        if (BizConstants.ORDER_COMPLETED.equals(order.getStatus())) {
            throw new BusinessException("工单已完成，不能再补录现场情况");
        }
        if (!BizConstants.TARGET_PIPE.equals(targetType)
                && !BizConstants.TARGET_MANHOLE.equals(targetType)) {
            throw new BusinessException("巡检对象类型只能为 PIPE / MANHOLE");
        }
        // 记录点必须在工单路线上
        List<RouteItem> items = routeService.listItems(order.getRouteId());
        boolean onRoute = items.stream().anyMatch(item ->
                targetType.equals(item.getTargetType()) && targetId.equals(item.getTargetId()));
        if (!onRoute) {
            throw new BusinessException("巡检对象不在该工单路线上");
        }

        FieldRecord record = new FieldRecord();
        record.setOrderId(orderId);
        record.setTargetType(targetType);
        record.setTargetId(targetId);
        record.setInspectorId(order.getAssigneeId());
        record.setRecordTime(LocalDateTime.now());
        record.setConditionStatus(conditionStatus);
        record.setContent(content);
        record.setHasIssue(hasIssue);
        fieldRecordMapper.insert(record);
        return record;
    }

    public List<FieldRecord> listByOrder(Long orderId) {
        return fieldRecordMapper.selectList(new QueryWrapper<FieldRecord>()
                .eq("order_id", orderId).orderByAsc("record_time").orderByAsc("id"));
    }
}
