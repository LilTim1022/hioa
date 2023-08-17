package com.example.hioa.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.example.hioa.api.common.util.PageUtils;
import com.example.hioa.api.common.util.R;
import com.example.hioa.api.controller.form.DeleteLeaveByIdForm;
import com.example.hioa.api.controller.form.InsertLeaveForm;
import com.example.hioa.api.controller.form.SearchLeaveByIdForm;
import com.example.hioa.api.controller.form.SearchLeaveByPageForm;
import com.example.hioa.api.db.pojo.TbLeave;
import com.example.hioa.api.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

@RestController
@RequestMapping("/leave")
@Tag(name="LeaveController", description = "员工请假Web接口")
public class LeaveController {
    @Autowired
    private LeaveService leaveService;

    @PostMapping("/searchLeaveByPage")
    @Operation(summary = "查询请假分页数据")
    @SaCheckLogin
    public R searchLeaveByPage(@Valid @RequestBody SearchLeaveByPageForm form) {
        int page = form.getPage();
        int length = form.getLength();
        int start = (page - 1) * length;
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        param.put("start", start);
        param.put("myId", StpUtil.getLoginIdAsInt());
        //判断权限
        if (!(StpUtil.hasPermission("LEAVE:SELECT") || StpUtil.hasPermission("ROOT"))) {
            param.put("userId", StpUtil.getLoginIdAsInt());
        }
        PageUtils pageUtils = leaveService.searchLeaveByPage(param);
        return R.ok().put("page", pageUtils);
    }

    @PostMapping("/insert")
    @Operation(summary = "添加请假记录")
    @SaCheckLogin
    public R insert(@Valid @RequestBody InsertLeaveForm form) {
        //验证开始与结束时间
        DateTime date_1 = DateUtil.parse(form.getStart());
        DateTime date_2 = DateUtil.parse(form.getEnd());
        if (date_1.isAfterOrEquals(date_2)) {
            return R.error("请假开始时间不能晚于截止时间");
        }
        HashMap param = new HashMap() {{
            put("userId", StpUtil.getLoginIdAsInt());
            put("start", form.getStart());
            put("end", form.getEnd());
        }};
        if (leaveService.searchContradiction(param)) {
            return R.error("当前请假时间与已与请假申请日期上有交集覆盖");
        }
        //计算查多少小时
        long hours = date_1.between(date_2, DateUnit.HOUR);
        String days = new BigDecimal(hours).divide(new BigDecimal(24), 1, RoundingMode.CEILING).toString(); //hours 除以24
        if (days.contains(".0")) {
            //抹掉.0
            days = days.replace(".0", "");
        }
        if (days.equals("0")) {
            //days等于0，但是可能是hours那里的小时是半个小时，取整后=0，接着计算days时也是=0，一律视为0.1天
            days = "0.1";
        }
        //把form转换为pojo对象
        TbLeave leave = JSONUtil.parse(form).toBean(TbLeave.class);
        leave.setUserId(StpUtil.getLoginIdAsInt());
        leave.setDays(days);
        int rows = leaveService.insert(leave);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/deleteLeaveById")
    @Operation(summary = "删除请假记录")
    @SaCheckLogin
    public R deleteLeaveById(@Valid @RequestBody DeleteLeaveByIdForm form) {
        HashMap param = new HashMap() {{
            put("id", form.getId());
            put("userId", StpUtil.getLoginIdAsInt());
        }};
        int rows = leaveService.deleteLeaveById(param);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/searchLeaveById")
    @Operation(summary = "根据ID查询请假数据")
    @SaCheckLogin
    public R searchLeaveById(@Valid @RequestBody SearchLeaveByIdForm form) {
        //form转hashmap
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        //判断权限，如果有权限就不往param中加userId了
        if (!(StpUtil.hasPermission("LEAVE:SELECT") || StpUtil.hasPermission("ROOT"))) {
            //往hashmap中加入userId
            param.put("userId", StpUtil.getLoginIdAsInt());
        }
        //HashMap传给业务层
        HashMap map = leaveService.searchLeaveById(param);
        return R.ok(map);
    }
}
