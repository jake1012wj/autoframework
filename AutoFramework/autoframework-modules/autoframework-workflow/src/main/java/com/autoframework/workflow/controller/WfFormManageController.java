package com.autoframework.workflow.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import com.autoframework.common.idempotent.annotation.RepeatSubmit;
import com.autoframework.common.log.annotation.Log;
import com.autoframework.common.web.core.BaseController;
import com.autoframework.common.mybatis.core.page.PageQuery;
import com.autoframework.common.core.domain.R;
import com.autoframework.common.core.validate.AddGroup;
import com.autoframework.common.core.validate.EditGroup;
import com.autoframework.common.log.enums.BusinessType;
import com.autoframework.common.excel.utils.ExcelUtil;
import com.autoframework.workflow.domain.vo.WfFormManageVo;
import com.autoframework.workflow.domain.bo.WfFormManageBo;
import com.autoframework.workflow.service.IWfFormManageService;
import com.autoframework.common.mybatis.core.page.TableDataInfo;

/**
 * 表单管理
 *
 * @author may
 * @date 2024-03-29
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/workflow/formManage")
public class WfFormManageController extends BaseController {

    private final IWfFormManageService wfFormManageService;

    /**
     * 查询表单管理列表
     */
    @SaCheckPermission("workflow:formManage:list")
    @GetMapping("/list")
    public TableDataInfo<WfFormManageVo> list(WfFormManageBo bo, PageQuery pageQuery) {
        return wfFormManageService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询表单管理列表
     */
    @SaCheckPermission("workflow:formManage:list")
    @GetMapping("/list/selectList")
    public R<List<WfFormManageVo>> selectList() {
        return R.ok(wfFormManageService.selectList());
    }

    /**
     * 导出表单管理列表
     */
    @SaCheckPermission("workflow:formManage:export")
    @Log(title = "表单管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(WfFormManageBo bo, HttpServletResponse response) {
        List<WfFormManageVo> list = wfFormManageService.queryList(bo);
        ExcelUtil.exportExcel(list, "表单管理", WfFormManageVo.class, response);
    }

    /**
     * 获取表单管理详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("workflow:formManage:query")
    @GetMapping("/{id}")
    public R<WfFormManageVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(wfFormManageService.queryById(id));
    }

    /**
     * 新增表单管理
     */
    @SaCheckPermission("workflow:formManage:add")
    @Log(title = "表单管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody WfFormManageBo bo) {
        return toAjax(wfFormManageService.insertByBo(bo));
    }

    /**
     * 修改表单管理
     */
    @SaCheckPermission("workflow:formManage:edit")
    @Log(title = "表单管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody WfFormManageBo bo) {
        return toAjax(wfFormManageService.updateByBo(bo));
    }

    /**
     * 删除表单管理
     *
     * @param ids 主键串
     */
    @SaCheckPermission("workflow:formManage:remove")
    @Log(title = "表单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(wfFormManageService.deleteByIds(List.of(ids)));
    }
}
