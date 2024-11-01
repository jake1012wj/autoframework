package com.autoframework.gen.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.autoframework.common.mybatis.core.mapper.BaseMapperPlus;
import com.autoframework.gen.domain.GenTableColumn;

/**
 * 业务字段 数据层
 *
 * @author Lion Li
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface GenTableColumnMapper extends BaseMapperPlus<GenTableColumn, GenTableColumn> {

}
