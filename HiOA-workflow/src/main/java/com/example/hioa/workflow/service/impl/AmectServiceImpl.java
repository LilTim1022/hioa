package com.example.hioa.workflow.service.impl;

import com.example.hioa.workflow.db.dao.TbAmectDao;
import com.example.hioa.workflow.db.pojo.TbAmect;
import com.example.hioa.workflow.service.AmectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AmectServiceImpl implements AmectService {
    @Autowired
    private TbAmectDao amectDao;

    @Override
    public int insert(TbAmect amect) {
        int rows = amectDao.insert(amect);
        return rows;
    }
}
