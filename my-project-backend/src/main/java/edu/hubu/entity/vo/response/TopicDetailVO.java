package edu.hubu.entity.vo.response;

import lombok.Data;

import java.util.Date;

@Data
public class TopicDetailVO {
    Integer id;
    String title;
    String content;
    Integer type;
    Date time;
    User user;
    @Data
    public static class User{
        Integer id;
        String username;
        String avatar;
        String desc;
        Integer gender;
        String phone;
        String qq;
        String wechat;
        String email;
    }
}
