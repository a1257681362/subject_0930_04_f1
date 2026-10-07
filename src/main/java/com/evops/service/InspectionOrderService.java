package com.evops.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evops.common.BizConstants;
import com.evops.common.BusinessException;
import com.evops.entity.InspectionOrder;
import com.evops.entity.RouteItem;
import com.evops.mapper.InspectionOrderMapper;
import com.evops.service.support.NoGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** 巡检工单：派检 -> 到场(巡检中) -> 完成 */
@Service
public class InspectionOrderService {

    private final InspectionOrderMapper orderMapper;
    private final InspectorService inspectorService;
    private final InspectionRouteService routeService;
    private final NoGenerator noGenerator;

    public InspectionOrderService(InspectionOrderMapper orderMapper,
                                  InspectorService inspectorService,
                                  InspectionRouteService routeService,
                                  NoGenerator noGenerator) {
        this.orderMapper = orderMapper;
        this.inspectorService = inspectorService;
        this.routeService = routeService;
        this.noGenerator = noGenerator;
    }

    /** 派检：为指定人员与路线生成工单 */
    @Transactional
    public InspectionOrder dispatch(Long routeId, Long assigneeId, LocalDateTime plannedTime) {
        routeService.mustGetRoute(routeId);
        inspectorService.mustGetActive(assigneeId);
        List<RouteItem> items = routeService.listItems(routeId);
        if (items.isEmpty()) {
            throw new BusinessException("路线尚未配置巡检点，无法派检");
        }
        InspectionOrder order = new InspectionOrder();
        order.setOrderNo(noGenerator.next("XJ"));
        order.setRouteId(routeId);
        order.setAssigneeId(assigneeId);
        order.setPlannedTime(plannedTime);
        order.setStatus(BizConstants.ORDER_ASSIGNED);
        orderMapper.insert(order);
        return order;
    }

    /** 巡检人员到场开始作业 */
    @Transactional
    public InspectionOrder start(Long orderId) {
        InspectionOrder order = mustGet(orderId);
        if (BizConstants.ORDER_COMPLETED.equals(order.getStatus())) {
            throw new BusinessException("工单已完成，不能重新开始");
        }
        if (BizConstants.ORDER_ASSIGNED.equals(order.getStatus())) {
            order.setStatus(BizConstants.ORDER_IN_PROGRESS);
            order.setStartedAt(LocalDateTime.now());
            orderMapper.updateById(order);
        }
        return order;
    }

    /** 完成工单 */
    @Transactional
    public InspectionOrder finish(Long orderId) {
        InspectionOrder order = mustGet(orderId);
        if (BizConstants.ORDER_ASSIGNED.equals(order.getStatus())) {
            throw new BusinessException("工单尚未开始，不能完成");
        }
        if (BizConstants.ORDER_COMPLETED.equals(order.getStatus())) {
            return order;
        }
        order.setStatus(BizConstants.ORDER_COMPLETED);
        order.setFinishedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        return order;
    }

    public InspectionOrder get(Long orderId) {
        return mustGet(orderId);
    }

    public List<InspectionOrder> list(String status, Long assigneeId) {
        QueryWrapper<InspectionOrder> wrapper = new QueryWrapper<>();
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq("status", status);
        }
        if (assigneeId != null) {
            wrapper.eq("assignee_id", assigneeId);
        }
        wrapper.orderByDesc("id");
        return orderMapper.selectList(wrapper);
    }

    private InspectionOrder mustGet(Long orderId) {
        InspectionOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("巡检工单不存在: " + orderId);
        }
        return order;
    }
}
