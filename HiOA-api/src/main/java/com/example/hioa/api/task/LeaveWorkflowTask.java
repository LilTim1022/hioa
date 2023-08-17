package com.example.hioa.api.task;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.hioa.api.db.dao.TbLeaveDao;
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
public class LeaveWorkflowTask {
    //定义若干值注入变量

    @Value("${workflow.url}")
    private String workflow;

    @Value("${hioa.recieveNotify}")
    private String recieveNotify;
    //定义dao引用变量
    @Autowired
    private TbUserDao userDao;

    @Autowired
    private TbLeaveDao leaveDao;

    @Async("AsyncTaskExecutor")
    public void startLeaveWorkflow(int id, int creatorId, String days) {
        HashMap info = userDao.searchUserInfo(creatorId);
        //用jsonobject或者hashmap都可以，但是object可以直接将json转换成字符串，不需要像hashmap一样借助工具类
        JSONObject json = new JSONObject();
        json.set("url", recieveNotify);
        json.set("creatorId", creatorId);
        json.set("creatorName", info.get("name").toString());
        json.set("title", info.get("dept").toString() + info.get("name").toString()+"的请假");
        //查部门经理，总经理的Id
        Integer managerId = userDao.searchDeptManagerId(creatorId);
        json.set("managerId", managerId);
        Integer gmId = userDao.searchGmId();
        json.set("gmId", gmId);
        //days转换为double浮点数传入
        json.set("days", Double.parseDouble(days));
        //完成jsonObj
        //发起Http请求，调用工作流项目的web方法
        String url = workflow + "/workflow/startLeaveProcess";  //方法路径
        //发起Http,保存好回应
        HttpResponse resp = HttpRequest.post(url).header("Content-Type", "application/json")
                .body(json.toString()).execute();
        if (resp.getStatus() == 200) {
            //对json重新赋值，拿到相应
            json = JSONUtil.parseObj(resp.body());
            String instanceId = json.getStr("instanceId");
            //执行update方法更新记录
            HashMap param = new HashMap();
            param.put("id", id);
            param.put("instanceId", instanceId);
            int rows = leaveDao.updateLeaveInstanceId(param);
            if (rows != 1) {
                throw new HioaException("保存请假工作流实例Id失败");
            }
        } else {
            log.error(resp.body());
        }
    }

    @Async("AsyncTaskExecutor")
    public void deleteLeaveWorkflow(String instanceId, String type, String reason) {
        //提交数据给工作流的web方法
        JSONObject json = new JSONObject();
        json.set("instanceId", instanceId);
        json.set("type", type);
        json.set("reason", reason);
        //规定web路径
        String url = workflow + "/workflow/deleteProcessById";  //方法路径
        //发起http请求
        HttpResponse resp = HttpRequest.post(url).header("Content-Type","application/json")
                .body(json.toString()).execute();
        if (resp.getStatus() != 200) {
            log.error(resp.body());
            throw new HioaException("请假工作流删除失败");
        }
    }
}
