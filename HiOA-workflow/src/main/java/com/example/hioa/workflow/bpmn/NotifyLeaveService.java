package com.example.hioa.workflow.bpmn;

import com.example.hioa.workflow.db.dao.TbLeaveDao;
import com.example.hioa.workflow.db.dao.TbUserDao;
import com.example.hioa.workflow.exception.HioaException;

import org.activiti.engine.HistoryService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class NotifyLeaveService implements JavaDelegate {
    @Autowired
    private HistoryService historyService;

    @Autowired
    private TbLeaveDao leaveDao;

    @Autowired
    private TbUserDao userDao;

//    @Autowired
//    private EmailTask emailTask;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        HistoricTaskInstance taskInstance = historyService.createHistoricTaskInstanceQuery().includeProcessVariables()
                .includeTaskLocalVariables().processInstanceId(delegateExecution.getProcessInstanceId())
                .orderByHistoricTaskInstanceEndTime().orderByTaskCreateTime().asc().list().get(0);
        String result = taskInstance.getTaskLocalVariables().get("result").toString();
        delegateExecution.setVariable("result", result);
        String instanceId = delegateExecution.getProcessInstanceId();
        
        HashMap param = new HashMap() {{
            put("status", "同意".equals(result) ? 3 : 2);
            put("instanceId", instanceId);
        }};

        int rows = leaveDao.updateLeaveStatus(param);
        if (rows != 1) {
            throw new HioaException("更新请假记录状态失败");
        }
        
    }
}
