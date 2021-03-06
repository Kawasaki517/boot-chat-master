package cn.tycoding.service;

import cn.tycoding.entity.Message;
import cn.tycoding.entity.User;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.List;

/**
 * @author tycoding
 * @date 2019-06-14
 */
public interface ChatSessionService {

    /**
     * 推送消息，储存到Session中
     *
     * @param fromId  推送方ID
     * @param toId    接收方ID
     * @param message 消息
     * @param session HttpSession
     */
    void pushMessage(String fromId, String toId, Message message, HttpSession session);

    /**
     * 获取在线用户列表
     * @param session
     * @return
     */
    List<User> onlineList(HttpSession session);

    /**
     * 获取公共消息内容 -- 群组
     *
     * @param session HttpSession
     * @return
     */
    List<Message> commonList(HttpSession session);
    /**
     * 获取该用户与指定窗口的推送消息
     *
     * @param fromId  推送方ID
     * @param toId    接收方ID
     * @param session HttpSession
     * @return
     */
    List<Message> selfList(String fromId, String toId, HttpSession session) throws ParseException;
    /**
     * 上传文件
     * @param formId    发送消息ID
     * @param toId      接收消息ID
     * @param file      文件
     * @param message   附带消息信息
     *
     * */
    void Upload(String formId, String toId , MultipartFile file, String message,HttpSession session);
}
