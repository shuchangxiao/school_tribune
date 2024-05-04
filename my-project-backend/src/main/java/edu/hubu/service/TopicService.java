package edu.hubu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.hubu.entity.dto.Topic;
import edu.hubu.entity.dto.TopicType;
import edu.hubu.entity.vo.request.TopicCreateVO;

import java.util.List;

public interface TopicService extends IService<Topic> {
    List<TopicType> topicType();
    String createTopic(int uid, TopicCreateVO vo);
}
