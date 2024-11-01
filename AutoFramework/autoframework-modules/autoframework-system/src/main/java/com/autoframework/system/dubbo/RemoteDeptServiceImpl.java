package com.autoframework.system.dubbo;

import com.autoframework.system.api.RemoteDeptService;
import com.autoframework.system.service.ISysDeptService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

/**
 * 部门服务
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
@DubboService
public class RemoteDeptServiceImpl implements RemoteDeptService {

    private final ISysDeptService sysDeptService;

    /**
     * 通过部门ID查询部门名称
     *
     * @param deptIds 部门ID串逗号分隔
     * @return 部门名称串逗号分隔
     */
    @Override
    public String selectDeptNameByIds(String deptIds) {
        return sysDeptService.selectDeptNameByIds(deptIds);
    }
}
