package com.evops.pipe.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evops.pipe.PipeConstants;
import com.evops.pipe.dto.DispatchRequest;
import com.evops.pipe.dto.RecordRequest;
import com.evops.pipe.entity.Hazard;
import com.evops.pipe.entity.InspectOrder;
import com.evops.pipe.entity.InspectOrderRoute;
import com.evops.pipe.entity.InspectRecord;
import com.evops.pipe.mapper.HazardMapper;
import com.evops.pipe.mapper.InspectOrderMapper;
import com.evops.pipe.mapper.InspectOrderRouteMapper;
import com.evops.pipe.mapper.InspectRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 巡检工单：派检 -> 现场记录 -> （隐患登记）-> 复核闭环。
 */
@Service
@RequiredArgsConstructor
public class InspectOrderService {

    private final InspectOrderMapper orderMapper;
    private final InspectOrderRouteMapper routeMapper;
    private final InspectRecordMapper recordMapper;
    private final HazardMapper hazardMapper;
    private final PipeSegmentService segmentService;
    private final PipeWellService wellService;

    /**
     * 派检：生成工单并落路线明细。
     */
    @Transactional(rollbackFor = Exception.class)
    public InspectOrder dispatch(DispatchRequest req) {
        InspectOrder order = new InspectOrder();
        order.setOrderNo(genNo("IO"));
        order.setInspectorName(req.getInspectorName());
        order.setRouteDesc(req.getRouteDesc());
        order.setStatus(PipeConstants.ORDER_DISPATCHED);
        orderMapper.insert(order);

        int seq = 1;
        for (DispatchRequest.RouteItem item : req.getRoute()) {
            if (item.getWellId() == null && item.getSegmentId() == null) {
                throw new IllegalArgumentException("路线第 " + seq + " 站必须关联井室或管段");
            }
            InspectOrderRoute route = new InspectOrderRoute();
            route.setOrderId(order.getId());
            if (item.getWellId() != null) {
                wellService.mustGet(item.getWellId());
                route.setWellId(item.getWellId());
            }
            if (item.getSegmentId() != null) {
                segmentService.mustGet(item.getSegmentId());
                route.setSegmentId(item.getSegmentId());
            }
            route.setSeqNo(seq++);
            routeMapper.insert(route);
        }
        return order;
    }

    /**
     * 现场记录：记录发现问题，工单推进为“现场记录已提交”。
     */
    @Transactional(rollbackFor = Exception.class)
    public InspectRecord addRecord(Long orderId, RecordRequest req) {
        InspectOrder order = mustGet(orderId);
        if (!PipeConstants.ORDER_DISPATCHED.equals(order.getStatus())
                && !PipeConstants.ORDER_FIELD_RECORDED.equals(order.getStatus())) {
            throw new IllegalStateException("工单当前状态不允许现场记录: " + order.getStatus());
        }
        if (req.getWellId() == null && req.getSegmentId() == null) {
            throw new IllegalArgumentException("现场记录必须关联井室或管段");
        }
        List<InspectOrderRoute> routes = routesOf(orderId);
        boolean inRoute = routes.stream().anyMatch(r ->
                (req.getWellId() != null && req.getWellId().equals(r.getWellId()))
                        || (req.getSegmentId() != null && req.getSegmentId().equals(r.getSegmentId())));
        if (!inRoute) {
            throw new IllegalArgumentException("记录对象不在工单路线内");
        }
        InspectRecord record = new InspectRecord();
        record.setOrderId(orderId);
        record.setWellId(req.getWellId());
        record.setSegmentId(req.getSegmentId());
        record.setFindingDesc(req.getFindingDesc());
        record.setHasHazard(Boolean.TRUE.equals(req.getHasHazard()));
        record.setRecordedBy(req.getRecordedBy());
        recordMapper.insert(record);

        order.setStatus(PipeConstants.ORDER_FIELD_RECORDED);
        orderMapper.updateById(order);
        return record;
    }

    /**
     * 复核闭环：现场记录完成且无待复核隐患时，工单闭环。
     */
    @Transactional(rollbackFor = Exception.class)
    public InspectOrder close(Long orderId) {
        InspectOrder order = mustGet(orderId);
        if (!PipeConstants.ORDER_FIELD_RECORDED.equals(order.getStatus())) {
            throw new IllegalStateException("仅现场记录完成的工单可复核闭环，当前状态: " + order.getStatus());
        }
        Long registered = hazardMapper.selectCount(new LambdaQueryWrapper<Hazard>()
                .eq(Hazard::getOrderId, orderId)
                .eq(Hazard::getStatus, PipeConstants.HAZARD_REGISTERED));
        if (registered != null && registered > 0) {
            throw new IllegalStateException("存在未复核隐患，不能闭环");
        }
        order.setStatus(PipeConstants.ORDER_CLOSED);
        orderMapper.updateById(order);
        return order;
    }

    /**
     * 工单详情：工单 + 路线 + 现场记录 + 隐患。
     */
    public Map<String, Object> detail(Long orderId) {
        InspectOrder order = mustGet(orderId);
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("order", order);
        detail.put("routes", routesOf(orderId));
        detail.put("records", recordMapper.selectList(new LambdaQueryWrapper<InspectRecord>()
                .eq(InspectRecord::getOrderId, orderId).orderByAsc(InspectRecord::getId)));
        detail.put("hazards", hazardMapper.selectList(new LambdaQueryWrapper<Hazard>()
                .eq(Hazard::getOrderId, orderId).orderByAsc(Hazard::getId)));
        return detail;
    }

    public List<InspectOrderRoute> routesOf(Long orderId) {
        return routeMapper.selectList(new LambdaQueryWrapper<InspectOrderRoute>()
                .eq(InspectOrderRoute::getOrderId, orderId).orderByAsc(InspectOrderRoute::getSeqNo));
    }

    public InspectOrder mustGet(Long id) {
        InspectOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new IllegalArgumentException("工单不存在: " + id);
        }
        return order;
    }

    public List<InspectOrder> list() {
        return orderMapper.selectList(new LambdaQueryWrapper<InspectOrder>().orderByAsc(InspectOrder::getId));
    }

    static String genNo(String prefix) {
        return prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
