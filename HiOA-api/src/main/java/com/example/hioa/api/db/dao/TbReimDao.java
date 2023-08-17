package com.example.hioa.api.db.dao;

import com.example.hioa.api.db.pojo.TbReim;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbReimDao {
    public ArrayList<HashMap> searchReimByPage(HashMap param);
    public long searchReimCount(HashMap param);

    public int updateReimInstanceId(HashMap param);

    public int insert(TbReim reim);

    public HashMap searchReimById(HashMap param);

    public String searchInstanceIdById(int id);

    public int deleteReimById(HashMap param);
}
