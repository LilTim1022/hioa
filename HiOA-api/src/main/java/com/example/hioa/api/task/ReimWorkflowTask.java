package com.example.hioa.api.task;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.hioa.api.db.dao.TbReimDao;
import com.example.hioa.api.db.dao.TbUserDao;
import com.example.hioa.api.exception.HioaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Slf4j
public class ReimWorkflowTask {

    @Value("${workflow.url}")
    private String workflow;

    @Value("${hioa.recieveNotify}")
    private String recieveNotify;

    //定义dao引用
    @Autowired
    private TbUserDao userDao;

    @Autowired
    private TbReimDao reimDao;

    @Async("AsyncTaskExecutor")
    public void startReimWorkflow(int id, int creatorId) {
        HashMap info = userDao.searchUserInfo(creatorId);
        JSONObject json = new JSONObject();
        json.set("url", recieveNotify);
        json.set("creatorId", creatorId);
        json.set("creatorName", info.get("name").toString());
        json.set("title", info.get("dept").toString() + info.get("name").toString() + "的报销");
        Integer managerId = userDao.searchDeptManagerId(creatorId);
        json.set("managerId", managerId);
        Integer gmId = userDao.searchGmId();
        json.set("gmId", gmId);
        //发起http请求
        String url = workflow + "/workflow/startReimProcess";
        HttpResponse resp = HttpRequest.post(url).header("Content-Type","application/json")
                .body(json.toString()).execute();
        if (resp.getStatus() == 200) {
            //响应体转换为json对象
            json = JSONUtil.parseObj(resp.body());
            String instanceId = json.getStr("instanceId");
            HashMap param = new HashMap();
            param.put("id", id);
            param.put("instanceId", instanceId);
            int rows = reimDao.updateReimInstanceId(param);
            if (rows != 1) {
                throw new HioaException("保存报销申请工作流实例ID失败");
            }
        } else {
            log.error(resp.body());
        }
    }

    @Async("AsyncTaskExecutor")
    public void deleteReimWorkFlow(String instanceId, String type, String reason) {
        JSONObject json = new JSONObject();
        json.set("instanceId", instanceId);
        json.set("type", type);
        json.set("reason", reason);
        //发起http请求
        String url = workflow + "/workflow/deleteProcessById";
        HttpResponse resp = HttpRequest.post(url).header("Content-Type", "application/json")
                .body(json.toString()).execute();
        if (resp.getStatus() != 200) {
            log.error(resp.body());
            throw new HioaException("报销工作流删除失败");
        }
    }
}

