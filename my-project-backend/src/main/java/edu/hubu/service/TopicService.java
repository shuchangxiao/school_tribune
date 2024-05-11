package edu.hubu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.hubu.entity.dto.Interact;
import edu.hubu.entity.dto.Topic;
import edu.hubu.entity.dto.TopicType;
import edu.hubu.entity.vo.request.TopicCreateVO;
import edu.hubu.entity.vo.response.TopicDetailVO;
import edu.hubu.entity.vo.response.TopicPreviewVO;
import edu.hubu.entity.vo.response.TopicTopVO;

import java.util.List;

public interface TopicService extends IService<Topic> {
    List<TopicType> topicType();
    String createTopic(int uid, TopicCreateVO vo);
    List<TopicPreviewVO> listTopicPreview(int page,int type);
    List<TopicTopVO> listTopTopics();
    TopicDetailVO getTopic(int tid);
    void interact(Interact interact,boolean state);
    List<TopicPreviewVO> listTopicCollects(int uid);
}
