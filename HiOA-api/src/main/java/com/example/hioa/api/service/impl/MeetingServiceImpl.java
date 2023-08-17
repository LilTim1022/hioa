package com.example.hioa.api.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.example.hioa.api.common.util.PageUtils;
import com.example.hioa.api.db.dao.TbMeetingDao;
import com.example.hioa.api.db.pojo.TbMeeting;
import com.example.hioa.api.exception.HioaException;
import com.example.hioa.api.service.MeetingService;
import com.example.hioa.api.task.MeetingWorkFlowTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
@Slf4j
public class MeetingServiceImpl implements MeetingService {
    @Autowired
    private TbMeetingDao meetingDao;

    @Autowired
    private MeetingWorkFlowTask meetingWorkflowTask;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public PageUtils searchOfflineMeetingByPage(HashMap param) {
        ArrayList<HashMap> list = meetingDao.searchOfflineMeetingByPage(param);
        long count = meetingDao.searchOfflineMeetingCount(param);
        //以下两种写法一样
        int start = (Integer)param.get("start");
        int length = MapUtil.getInt(param,"length");
        for(HashMap map : list) {
            String meeting = (String) map.get("meeting");
            if(meeting!=null&&meeting.length()>0) {
                map.replace("meeting", JSONUtil.parseArray(meeting));
            }
        }
        PageUtils pageUtils = new PageUtils(list, count, start, length);
        return pageUtils;
    }

    @Override
    public int insert(TbMeeting meeting) {
        int rows = meetingDao.insert(meeting);
        if (rows != 1) {
            throw new HioaException("会议添加失败");
        }
        meetingWorkflowTask.startMeetingWorkflow(meeting.getUuid(), meeting.getCreatorId(), meeting.getTitle(), meeting.getDate(),
                meeting.getStart() + ":00",meeting.getType() == 1 ?"线上会议" : "线下会议");
        return rows;
    }

    @Override
    public ArrayList<HashMap> searchOfflineMeetingInWeek(HashMap param) {
        ArrayList<HashMap> list = meetingDao.searchOfflineMeetingInWeek(param);
        return list;
    }

    @Override
    public HashMap searchMeetingInfo(short status, long id) {
        //判断正在进行中的会议
        HashMap map;
        //此处代码升级过，正在进行和已经结束的会议都可以查询present和unpresent字段
        if (status == 4||status==5) {
            map = meetingDao.searchCurrentMeetingInfo(id);
        } else {
            map = meetingDao.searchMeetingInfo(id);
        }
        return map;
    }

    @Override
    public int deleteMeetingApplication(HashMap param){
        Long id = MapUtil.getLong(param, "id");
        String uuid = MapUtil.getStr(param, "uuid");
        String instanceId = MapUtil.getStr(param, "instanceId");
        //查询会议详情，一会儿要判断是否距离会议开始不足20分钟
        HashMap meeting = meetingDao.searchMeetingById(param);
        String date = MapUtil.getStr(meeting, "date");
        String start = MapUtil.getStr(meeting, "start");
        int status = MapUtil.getInt(meeting, "status");
        boolean isCreator = Boolean.parseBoolean(MapUtil.getStr(meeting, "isCreator"));
        DateTime dateTime = DateUtil.parse(date + " " + start);
        DateTime now = DateUtil.date();
        //距离会议开始不足20分钟，不能删除会议
        if (now.isAfterOrEquals(dateTime.offset(DateField.MINUTE, -20))) {
            throw new HioaException("距离会议开始不足20分钟，不能删除会议");
        }
        //只能申请人删除该会议
        if (!isCreator) {
            throw new HioaException("只能申请人删除该会议");
        }
        //待审批和未开始的会议可以删除
        if (status == 1 || status == 3) {
            int rows = meetingDao.deleteMeetingApplication(param);
            if (rows == 1) {
                String reason = param.get("reason").toString();
                meetingWorkflowTask.deleteMeetingApplication(uuid, instanceId, reason);
            }
            return rows;
        } else {
            throw new HioaException("只能删除待审批和未开始的会议");
        }
    }
    @Override
    public PageUtils searchOnlineMeetingByPage(HashMap param) {
        ArrayList<HashMap> list = meetingDao.searchOnlineMeetingByPage(param);
        long count = meetingDao.searchOnlineMeetingCount(param);
        int start = (Integer) param.get("start");
        int length = (Integer) param.get("length");
        PageUtils pageUtils = new PageUtils(list, count, start, length);
        return pageUtils;
    }

    @Override
    public Long searchRoomIdByUUID(String uuid) {
        if(redisTemplate.hasKey(uuid)) {
            Object temp = redisTemplate.opsForValue().get(uuid);
            long roomId = Long.parseLong(temp.toString());
            return roomId;
        }
        return null;
    }

    @Override
    public ArrayList<HashMap> searchOnlineMeetingMembers(HashMap param) {
        ArrayList<HashMap> list = meetingDao.searchOnlineMeetingMembers(param);
        return list;
    }

    @Override
    public boolean searchCanCheckinMeeting(HashMap param) {
        long count = meetingDao.searchCanCheckinMeeting(param);
        return count == 1 ? true : false;
    }

    @Override
    public int updateMeetingPresent(HashMap param) {
        int rows = meetingDao.updateMeetingPresent(param);
        return rows;
    }

}
