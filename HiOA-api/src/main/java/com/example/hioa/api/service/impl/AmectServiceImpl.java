package com.example.hioa.api.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.example.hioa.api.common.util.PageUtils;
import com.example.hioa.api.db.dao.TbAmectDao;
import com.example.hioa.api.db.pojo.TbAmect;
import com.example.hioa.api.exception.HioaException;
import com.example.hioa.api.service.AmectService;
import com.example.hioa.api.wxpay.MyWXPayConfig;
import com.example.hioa.api.wxpay.WXPay;
import com.example.hioa.api.wxpay.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AmectServiceImpl implements AmectService {
    @Autowired
    private TbAmectDao amectDao;

    @Autowired
    private MyWXPayConfig myWXPayConfig;

    @Override
    public PageUtils searchAmectByPage(HashMap param) {
        ArrayList<HashMap> list = amectDao.searchAmectByPage(param);
        long count = amectDao.searchAmectCount(param);
        int start = (Integer) param.get("start");
        int length = (Integer) param.get("length");
        PageUtils pageUtils = new PageUtils(list, count, start, length);
        return pageUtils;
    }

    @Override
    @Transactional
    public int insert(ArrayList<TbAmect> list) {
        list.forEach(one -> {
            amectDao.insert(one);
        });
        return list.size();
    }

    @Override
    public HashMap searchById(int id) {
        HashMap map = amectDao.searchById(id);
        return map;
    }

    @Override
    public int update(HashMap param) {
        int rows = amectDao.update(param);
        return rows;
    }

    @Override
    public int deleteAmectByIds(Integer[] ids) {
        int rows = amectDao.deleteAmectByIds(ids);
        return rows;
    }

    @Override
    public String createNativeAmectPayOrder(HashMap param) {
        int userId = MapUtil.getInt(param, "userId");
        int amectId = MapUtil.getInt(param, "amectId");
        HashMap map = amectDao.searchAmectByCondition(param);
        if (map != null && map.size() > 0) {
            String amount = new BigDecimal(MapUtil.getStr(map, "amount")).multiply(new BigDecimal("100")).intValue()+"";
            try {
                WXPay wxPay = new WXPay(myWXPayConfig);
                param.clear();  //清理HashMap，放置创建订单需要的数据
                param.put("nonce_str", WXPayUtil.generateNonceStr());   //随机字符串
                param.put("body", "缴纳罚款");
                param.put("out_trade_no", MapUtil.getStr(map, "uuid"));
                param.put("total_fee", amount);
                param.put("spbill_create_ip", "127.0.0.1");
                param.put("notify_url", "https://c64a-31-205-117-88.ngrok-free.app/amect/recieveMessage");    //内网穿透地址+访问后端项目的路径
                param.put("trade_type", "NATIVE");
                String sign = WXPayUtil.generateSignature(param, myWXPayConfig.getKey());
                param.put("sign", sign);
                Map<String, String> result = wxPay.unifiedOrder(param);
                String prepayId = result.get("prepay_id");
                String codeUrl = result.get("code_url");
                //如果prepayId字段不为空，后端创建订单成功
                if (prepayId != null) {
                    param.clear();
                    param.put("prepayId", prepayId);
                    param.put("amectId", amectId);
                    int rows = amectDao.updatePrepayId(param);
                    if (rows != 1) {
                        throw new HioaException("更新罚款单的支付订单ID失败");
                    }
                    //生成base64的url，传给前端，前端才能生成二维码图片
                    QrConfig qrConfig = new QrConfig();
                    qrConfig.setWidth(255);
                    qrConfig.setHeight(255);
                    qrConfig.setMargin(2);
                    String qrCodeBase64 = QrCodeUtil.generateAsBase64(codeUrl, qrConfig, "jpg");
                    return qrCodeBase64;
                } else {
                    log.error("创建支付订单失败", result);
                    throw new HioaException("创建支付订单失败");
                }
            } catch (Exception e) {
                log.error("创建支付订单失败", e);
                throw new HioaException("创建支付订单失败");
            }
        } else {
            throw new HioaException("没有找到罚款单");
        }
    }

    @Override
    public int updateStatus(HashMap param) {
        int rows = amectDao.updateStatus(param);
        return rows;
    }

    @Override
    public int searchUserIdByUUID(String uuid) {
        int userId = amectDao.searchUserIdByUUID(uuid);
        return 0;
    }

    @Override
    public void searchNativeAmectPayResult(HashMap param) {
        //拿到罚款结果
        HashMap map = amectDao.searchAmectByCondition(param);
        if (MapUtil.isNotEmpty(map)) {
            String uuid = MapUtil.getStr(map, "uuid");
            param.clear();
            param.put("appid", myWXPayConfig.getAppID());
            param.put("mch_id", myWXPayConfig.getMchID());
            param.put("out_trade_no", uuid);
            param.put("nonce_str", WXPayUtil.generateNonceStr());
            try {
                //生成数字签名
                String sign = WXPayUtil.generateSignature(param, myWXPayConfig.getKey());
                param.put("sign", sign);
                //新建wxPay对象，需要拿到值注入数据
                WXPay wxPay = new WXPay(myWXPayConfig);
                //根据param查出wxPay的对象
                Map<String, String> result = wxPay.orderQuery(param);
                //判断状态码是否为SUCCESS
                String returnCode = result.get("return_code");
                String resultCode = result.get("result_code");
                if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode)) {
                    String tradeState = result.get("trade_state");
                    if ("SUCCESS".equals(returnCode)) {
                        amectDao.updateStatus(new HashMap(){{
                            put("uuid", uuid);
                            put("status", 2);   //修改状态为2状态，已付款的状态
                        }});
                    }
                }
            } catch (Exception e) {
                log.error("执行异常", e);
                throw new HioaException("执行异常");
            }
        }
    }
}
