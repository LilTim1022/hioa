package com.example.hioa.workflow.db.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;

@Mapper
public interface TbReimDao {
    public HashMap searchReimByInstanceId(String instanceId);

    public int updateReimStatus(HashMap param);
}
