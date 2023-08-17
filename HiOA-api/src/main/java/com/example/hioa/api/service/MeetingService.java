package com.example.hioa.api.service;

import com.example.hioa.api.common.util.PageUtils;
import com.example.hioa.api.db.pojo.TbMeeting;

import java.util.ArrayList;
import java.util.HashMap;

public interface MeetingService {
    public PageUtils searchOfflineMeetingByPage(HashMap param);

    public int insert(TbMeeting meeting);

    public ArrayList<HashMap> searchOfflineMeetingInWeek(HashMap param);
    public HashMap searchMeetingInfo(short status, long id);

    public int deleteMeetingApplication(HashMap param);

    public PageUtils searchOnlineMeetingByPage(HashMap param);

    public Long searchRoomIdByUUID(String uuid);

    public ArrayList<HashMap> searchOnlineMeetingMembers(HashMap param);

    public boolean searchCanCheckinMeeting(HashMap param);
    public int updateMeetingPresent(HashMap param);
}
