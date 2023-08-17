package com.example.hioa.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.example.hioa.api.common.util.PageUtils;
import com.example.hioa.api.common.util.R;
import com.example.hioa.api.controller.form.*;
import com.example.hioa.api.controller.form.*;
import com.example.hioa.api.db.pojo.TbAmect;
import com.example.hioa.api.service.AmectService;
import com.example.hioa.api.websocket.WebSocketService;
import com.example.hioa.api.wxpay.WXPayUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/amect")
@Tag(name= "AmectController", description = "罚款Web接口")
@Slf4j
public class AmectController {
    @Autowired
    private AmectService amectService;

    @Value("${wx.key")
    private String key;

    @PostMapping("/searchAmectByPage")
    @Operation(summary = "查询罚款分页记录")
    @SaCheckLogin
    public R searchAmectByPage(@Valid @RequestBody SearchAmectByPageForm form) {
        if((form.getStartDate() != null && form.getEndDate() == null) || (form.getStartDate() == null && form.getEndDate() != null) ) {
            return R.error("startDate和endDate只能同时为空，或者不为空");
        }

        int page = form.getPage();
        int length = form.getLength();
        int start = (page - 1) * length;
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        param.put("start", start);
        param.put("currentUserId", StpUtil.getLoginIdAsInt());
        if (!(StpUtil.hasPermission("AMECT:SELECT") || StpUtil.hasPermission("ROOT"))) {
            param.put("userId", StpUtil.getLoginIdAsInt());
        }

        PageUtils pageUtils = amectService.searchAmectByPage(param);
        return R.ok().put("page", pageUtils);
    }

    @PostMapping("/insert")
    @Operation
    @SaCheckPermission(value = {"ROOT", "AMECT:INSERT"}, mode = SaMode.OR)
    public R insert(@Valid @RequestBody InsertAmectForm form) {
        ArrayList<TbAmect> list = new ArrayList<TbAmect>();
        for (Integer userId: form.getUserId()) {
            TbAmect amect = new TbAmect();
            amect.setAmount(new BigDecimal(form.getAmount()));
            amect.setTypeId(form.getTypeId());
            amect.setReason(form.getReason());
            amect.setUserId(userId);
            //自动生成UUID字符串
            amect.setUuid(IdUtil.simpleUUID());
            list.add(amect);
        }
        int rows = amectService.insert(list);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/searchById")
    @Operation(summary = "根据ID查找罚款记录")
    @SaCheckPermission(value = {"ROOT", "AMECT:SELECT"}, mode = SaMode.OR)
    public R searchById(@Valid @RequestBody SearchAmectByIdForm form) {
        HashMap map = amectService.searchById(form.getId());
        return R.ok(map);
    }

    @PostMapping("/update")
    @Operation(summary = "更新罚款记录")
    @SaCheckPermission(value = {"ROOT", "AMECT:UPDATE"}, mode = SaMode.OR)
    public R update(@Valid @RequestBody UpdateAmectForm form) {
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        int rows = amectService.update(param);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/deleteAmectByIds")
    @Operation(summary = "删除罚款记录")
    @SaCheckPermission(value = {"ROOT", "AMECT:DELETE"}, mode = SaMode.OR)
    public R deleteAmectByIds(@Valid @RequestBody DeleteAmectByIdsForm form) {
        int rows = amectService.deleteAmectByIds(form.getIds());
        return R.ok().put("rows", rows);
    }

    @PostMapping("/createNativeAmectPayOrder")
    @Operation(summary = "创建Native支付罚款订单")
    @SaCheckLogin
    public R createNativeAmectPayOrder(@Valid @RequestBody CreateNativeAmectPayOrderForm form) {
        int userId = StpUtil.getLoginIdAsInt();
        int amectId = form.getAmectId();
        HashMap param = new HashMap() {{
            put("userId", userId);
            put("amectId", amectId);
        }};
        String qrCodeBase64 = amectService.createNativeAmectPayOrder(param);
        return R.ok().put("qrCodeBase64", qrCodeBase64);
    }

    @RequestMapping("/recieveMessage")
    @Operation(summary = "接收通知消息")
    public void recieveMessage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setCharacterEncoding("utf-8");
        //创建io流，读取文字数据，用reader
        Reader reader = request.getReader();
        //缓冲流提高效率
        BufferedReader buffer = new BufferedReader(reader);
        //读取数据
        String line = buffer.readLine();
        StringBuffer temp = new StringBuffer();
        //判断第一行是否有效，不为空
        while (line != null) {
            temp.append(line);
            line = buffer.readLine();
        }
        //关闭io流
        buffer.close();
        reader.close();
        String xml = temp.toString();
        //数字签名验证
        if (WXPayUtil.isSignatureValid(xml, key)) { //数字签名验证成功
            //xml转map
            Map<String, String> map = WXPayUtil.xmlToMap(xml);
            String resultCode = map.get("result_code");
            String returnCode = map.get("return_code");
            if("SUCCESS".equals(resultCode) && "SUCCESS".equals(returnCode)) {
                //商品订单的id为outTradeNo
                String outTradeNo = map.get("out_trade_no");
                HashMap param = new HashMap() {{
                   put("status", 2);
                   put("uuid", outTradeNo);
                }};
                int rows = amectService.updateStatus(param);
                //判断rows是否==1，修改成功
                if (rows == 1) {
                    //向前端页面推送付款结果
                    int userId = amectService.searchUserIdByUUID(outTradeNo);
                    WebSocketService.sendInfo("收款成功", userId+"");
                    //响应微信平台
                    response.setCharacterEncoding("utf-8");
                    //写入xml响应内容
                    response.setContentType("application/xml");
                    Writer writer = response.getWriter();
                    BufferedWriter bufferedWriter = new BufferedWriter(writer);
                    bufferedWriter.write("<xml><return_code><![CDATA[SUCCESS]]></return_code> <return_msg><![CDATA[OK]]></return_msg></xml>");
                    bufferedWriter.close();
                    writer.close();
                } else {//罚款记录！=1
                    response.sendError(500, "更新订单状态失败");
                }
            }
        } else {    //数字签名验证失败
            response.sendError(500, "数字签名异常");
        }
    }

    @PostMapping("/searchNativeAmectPayResult")
    @Operation(summary = "查询Native支付罚款订单的结果")
    @SaCheckLogin
    public R searchNativeAmectPayResult(@Valid @RequestBody SearchNativeAmectPayResultForm form) {
        int userId = StpUtil.getLoginIdAsInt();
        int amectId = form.getAmectId();
        HashMap param = new HashMap(){{
            put("amectId", amectId);
            put("userId", userId);
            put("status", 1);
        }};
        //把HashMap传给service方法
        amectService.searchNativeAmectPayResult(param);
        return R.ok();
    }
}
