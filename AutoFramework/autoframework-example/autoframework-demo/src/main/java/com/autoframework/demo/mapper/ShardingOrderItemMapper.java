package com.autoframework.demo.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.autoframework.demo.domain.ShardingOrderItem;

@Mapper
@DS("sharding")
public interface ShardingOrderItemMapper extends BaseMapper<ShardingOrderItem> {


}
