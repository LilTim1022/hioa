package com.example.hioa.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.example.hioa.api.common.util.PageUtils;
import com.example.hioa.api.common.util.R;
import com.example.hioa.api.controller.form.*;
import com.example.hioa.api.db.pojo.TbUser;
import com.example.hioa.api.service.UserService;
import com.example.hioa.api.controller.form.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

//    restcontroller说明返回的都是json格式的
@RestController
//    返回路径是user
@RequestMapping("/user")
//    tag注解给springdoc模块使用
@Tag(name = "UserController", description = "用户Web接口")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 生成登陆二维码的字符串
     */
    @GetMapping("/createQrCode")
    @Operation(summary = "生成二维码Base64格式的字符串")
    public R createQrCode() {
        HashMap map = userService.createQrCode();
        return R.ok(map);
    }

    /**
     * 检测登陆验证码
     *
     * @param form
     * @return
     */
    @PostMapping("/checkQrCode")
    @Operation(summary = "检测登陆验证码")
    public R checkQrCode(@Valid @RequestBody CheckQrCodeForm form) {
        boolean bool = userService.checkQrCode(form.getCode(), form.getUuid());
        return R.ok().put("result", bool);
    }

    @PostMapping("/wechatLogin")
    @Operation(summary = "微信小程序登陆")
    public R wechatLogin(@Valid @RequestBody WechatLoginForm form) {
        HashMap map = userService.wechatLogin(form.getUuid());
        boolean result = (boolean) map.get("result");
        if (result) {
            int userId = (int) map.get("userId");
            StpUtil.setLoginId(userId);
            Set<String> permissions = userService.searchUserPermissions(userId);
            map.remove("userId");
            map.put("permissions", permissions);
        }
        return R.ok(map);
    }

    /**
     * 登陆成功后加载用户的基本信息
     */
    @GetMapping("/loadUserInfo")
    @Operation(summary = "登陆成功后加载用户的基本信息")
    @SaCheckLogin
    public R loadUserInfo() {
        int userId = StpUtil.getLoginIdAsInt();
        HashMap summary = userService.searchUserSummary(userId);
        return R.ok(summary);
    }

    @PostMapping("/searchById")
    @Operation(summary = "根据ID查找用户")
    @SaCheckPermission(value = {"ROOT", "USER:SELECT"}, mode = SaMode.OR)
    public R searchById(@Valid @RequestBody SearchUserByIdForm form) {
        HashMap map = userService.searchById(form.getUserId());
        return R.ok(map);
    }

    @GetMapping("/searchAllUser")
    @Operation(summary = "查询所有用户")
    @SaCheckLogin
    public R searchAllUser() {
        ArrayList<HashMap> list = userService.searchAllUser();
        return R.ok().put("list", list);
    }

    @PostMapping("/login")
    @Operation(summary = "登陆系统")
    public R login(@Valid @RequestBody LoginForm form) {
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        Integer userId = userService.login(param);
        R r = R.ok().put("result", userId != null ? true : false);
        if (userId != null) {
            StpUtil.setLoginId(userId);
            Set<String> permissions = userService.searchUserPermissions(userId);
            /*
             * 因为新版的Chrome浏览器不支持前端Ajax的withCredentials，
             * 导致Ajax无法提交Cookie，所以我们要取出生成的Token返回给前端，
             * 让前端保存在Storage中，然后每次在Ajax的Header上提交Token
             */
            String token=StpUtil.getTokenInfo().getTokenValue();
            r.put("permissions",permissions).put("token",token);
        }
        return r;
    }

    //由于不需要接收任何数据，所以是get请求
    @GetMapping("/logout")
    //Opeartion注解是swagger用到的
    @Operation(summary = "退出系统")
    public R logout() {
        //退出系统主要用到saToken，提供工具类为logout，方法执行时做两件事：1、把Redis中缓存的信息给抹除掉 2、浏览器保存的cookie过期
        //所以哪怕相应没有成功反映给浏览器，但是redis中缓存的令牌信息已经被抹除了；就算是用户再提交请求吧coockie提交给后端项目，但是后端比对token时发现token在redis中没有缓存，所以认定用户登录无效，重新登陆
        StpUtil.logout();
        return R.ok();
    }

    //因为需要接收数据，所以post请求
    @PostMapping("/updatePassword")
    //判定用户有没有登录，登录了才可以改密码;但是改自己的密码不需要查权限。
    @SaCheckLogin
    //Swagger注解
    @Operation(summary = "修改密码")
    public R updatePassword(@Valid @RequestBody UpdatePasswordForm form) {
        //把浏览器中cookie对应的令牌（token字符串），转换成userID
        int userId = StpUtil.getLoginIdAsInt();
        //HashMap传给dao就可以修改密码了；
        // 语法结构创建hashmap对象的时候，同时对里面赋值。
        HashMap param = new HashMap() {{
            put("userId", userId);
            //password是从form表单得到的
            put("password", form.getPassword());
            put("newPassword", form.getNewPassword());
        }};
        //接下来传给业务层，再业务层调用持久层，保存多少条记录
        int rows = userService.updatePassword(param);
        //返回修改多少条记录
        return R.ok().put("rows",rows);
    }

    @PostMapping("/searchUserByPage")
    @Operation(summary = "查询用户分页记录")
    @SaCheckPermission(value = {"ROOT", "USER:SELECT"}, mode = SaMode.OR)
    public R searchUserByPage(@Valid @RequestBody SearchUserByPageForm form) {
        int page = form.getPage();
        int length = form.getLength();
        int start = (page - 1) * length;
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        param.put("start", start);
        PageUtils pageUtils = userService.searchUserByPage(param);
        return R.ok().put("page", pageUtils);
    }

    @PostMapping("/insert")
    @SaCheckPermission(value = {"ROOT", "USER:INSERT"}, mode = SaMode.OR)
    public R insert(@Valid @RequestBody InsertUserForm form) {
        //最终要得到POJO对象
        TbUser user = JSONUtil.parse(form).toBean(TbUser.class);
        user.setStatus((byte)1);
        user.setRole(JSONUtil.parseArray(form.getRole()).toString());
        user.setCreateTime(new Date());
        int rows = userService.insert(user);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/update")
    @SaCheckPermission(value = {"ROOT", "USER:UPDATE"}, mode = SaMode.OR)
    @Operation(summary = "修改用户")
    public R update(@Valid @RequestBody UpdateUserForm form) {
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        param.replace("role", JSONUtil.parseArray(form.getRole()).toString());
        int rows = userService.update(param);
        if (rows == 1) {
            //修改资料后，把该用户踢下线
            StpUtil.logoutByLoginId(form.getUserId());
        }
        return R.ok().put("rows", rows);
    }

    @PostMapping("/deleteUserByIds")
    @SaCheckPermission(value = {"ROOT", "USER:DELETE"}, mode = SaMode.OR)
    @Operation(summary = "删除用户")
    public R deleteUserByIds(@Valid @RequestBody DeleteUserByIdsForm form) {
        Integer userId = StpUtil.getLoginIdAsInt();
        if (ArrayUtil.contains(form.getIds(), userId)) {
            return R.error("您不能删除自己的账户");
        }
        int rows = userService.deleteUserByIds(form.getIds());
        if(rows>0) {
            //把被删除的用户踢下线
            for(Integer id : form.getIds()) {
                StpUtil.logoutByLoginId(id);
            }
        }
        return R.ok().put("rows",rows);
    }

    @PostMapping("/searchNameAndDept")
    @Operation(summary = "查找员工姓名和部门")
    @SaCheckLogin
    public R searchNameAndDept(@Valid @RequestBody SearchNameAndDeptForm form) {
        HashMap map = userService.searchNameAndDept(form.getId());
        return R.ok(map);
    }
}
