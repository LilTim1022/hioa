package com.example.hioa.workflow.service.impl;

import com.example.hioa.workflow.db.dao.TbReimDao;
import com.example.hioa.workflow.service.ReimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class ReimServiceImpl implements ReimService {

    @Autowired
    private TbReimDao reimDao;

    @Override
    public HashMap searchReimByInstanceId(String instanceId) {
        HashMap map = reimDao.searchReimByInstanceId(instanceId);
        return map;
    }
}
