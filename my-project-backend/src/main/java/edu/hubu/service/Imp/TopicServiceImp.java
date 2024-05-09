package edu.hubu.service.Imp;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.util.BeanUtil;
import edu.hubu.entity.dto.Topic;
import edu.hubu.entity.dto.TopicType;
import edu.hubu.entity.vo.request.TopicCreateVO;
import edu.hubu.entity.vo.response.TopicPreviewVO;
import edu.hubu.entity.vo.response.TopicTopVO;
import edu.hubu.mapper.TopicMapper;
import edu.hubu.mapper.TopicTypeMapper;
import edu.hubu.service.TopicService;
import edu.hubu.utils.CacheUtils;
import edu.hubu.utils.Const;
import edu.hubu.utils.FlowUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TopicServiceImp extends ServiceImpl<TopicMapper, Topic> implements TopicService {
    @Resource
    TopicTypeMapper topicTypeMapper;
    @Resource
    FlowUtils flowUtils;
    @Resource
    CacheUtils cacheUtils;
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
        if(this.save((topic))) {
            cacheUtils.deleteCache(Const.FORUM_TOPIC_PREVIEW_CACHE + "*");
            return null;
        }
        else return "内部出现错误，请联系管理员";

    }

    @Override
    public List<TopicPreviewVO> listTopicPreview(int page, int type) {
        String key = Const.FORUM_TOPIC_PREVIEW_CACHE + page +":" + type;
        List<TopicPreviewVO> list = cacheUtils.takeListFromCache(key, TopicPreviewVO.class);
        if(list!=null) return list;
        List<TopicPreviewVO> previewVOList= null;
        List<Topic> topics;
        if(type == 0){
            topics = baseMapper.topicList(page *10);
        }else {
            topics = baseMapper.topicListByType(page *10,type);
        }
        if(topics.isEmpty()) return null;
        previewVOList = topics.stream().map(this::resolveToPreview).toList();
        cacheUtils.saveListToCache(key,previewVOList,60);
        return previewVOList;
    }

    @Override
    public List<TopicTopVO> listTopTopics() {
        List<Topic> topics = this.baseMapper.selectList(Wrappers
                .<Topic>query()
                .select("id","title","time")
                .eq("top",1));
        return topics.stream()
                .map(topic -> topic.asViewObject(TopicTopVO.class))
                .toList();
    }

    private TopicPreviewVO resolveToPreview(Topic topic){
        TopicPreviewVO vo = new TopicPreviewVO();
        BeanUtils.copyProperties(topic,vo);
        List<String> images = new ArrayList<>();
        StringBuilder previewText = new StringBuilder();
        JSONArray ops = JSONObject.parseObject(topic.getContent()).getJSONArray("ops");
        for (Object op : ops) {
            Object insert = JSONObject.from(op).get("insert");
            if(insert instanceof String text){
                if(previewText.length()>=300) continue;
                previewText.append(text);
            }else if(insert instanceof Map<?,?> map){
                Optional
                        .ofNullable(map.get("image"))
                        .ifPresent(object -> images.add(object.toString()));
            }
        }
        vo.setText(previewText.length()>300 ? previewText.substring(0,300) : previewText.substring(0,previewText.length()));
        vo.setImage(images);
        return vo;

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
