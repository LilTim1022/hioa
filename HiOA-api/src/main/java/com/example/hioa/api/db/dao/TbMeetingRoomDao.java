package com.example.hioa.api.db.dao;

import com.example.hioa.api.db.pojo.TbMeetingRoom;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbMeetingRoomDao {
    public ArrayList<HashMap> searchAllMeetingRoom();
    
    public HashMap searchById(int id);
    
    public ArrayList<String> searchFreeMeetingRoom(HashMap param);

    public ArrayList<HashMap> searchMeetingRoomByPage(HashMap param);

    public long searchMeetingRoomCount(HashMap param);

    public int insert(TbMeetingRoom meetingRoom);

    public int update(TbMeetingRoom meetingRoom);

    public boolean searchCanDelete(Integer[] ids);
    public int deleteMeetingRoomByIds(Integer[] ids);
}
