package com.example.hioa.api.db.dao;

import com.example.hioa.api.db.pojo.TbAmect;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbAmectDao {
    public ArrayList<HashMap> searchAmectByPage(HashMap param);

    public long searchAmectCount(HashMap param);

    public int insert(TbAmect amect);

    public HashMap searchById(int id);
    public int update(HashMap param);

    public int deleteAmectByIds(Integer[] ids);

    public HashMap searchAmectByCondition(HashMap param);
    public int updatePrepayId(HashMap param);

    public int updateStatus(HashMap param);

    public int searchUserIdByUUID(String uuid);
}
