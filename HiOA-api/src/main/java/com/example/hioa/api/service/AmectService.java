package com.example.hioa.api.service;

import com.example.hioa.api.common.util.PageUtils;
import com.example.hioa.api.db.pojo.TbAmect;

import java.util.ArrayList;
import java.util.HashMap;

public interface AmectService {
    public PageUtils searchAmectByPage(HashMap param);

    public int insert(ArrayList<TbAmect> list);

    public HashMap searchById(int id);
    public int update(HashMap param);

    public int deleteAmectByIds(Integer[] ids);

    public String createNativeAmectPayOrder(HashMap param);

    public int updateStatus(HashMap param);

    public int searchUserIdByUUID(String uuid);

    public void searchNativeAmectPayResult(HashMap param);
}
