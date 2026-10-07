package com.evops.controller;

import com.evops.common.ApiResponse;
import com.evops.entity.InspectionRoute;
import com.evops.entity.RouteItem;
import com.evops.service.InspectionRouteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class InspectionRouteController {

    private final InspectionRouteService routeService;

    public InspectionRouteController(InspectionRouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping
    public ApiResponse<InspectionRoute> create(@RequestBody InspectionRoute route) {
        return ApiResponse.ok(routeService.create(route));
    }

    @GetMapping
    public ApiResponse<List<InspectionRoute>> list() {
        return ApiResponse.ok(routeService.list());
    }

    /** 向路线追加巡检点：targetType=PIPE/MANHOLE */
    @PostMapping("/{id}/items")
    public ApiResponse<RouteItem> addItem(@PathVariable Long id,
                                          @RequestParam String targetType,
                                          @RequestParam Long targetId,
                                          @RequestParam(required = false) Integer seqNo) {
        return ApiResponse.ok(routeService.addItem(id, targetType, targetId, seqNo));
    }

    @GetMapping("/{id}/items")
    public ApiResponse<List<RouteItem>> listItems(@PathVariable Long id) {
        return ApiResponse.ok(routeService.listItems(id));
    }
}
