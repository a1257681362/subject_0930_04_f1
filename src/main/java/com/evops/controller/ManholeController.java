package com.evops.controller;

import com.evops.common.ApiResponse;
import com.evops.entity.Manhole;
import com.evops.entity.ManholeLocationHistory;
import com.evops.service.ManholeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/manholes")
public class ManholeController {

    private final ManholeService manholeService;

    public ManholeController(ManholeService manholeService) {
        this.manholeService = manholeService;
    }

    /** 建档：井室编号必须唯一，建档坐标自动留痕 */
    @PostMapping
    public ApiResponse<Manhole> create(@RequestBody Manhole manhole) {
        return ApiResponse.ok(manholeService.create(manhole));
    }

    /** 变更：坐标变化自动追加历史位置，历史位置只追加不可删除 */
    @PutMapping("/{id}")
    public ApiResponse<Manhole> update(@PathVariable Long id,
                                       @RequestBody Manhole manhole,
                                       @RequestParam(required = false) String locationChangeRemark) {
        manhole.setId(id);
        return ApiResponse.ok(manholeService.update(manhole, locationChangeRemark));
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<Manhole> confirm(@PathVariable Long id) {
        return ApiResponse.ok(manholeService.confirm(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<Manhole> get(@PathVariable Long id) {
        return ApiResponse.ok(manholeService.get(id));
    }

    @GetMapping
    public ApiResponse<List<Manhole>> list(@RequestParam(required = false) String riskLevel) {
        return ApiResponse.ok(manholeService.list(riskLevel));
    }

    @GetMapping("/{id}/location-history")
    public ApiResponse<List<ManholeLocationHistory>> locationHistory(@PathVariable Long id) {
        return ApiResponse.ok(manholeService.listLocationHistory(id));
    }
}
