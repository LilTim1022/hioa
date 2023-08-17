package com.example.hioa.api.service;

import com.example.hioa.api.common.util.PageUtils;
import com.example.hioa.api.db.pojo.TbDept;

import java.util.ArrayList;
import java.util.HashMap;

public interface DeptService {
    public ArrayList<HashMap> searchAllDept();
    public HashMap searchById(int id);

    public PageUtils searchDeptByPage(HashMap param);

    public int insert(TbDept dept);

    public int update(TbDept dept);

    public int deleteDeptByIds(Integer[] ids);
}
