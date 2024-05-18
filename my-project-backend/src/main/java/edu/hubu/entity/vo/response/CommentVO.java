package edu.hubu.entity.vo.response;

import lombok.Data;

import java.util.Date;
@Data
public class CommentVO {
    Integer id;
    String content;
    Date time;
    String quote;
    User user;
    @Data
    public static class User{
        Integer id;
        String username;
        String avatar;
    }
}
