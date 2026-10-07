package com.evops.pipe.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evops.pipe.entity.PipeWellLocationHist;

/**
 * 井室位置历史 Mapper。历史位置不能被删除：
 * 业务层只允许 insert/select，严禁调用 update/delete 系列方法。
 */
public interface PipeWellLocationHistMapper extends BaseMapper<PipeWellLocationHist> {
}
