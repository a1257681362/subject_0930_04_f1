package com.evops.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evops.common.BusinessException;
import com.evops.entity.Inspector;
import com.evops.mapper.InspectorMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InspectorService {

    private final InspectorMapper inspectorMapper;

    public InspectorService(InspectorMapper inspectorMapper) {
        this.inspectorMapper = inspectorMapper;
    }

    public Inspector create(Inspector inspector) {
        if (inspector.getEmployeeNo() == null || inspector.getEmployeeNo().trim().isEmpty()) {
            throw new BusinessException("工号不能为空");
        }
        if (inspector.getName() == null || inspector.getName().trim().isEmpty()) {
            throw new BusinessException("姓名不能为空");
        }
        Long count = inspectorMapper.selectCount(
                new QueryWrapper<Inspector>().eq("employee_no", inspector.getEmployeeNo()));
        if (count != null && count > 0) {
            throw new BusinessException("工号已存在: " + inspector.getEmployeeNo());
        }
        inspector.setId(null);
        if (inspector.getEnabled() == null) {
            inspector.setEnabled(true);
        }
        inspectorMapper.insert(inspector);
        return inspector;
    }

    public List<Inspector> list() {
        return inspectorMapper.selectList(new QueryWrapper<Inspector>().orderByDesc("id"));
    }

    public Inspector mustGetActive(Long id) {
        Inspector inspector = inspectorMapper.selectById(id);
        if (inspector == null) {
            throw new BusinessException("巡检人员不存在: " + id);
        }
        if (Boolean.FALSE.equals(inspector.getEnabled())) {
            throw new BusinessException("巡检人员已停用: " + inspector.getName());
        }
        return inspector;
    }
}
