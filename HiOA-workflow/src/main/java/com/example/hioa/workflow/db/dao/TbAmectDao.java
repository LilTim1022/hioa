package com.example.hioa.workflow.db.dao;

import com.example.hioa.workflow.db.pojo.TbAmect;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TbAmectDao {
    public int insert(TbAmect amect);
}
