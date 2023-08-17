package com.example.hioa.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.example.hioa.api.common.util.PageUtils;
import com.example.hioa.api.common.util.R;
import com.example.hioa.api.controller.form.DeleteReimByIdForm;
import com.example.hioa.api.controller.form.InsertReimForm;
import com.example.hioa.api.controller.form.SearchReimByIdForm;
import com.example.hioa.api.controller.form.SearchReimByPageForm;
import com.example.hioa.api.db.pojo.TbReim;
import com.example.hioa.api.service.ReimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;

@RestController
@RequestMapping("/reim")
@Tag(name = "ReimController", description = "报销模块Web接口")
public class ReimController {
    @Autowired
    private ReimService reimService;

    @PostMapping("/searchReimByPage")
    @Operation(summary = "查询报销分页记录")
    @SaCheckLogin
    public R searchReimByPage(@Valid @RequestBody SearchReimByPageForm form) {
        int page = form.getPage();
        int length = form.getLength();
        int start = (page - 1)*length;
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        param.put("start", start);
        param.put("currentUserId", StpUtil.getLoginIdAsInt());
        //权限
        if (!(StpUtil.hasPermission("REIM:SELECT") || StpUtil.hasPermission("ROOT"))) {
            //没有这两个权限只能看到自己的报销记录
            param.put("userId", StpUtil.getLoginIdAsInt());
        }
        PageUtils pageUtils = reimService.searchReimByPage(param);
        return R.ok().put("page", pageUtils);
    }

    @PostMapping("/insert")
    @Operation(summary = "添加报销申请")
    @SaCheckLogin
    public R insert(@Valid @RequestBody InsertReimForm form) {
        //判断content是否是json数组
        if (!JSONUtil.isJsonArray(form.getContent())) {
            return R.error("content不是JSON数组");
        }
        TbReim reim = JSONUtil.parse(form).toBean(TbReim.class);
        reim.setUserId(StpUtil.getLoginIdAsInt());
        reim.setStatus((byte)1);
        //调用servic层
        int rows = reimService.insert(reim);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/searchReimById")
    @Operation(summary = "根据ID查询报销单")
    @SaCheckLogin
    public R searchReimById(@Valid @RequestBody SearchReimByIdForm form) {
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        //条件判断用户身份是否是管理者
        if (!(StpUtil.hasPermission("REIM:SELECT")|| StpUtil.hasPermission("ROOT"))) {
            //是普通用户，添加userId作为查询条件
            param.put("userId", StpUtil.getLoginIdAsInt());
        }
        HashMap map = reimService.searchReimById(param);
        return R.ok(map);
    }

    @PostMapping("/deleteReimById")
    @Operation(summary = "删除报销申请")
    @SaCheckLogin
    public R deleteReimById(@Valid @RequestBody DeleteReimByIdForm form) {
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        param.put("userId", StpUtil.getLoginIdAsInt());
        int rows = reimService.deleteReimById(param);
        return R.ok().put("rows", rows);
    }
}
