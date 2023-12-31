package com.example.hioa.api.service.impl;

import cn.hutool.core.map.MapUtil;
import com.example.hioa.api.common.util.PageUtils;
import com.example.hioa.api.db.dao.TbLeaveDao;
import com.example.hioa.api.db.pojo.TbLeave;
import com.example.hioa.api.exception.HioaException;
import com.example.hioa.api.service.LeaveService;
import com.example.hioa.api.task.LeaveWorkflowTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class LeaveServiceImpl implements LeaveService {
    @Autowired
    private TbLeaveDao leaveDao;

    @Autowired
    private LeaveWorkflowTask leaveWorkflowTask;

    @Override
    public PageUtils searchLeaveByPage(HashMap param) {
        ArrayList<HashMap> list = leaveDao.searchLeaveByPage(param);
        long count = leaveDao.searchLeaveCount(param);
        int start = (Integer) param.get("start");
        int length = (Integer) param.get("length");
        PageUtils pageUtils = new PageUtils(list, count, start, length);
        return pageUtils;
    }

    @Override
    public boolean searchContradiction(HashMap param) {
        long count = leaveDao.searchContradiction(param);
        boolean bool = count > 0;
        return bool;
    }

    @Override
    public int insert(TbLeave leave) {
        int rows = leaveDao.insert(leave);
        if (rows == 1) {
            //调用异步线程任务类
            leaveWorkflowTask.startLeaveWorkflow(leave.getId(), leave.getUserId(), leave.getDays());
        } else {
            throw new HioaException("会议添加失败");
        }
        return rows;
    }

    @Override
    public int deleteLeaveById(HashMap param) {
        //从hashmap获取请假记录的主键值
        int id = MapUtil.getInt(param, "id");
        String instanceId = leaveDao.searchInstanceIdById(id);
        int rows = leaveDao.deleteLeaveById(param);
        if (rows == 1) {
            //调用异步线程任务类
            leaveWorkflowTask.deleteLeaveWorkflow(instanceId, "员工请假", "删除请假申请");
        } else {
            throw new HioaException("删除请假申请失败");
        }
        return rows;
    }

    @Override
    public HashMap searchLeaveById(HashMap param) {
        HashMap map = leaveDao.searchLeaveById(param);
        return map;
    }
}
