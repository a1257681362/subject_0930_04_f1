package com.evops.pipe.service;

import com.evops.pipe.PipeConstants;
import com.evops.pipe.entity.NetworkPermission;
import com.evops.pipe.mapper.NetworkPermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 管网权限模式。权限收紧（TIGHTENED）时：
 * 管段档案停止推进（一切写操作拒绝）；
 * 井室档案保留已经确认的事实（已确认数据保留可查、不删除，写操作冻结）。
 */
@Service
@RequiredArgsConstructor
public class NetworkPermissionService {

    private final NetworkPermissionMapper permissionMapper;

    /**
     * 当前模式（单行表，不存在时补建默认 NORMAL）。
     */
    public NetworkPermission current() {
        List<NetworkPermission> all = permissionMapper.selectList(null);
        if (!all.isEmpty()) {
            return all.get(0);
        }
        NetworkPermission p = new NetworkPermission();
        p.setMode(PipeConstants.MODE_NORMAL);
        p.setReason("初始：正常模式");
        permissionMapper.insert(p);
        return p;
    }

    public NetworkPermission tighten(String reason) {
        NetworkPermission p = current();
        p.setMode(PipeConstants.MODE_TIGHTENED);
        p.setReason(StringUtils.hasText(reason) ? reason : "管网权限收紧");
        permissionMapper.updateById(p);
        return p;
    }

    public NetworkPermission restore(String reason) {
        NetworkPermission p = current();
        p.setMode(PipeConstants.MODE_NORMAL);
        p.setReason(StringUtils.hasText(reason) ? reason : "管网权限恢复正常");
        permissionMapper.updateById(p);
        return p;
    }

    public boolean isTightened() {
        return PipeConstants.MODE_TIGHTENED.equals(current().getMode());
    }

    /** 管段档案写操作守卫：权限收紧时停止推进。 */
    public void requireSegmentWritable() {
        if (isTightened()) {
            throw new IllegalStateException("管网权限已收紧，管段档案停止推进");
        }
    }

    /** 井室档案写操作守卫：权限收紧时仅保留已确认事实，禁止变更。 */
    public void requireWellWritable() {
        if (isTightened()) {
            throw new IllegalStateException("管网权限已收紧，井室档案仅保留已确认事实，禁止变更");
        }
    }
}
