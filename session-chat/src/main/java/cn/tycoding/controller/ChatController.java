package cn.tycoding.controller;

import cn.tycoding.entity.Message;
import cn.tycoding.entity.User;
import cn.tycoding.service.ChatSessionService;
import cn.tycoding.utils.R;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;

/**
 * @author tycoding
 * @date 2019-06-11
 */
@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatSessionService chatSessionService;

    /**
     * 获取当前窗口用户信息
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/{id}")
    public R info(@PathVariable("id") String id, HttpServletRequest request) {
        User user = new User();
        if (request.getSession().getAttribute(id) instanceof User) {
            user = (User) request.getSession().getAttribute(id);
        }
        return new R(user);
    }

    /**
     * 向指定窗口推送消息
     *
     * @param toId 接收方ID
     * @param message 消息
     * @return
     */
    @PostMapping("/push/{toId}")
    public R push(@PathVariable("toId") String toId, @RequestBody Message message, HttpServletRequest request) {
        try {
            WebsocketServerEndpoint endpoint = new WebsocketServerEndpoint();
            endpoint.sendTo(toId, message, request.getSession());
            return new R();
        } catch (Exception e) {
            e.printStackTrace();
            return new R(500, "消息推送失败");
        }
    }

    /**
     * 获取在线用户列表
     *
     * @param request 从Session中获取
     * @return
     */
    @GetMapping("/online/list")
    public R onlineList(HttpServletRequest request) {
        List<User> list = chatSessionService.onlineList(request.getSession());
        return new R(list);
    }

    /**
     * 获取公共聊天消息内容
     *
     * @param request 从Session中获取
     * @return
     */
    @GetMapping("/common")
    public R commonList(HttpServletRequest request) {
        List<Message> list = chatSessionService.commonList(request.getSession());
        return new R(list);
    }

    /**
     * 获取指定用户的聊天消息内容
     *
     * @param fromId  该用户ID
     * @param toId    哪个窗口
     * @param request 从Session中获取
     * @return
     */
    @GetMapping("/self/{fromId}/{toId}")
    public R selfList(@PathVariable("fromId") String fromId, @PathVariable("toId") String toId, HttpServletRequest request) throws ParseException {
        List<Message> list = chatSessionService.selfList(fromId, toId, request.getSession());
        return new R(list);
    }

    /**
     * 退出登录
     *
     * @param id      用户ID
     * @param request 从Session中剔除
     * @return
     */
    @DeleteMapping("/{id}")
    public R logout(@PathVariable("id") String id, HttpServletRequest request) {
        if (id != null) {
            request.getSession().removeAttribute(id);
        }
        return new R();
    }

    /**
     * 文件上传
     * */
    @RequestMapping(value = "/upload/{id}/{toId}",method = RequestMethod.POST)
    public R upload(@RequestParam MultipartFile file ,
                    @RequestParam String messages,
                    @PathVariable("id") String id,
                    @PathVariable("toId") String toId,
                    HttpSession session
    ) throws IOException {
        if (file!=null){
            chatSessionService.Upload(id,toId,file,messages,session);
            return new R();
        }
        else return new R(404,"未找到文件");
    }

    /**
     * @param id 发送方.
     * @Param toId 接收方.
     * */
    @RequestMapping(value = "/download/{id}/{toId}/{downloadName}",method = RequestMethod.GET)
    public R download(@PathVariable("id") String id,
                           @PathVariable("toId") String toId,
                           @PathVariable("downloadName") String downloadName,
                           HttpServletRequest request, HttpServletResponse response
                           ){
        //获取要下载的文件类型
        ServletContext servletContext = request.getServletContext();
        String mimeType = servletContext.getMimeType(downloadName);
        log.info("文件下载的类型：mimeType:"+mimeType);
        //通过响应头告诉客户端的文件类型
        response.setContentType(mimeType);
        //通过响应头告诉客户端说用于下载的
        //Content-Disposition响应头表示，收到的数据怎么处理
        //attachment表示附件，表示下载使用
        //download表示指定下载的文件名
        response.setHeader("Content-Disposition","attachment;filename="+downloadName);
        //读取要下载的文件内容
        try{
            InputStream inputStream=servletContext.getClassLoader().getResourceAsStream("Cache/"+id+"/"+toId+"/"+downloadName);
            ServletOutputStream outputStream = response.getOutputStream();
            if (inputStream==null)return new R(404,"为找到文件");
            IOUtils.copy(inputStream,outputStream);
        }catch (Exception e){
            System.out.println("下载出现错误");
        }

        return new R();
    }

}
