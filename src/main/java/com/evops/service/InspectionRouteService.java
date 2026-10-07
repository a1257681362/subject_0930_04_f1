package com.evops.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evops.common.BizConstants;
import com.evops.common.BusinessException;
import com.evops.entity.InspectionRoute;
import com.evops.entity.Manhole;
import com.evops.entity.PipeSegment;
import com.evops.entity.RouteItem;
import com.evops.mapper.InspectionRouteMapper;
import com.evops.mapper.ManholeMapper;
import com.evops.mapper.PipeSegmentMapper;
import com.evops.mapper.RouteItemMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 巡检路线：由管段/井室巡检点按顺序组成 */
@Service
public class InspectionRouteService {

    private final InspectionRouteMapper routeMapper;
    private final RouteItemMapper routeItemMapper;
    private final PipeSegmentMapper pipeSegmentMapper;
    private final ManholeMapper manholeMapper;

    public InspectionRouteService(InspectionRouteMapper routeMapper,
                                  RouteItemMapper routeItemMapper,
                                  PipeSegmentMapper pipeSegmentMapper,
                                  ManholeMapper manholeMapper) {
        this.routeMapper = routeMapper;
        this.routeItemMapper = routeItemMapper;
        this.pipeSegmentMapper = pipeSegmentMapper;
        this.manholeMapper = manholeMapper;
    }

    public InspectionRoute create(InspectionRoute route) {
        if (route.getRouteName() == null || route.getRouteName().trim().isEmpty()) {
            throw new BusinessException("路线名称不能为空");
        }
        Long count = routeMapper.selectCount(
                new QueryWrapper<InspectionRoute>().eq("route_name", route.getRouteName()));
        if (count != null && count > 0) {
            throw new BusinessException("路线名称已存在: " + route.getRouteName());
        }
        route.setId(null);
        routeMapper.insert(route);
        return route;
    }

    @Transactional
    public RouteItem addItem(Long routeId, String targetType, Long targetId, Integer seqNo) {
        mustGetRoute(routeId);
        if (BizConstants.TARGET_PIPE.equals(targetType)) {
            PipeSegment pipe = pipeSegmentMapper.selectById(targetId);
            if (pipe == null) {
                throw new BusinessException("管段不存在: " + targetId);
            }
        } else if (BizConstants.TARGET_MANHOLE.equals(targetType)) {
            Manhole manhole = manholeMapper.selectById(targetId);
            if (manhole == null) {
                throw new BusinessException("井室不存在: " + targetId);
            }
        } else {
            throw new BusinessException("巡检点类型只能为 PIPE / MANHOLE");
        }
        RouteItem item = new RouteItem();
        item.setRouteId(routeId);
        item.setTargetType(targetType);
        item.setTargetId(targetId);
        item.setSeqNo(seqNo == null ? 0 : seqNo);
        routeItemMapper.insert(item);
        return item;
    }

    public List<RouteItem> listItems(Long routeId) {
        mustGetRoute(routeId);
        return routeItemMapper.selectList(new QueryWrapper<RouteItem>()
                .eq("route_id", routeId).orderByAsc("seq_no").orderByAsc("id"));
    }

    public List<InspectionRoute> list() {
        return routeMapper.selectList(new QueryWrapper<InspectionRoute>().orderByDesc("id"));
    }

    public InspectionRoute mustGetRoute(Long routeId) {
        InspectionRoute route = routeMapper.selectById(routeId);
        if (route == null) {
            throw new BusinessException("巡检路线不存在: " + routeId);
        }
        return route;
    }
}
