package edu.hubu.service.Imp;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.util.BeanUtil;
import edu.hubu.entity.dto.Topic;
import edu.hubu.entity.dto.TopicType;
import edu.hubu.entity.vo.request.TopicCreateVO;
import edu.hubu.mapper.TopicMapper;
import edu.hubu.mapper.TopicTypeMapper;
import edu.hubu.service.TopicService;
import edu.hubu.utils.Const;
import edu.hubu.utils.FlowUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TopicServiceImp extends ServiceImpl<TopicMapper, Topic> implements TopicService {
    @Resource
    TopicTypeMapper topicTypeMapper;
    @Resource
    FlowUtils flowUtils;
    private  Set<Integer> types = null;
    @Override
    public List<TopicType> topicType() {
        return topicTypeMapper.selectList(null);
    }
    @PostConstruct
    private void init(){
        types = this.topicType().stream().map(TopicType::getId).collect(Collectors.toSet());
    }
    @Override
    public String createTopic(int uid, TopicCreateVO vo) {
        if(!textLimitCheck(vo.getContent())) return "文章内容太多，发文失败";
        if(!types.contains(vo.getType())) return "文章类型非法";
        String key = Const.FORUM_TOPIC_CREATE_COUNTER + uid;
        if(!flowUtils.limitPeriodCounterCheck(key,3,3600)) return "发文频繁，请稍后再试";
        Topic topic = new Topic();
        BeanUtils.copyProperties(vo,topic);
        topic.setContent(vo.getContent().toJSONString());
        topic.setUid(uid);
        topic.setTime(new Date());
        if(this.save((topic))) return null;
        else return "内部出现错误，请联系管理员";

    }
    private boolean textLimitCheck(JSONObject object){
        if(object == null) return false;
        long length = 0;
        for (Object ops : object.getJSONArray("ops")) {
            length += JSONObject.from(ops).getString("insert").length();
            if(length >2000) return false;
        }
        return true;

    }
}
