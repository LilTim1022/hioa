package com.example.hioa.api.service;

import com.example.hioa.api.common.util.PageUtils;
import com.example.hioa.api.db.pojo.TbReim;

import java.util.HashMap;

public interface ReimService {
    public PageUtils searchReimByPage(HashMap param);

    public int insert(TbReim reim);

    public HashMap searchReimById(HashMap param);

    public int deleteReimById(HashMap param);
}
