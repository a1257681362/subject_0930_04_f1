package com.evops.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evops.common.BizConstants;
import com.evops.entity.SysConfig;
import com.evops.mapper.SysConfigMapper;
import org.springframework.stereotype.Service;

@Service
public class SysConfigService {

    private final SysConfigMapper sysConfigMapper;

    public SysConfigService(SysConfigMapper sysConfigMapper) {
        this.sysConfigMapper = sysConfigMapper;
    }

    /** 城市地下管网权限是否已收紧 */
    public boolean isPermissionRestricted() {
        return Boolean.parseBoolean(getValue(BizConstants.CFG_PERMISSION_RESTRICTED));
    }

    public String getValue(String key) {
        SysConfig config = sysConfigMapper.selectOne(
                new QueryWrapper<SysConfig>().eq("config_key", key));
        return config == null ? null : config.getConfigValue();
    }

    /** 设置权限收紧开关，供管理接口调用 */
    public void setPermissionRestricted(boolean restricted) {
        SysConfig config = sysConfigMapper.selectOne(
                new QueryWrapper<SysConfig>().eq("config_key", BizConstants.CFG_PERMISSION_RESTRICTED));
        if (config == null) {
            config = new SysConfig();
            config.setConfigKey(BizConstants.CFG_PERMISSION_RESTRICTED);
            config.setConfigValue(Boolean.toString(restricted));
            config.setDescription("城市地下管网权限收紧开关");
            sysConfigMapper.insert(config);
        } else {
            config.setConfigValue(Boolean.toString(restricted));
            sysConfigMapper.updateById(config);
        }
    }
}
