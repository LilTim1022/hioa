package com.example.hioa.workflow.db.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
@Mapper
public interface TbAmectTypeDao {
    public HashMap searchByType(String type);
}
