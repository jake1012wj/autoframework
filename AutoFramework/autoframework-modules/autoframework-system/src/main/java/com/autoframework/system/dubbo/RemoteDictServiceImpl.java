package com.autoframework.system.dubbo;

import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import com.autoframework.common.core.utils.MapstructUtils;
import com.autoframework.system.api.RemoteDictService;
import com.autoframework.system.api.domain.vo.RemoteDictDataVo;
import com.autoframework.system.domain.vo.SysDictDataVo;
import com.autoframework.system.service.ISysDictTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典服务
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
@DubboService
public class RemoteDictServiceImpl implements RemoteDictService {

    private final ISysDictTypeService sysDictTypeService;

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    @Override
    public List<RemoteDictDataVo> selectDictDataByType(String dictType) {
        List<SysDictDataVo> list = sysDictTypeService.selectDictDataByType(dictType);
        return MapstructUtils.convert(list, RemoteDictDataVo.class);
    }

}
