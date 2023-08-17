package com.example.hioa.api.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.example.hioa.api.common.util.PageUtils;
import com.example.hioa.api.db.dao.TbReimDao;
import com.example.hioa.api.db.pojo.TbReim;
import com.example.hioa.api.exception.HioaException;
import com.example.hioa.api.service.ReimService;
import com.example.hioa.api.task.ReimWorkflowTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
@Slf4j
public class ReimServiceImpl implements ReimService {
    @Autowired
    private TbReimDao reimDao;

    @Autowired
    private ReimWorkflowTask reimWorkflowTask;

    @Override
    public PageUtils searchReimByPage(HashMap param) {
        //查询分页总数
        ArrayList<HashMap> list = reimDao.searchReimByPage(param);
        long count = reimDao.searchReimCount(param);
        int start =(Integer) param.get("start");
        int length =(Integer) param.get("length");
        //创建pageUtils对象，把数据封装在对象中
        PageUtils pageUtils = new PageUtils(list, count, start, length);
        return pageUtils;

    }

    @Override
    public int insert(TbReim reim) {
        int rows = reimDao.insert(reim);
        if (rows == 1) {
            reimWorkflowTask.startReimWorkflow(reim.getId(), reim.getUserId());
        } else {
            throw new HioaException("报销申请保存失败");
        }
        return rows;
    }

    @Override
    public HashMap searchReimById(HashMap param) {
        //查询结果保存在HashMap
        HashMap map = reimDao.searchReimById(param);
        String instanceId = MapUtil.getStr(map, "instanceId");
        //生成二维码
        QrConfig qrConfig = new QrConfig();
        //二维码分辨率和边框设置
        qrConfig.setWidth(70);
        qrConfig.setHeight(70);
        qrConfig.setMargin(2);
        //调用静态方法生成二维码图片并转换为base64字符串
        String qrCodeBase64 = QrCodeUtil.generateAsBase64(instanceId, qrConfig, "jpg");
        map.put("qrCodeBase64", qrCodeBase64);
        return map;
    }

    @Override
    public int deleteReimById(HashMap param) {
        int id = MapUtil.getInt(param, "id");
        String instanceId = reimDao.searchInstanceIdById(id);
        int rows = reimDao.deleteReimById(param);
        if (rows == 1) {
            reimWorkflowTask.deleteReimWorkFlow(instanceId,"报销申请", "删除报销申请");
        }
        return rows;
    }
}
