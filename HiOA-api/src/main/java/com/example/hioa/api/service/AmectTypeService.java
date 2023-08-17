package com.example.hioa.api.service;

import com.example.hioa.api.common.util.PageUtils;
import com.example.hioa.api.db.pojo.TbAmectType;

import java.util.ArrayList;
import java.util.HashMap;

public interface AmectTypeService {
    public ArrayList<TbAmectType> searchAllAmectType();

    public PageUtils searchAmectTypeByPage(HashMap param);

    public int insert(TbAmectType amectType);

    public HashMap searchById(int id);
    public int update(HashMap param);

    public int deleteAmectTypeByIds(Integer[] ids);
}
