package com.example.hioa.workflow.service.impl;

import com.example.hioa.workflow.db.dao.TbLeaveDao;
import com.example.hioa.workflow.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class LeaveServiceImpl implements LeaveService {
    @Autowired
    private TbLeaveDao leaveDao;

    @Override
    public HashMap searchLeaveByInstanceId(String instanceId) {
        HashMap map = leaveDao.searchLeaveByInstanceId(instanceId);
        return map;
    }
}
