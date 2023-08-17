package com.example.hioa.api.websocket;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint(value = "/socket")
@Component
public class WebSocketService {
    //用concurrentHashMap充当全局缓存（全局静态）
    public static ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {

    }

    /* onclose函数涉及清理缓存中的session*/
    @OnClose
    public void onClose(Session session) {
        Map map = session.getUserProperties();
        if (map.containsKey("userId")) {
            String userId = MapUtil.getStr(map, "userId");
            sessionMap.remove(userId);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        JSONObject json = JSONUtil.parseObj(message);
        String opt = json.getStr("opt");
        //opt等于ping的话有轮询请求，避免触发webSocket超时
        if ("ping".equals(opt)) {
            return;
        }
        String token = json.getStr("token");
        String userId = StpUtil.stpLogic.getLoginIdByToken(token).toString();
        //给session绑定属性，绑定的属性是userId（原因：onClose可以取到session，根据userId把缓存删除）
        Map map = session.getUserProperties();
        //判断有无绑定userId属性，若无则绑定
        if (map.containsKey("userId")) {
            map.put("userId", userId);
        }
        //把session缓存，先判断缓存中是否有session,有则替换，无则生成
        if (sessionMap.containsKey(userId)) {
            sessionMap.replace(userId, session);
        } else {
            sessionMap.put(userId, session);
        }
        sendInfo("ok", userId);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误", error);
    }

    //不需要new对象，直接用静态的sendInfo方法,所以该方法调用的方法sendMessage也要是静态的。
    public static void sendInfo(String message, String userId) {
        //取出userId对应的session，把消息传给方法就可以发消息了
        //判断userId不为空，且缓存中存在该userId对应的session
        if (StrUtil.isNotBlank(userId) && sessionMap.containsKey(userId)) {
            Session session = sessionMap.get(userId);
            //发送消息
            sendMessage(message, session);
        }
    }

    //（+不想被外部访问，所以sendMessage是private）
    private static void sendMessage(String message, Session session) {
        try {
            //发送消息
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("执行异常", e);
        }
    }
}
