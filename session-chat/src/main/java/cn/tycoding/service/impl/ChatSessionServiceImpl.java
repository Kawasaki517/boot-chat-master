package cn.tycoding.service.impl;

import cn.tycoding.constant.CommonConstant;
import cn.tycoding.controller.WebsocketServerEndpoint;
import cn.tycoding.entity.Message;
import cn.tycoding.entity.User;
import cn.tycoding.service.ChatSessionService;
import cn.tycoding.utils.CoreUtil;
import cn.tycoding.utils.R;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

/**
 * @author tycoding
 * @date 2019-06-14
 */
@Slf4j
@Service
public class ChatSessionServiceImpl implements ChatSessionService {

    @Override
    public void pushMessage(String fromId, String toId, Message message, HttpSession session) {
        Object user = session.getAttribute(fromId);
        if (user instanceof User) {
            message.setTime(CoreUtil.format(new Date()));
            if (toId != null) {
                //查询接收方信息
                Object to = session.getAttribute(toId);
                if (to instanceof User) {
                    message.setTo((User)to);
                }
                //单个用户推送
                push(message, CommonConstant.CHAT_FROM_PREFIX + fromId + CommonConstant.CHAT_TO_PREFIX + toId, session);
            } else {
                //公共消息 -- 群组

                message.setTo(null);
                push(message, CommonConstant.CHAT_COMMON_PREFIX + fromId, session);
            }

        }
    }

    /**
     * 推送消息
     *
     * @param entity  Session value
     * @param key     Session key
     * @param session HttpSession
     */
    private void push(Message entity, String key, HttpSession session) {
        //这里按照 PREFIX_ID 格式，作为KEY储存消息记录
        //但一个用户可能推送很多消息，VALUE应该是数组
        List<Message> list = new ArrayList<>();
        Object msg = session.getAttribute(key);
        if (msg == null) {
            //第一次推送消息
            list.add(entity);
        } else {
            //第n次推送消息
            if (msg instanceof List) {
                list = (List<Message>) msg;
                list.add(entity);
            }
        }
        session.setAttribute(key, list);
    }

    @Override
    public List<User> onlineList(HttpSession session) {
        List<User> list = new ArrayList<>();
        Enumeration<String> ids = session.getAttributeNames();
        while (ids.hasMoreElements()) {
            String id = ids.nextElement();
            if (session.getAttribute(id) instanceof User) {
                list.add((User) session.getAttribute(id));
            }
        }
        return list;
    }

    @Override
    public List<Message> commonList(HttpSession session) {
        List<Message> list = new ArrayList<>();
        Enumeration<String> ids = session.getAttributeNames();
        while (ids.hasMoreElements()) {
            String id = ids.nextElement();
            //指定前缀标识
            if (id.startsWith(CommonConstant.CHAT_COMMON_PREFIX)) {
                Object attribute = session.getAttribute(id);
                if (attribute instanceof List) {
                    List<Message> data = (List<Message>) attribute;
                    list.addAll(data);
                    CoreUtil.push(list);
                }
            }
        }
        return list;
    }

    @Override
    public List<Message> selfList(String fromId, String toId, HttpSession session) throws ParseException {
        List<Message> list = new ArrayList<>();
        Enumeration<String> ids = session.getAttributeNames();
        while (ids.hasMoreElements()) {
            String id = ids.nextElement();
            //指定前缀标识
            if (id.equals(CommonConstant.CHAT_FROM_PREFIX+fromId+CommonConstant.CHAT_TO_PREFIX+toId) || id.equals(CommonConstant.CHAT_FROM_PREFIX+toId+CommonConstant.CHAT_TO_PREFIX+fromId)) {
                Object attribute = session.getAttribute(id);
                if (attribute instanceof List) {
                    list.addAll((List<Message>) attribute);
                }
            }
        }
        CoreUtil.push(list);
        return list;
    }

    @Override
    public void Upload(String formId, String toId, MultipartFile file, String messageBody,HttpSession session) {
        WebsocketServerEndpoint websocketServerEndpoint=new WebsocketServerEndpoint();
        JSONObject jsonObject = JSONObject.parseObject(messageBody);
        Message messages = JSON.toJavaObject(jsonObject, Message.class);
        File cache = new File("session-chat/src/main/resources/Cache/" + formId + "/" + toId);
        if (!cache.exists()) {
            System.out.println(cache.mkdirs());
        }
        try {
            file.transferTo(Paths.get(cache.getPath() + "/" + file.getOriginalFilename()));
            messages.setUrl("/Cache/"+formId+"/"+toId+"/"+file.getOriginalFilename());
            messages.setMessage(file.getOriginalFilename());
        } catch (Exception e) {
            log.info("解析文件出错");
        }
        if (Integer.parseInt(toId)==0){
            try {
                websocketServerEndpoint.onMessage(JSON.toJSONString(messages));
                log.info(JSON.toJSONString(messages));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            websocketServerEndpoint.sendTo(toId,messages,session);

        }

    }
}



