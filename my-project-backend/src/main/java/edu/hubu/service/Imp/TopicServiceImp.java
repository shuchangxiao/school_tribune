package edu.hubu.service.Imp;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.util.BeanUtil;
import edu.hubu.entity.dto.*;
import edu.hubu.entity.vo.request.TopicCreateVO;
import edu.hubu.entity.vo.response.TopicDetailVO;
import edu.hubu.entity.vo.response.TopicPreviewVO;
import edu.hubu.entity.vo.response.TopicTopVO;
import edu.hubu.mapper.*;
import edu.hubu.service.TopicService;
import edu.hubu.utils.CacheUtils;
import edu.hubu.utils.Const;
import edu.hubu.utils.FlowUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class TopicServiceImp extends ServiceImpl<TopicMapper, Topic> implements TopicService {
    @Resource
    TopicTypeMapper topicTypeMapper;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    FlowUtils flowUtils;
    @Resource
    CacheUtils cacheUtils;
    @Resource
    AccountMapper accountMapper;
    @Resource
    AccountDetailMapper accountDetailMapper;
    @Resource
    AccountPrivacyMapper accountPrivacyMapper;
    private  Set<Integer> types = null;

    @Override
    public List<TopicPreviewVO> listTopicCollects(int uid) {
        List<Integer> collect = baseMapper.getCollect(uid);
        List<Topic> topics = this.baseMapper.selectBatchIds(collect);
        if(topics.isEmpty()) return null;
        return topics.stream().map(topic -> {
            TopicPreviewVO previewVO = new TopicPreviewVO();
            BeanUtils.copyProperties(topic,previewVO);
            return previewVO;
        }).toList();

    }

    @Override
    public void interact(Interact interact, boolean state) {
        String type = interact.getType();
        synchronized (type.intern()){
            stringRedisTemplate.opsForHash().put(type,interact.toKey(),Boolean.toString(state));
            this.saveInteractSchedule(type);
        }
    }
    ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    private final Map<String,Boolean> state = new HashMap<>();
    private void saveInteractSchedule(String type){
        if (!state.getOrDefault(type,false)) {
            state.put(type,true);
            service.schedule(()->{
                this.saveInteract(type);
                state.put(type,false);
            },3, TimeUnit.SECONDS);
        }
    }

    private void saveInteract(String type){
        synchronized (type.intern()){
            List<Interact> check = new LinkedList<>();
            List<Interact> uncheck = new LinkedList<>();
            stringRedisTemplate.opsForHash().entries(type).forEach((k,v)->{
                if(Boolean.parseBoolean(v.toString())){
                    check.add(Interact.parseInteract(k.toString(),type));
                }else {
                    uncheck.add(Interact.parseInteract(k.toString(),type));
                }
            });
            if(!check.isEmpty()) baseMapper.addInteract(check,type);
            if(!uncheck.isEmpty()) baseMapper.deleteInteract(uncheck,type);
            stringRedisTemplate.delete(type);
        }
    }

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
    public List<TopicPreviewVO> listTopicPreview(int pageNumber, int type) {
        String key = Const.FORUM_TOPIC_PREVIEW_CACHE + pageNumber +":" + type;
        List<TopicPreviewVO> list = cacheUtils.takeListFromCache(key, TopicPreviewVO.class);
        if(list!=null) return list;
        List<TopicPreviewVO> previewVOList= null;
        Page<Topic> page = Page.of(pageNumber,10);
        if(type == 0){
            baseMapper.selectPage(page,Wrappers.<Topic>query().orderByDesc("time"));
        }else {
            baseMapper.selectPage(page,Wrappers.<Topic>query().eq("type",type).orderByDesc("time"));
        }
        List<Topic> topics = page.getRecords();
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

    @Override
    public TopicDetailVO getTopic(int tid) {
        Topic topic = baseMapper.selectById(tid);
        if(topic == null) return null;
        TopicDetailVO vo = new TopicDetailVO();
        TopicDetailVO.Interact interact = new TopicDetailVO.Interact(
                hasInteract(tid,topic.getUid(),"like"),
                hasInteract(tid,topic.getUid(),"collect")
        );
        vo.setInteract(interact);
        BeanUtils.copyProperties(topic,vo);
        TopicDetailVO.User user = new TopicDetailVO.User();
        vo.setUser(this.fillUserDetailsByPrivacy(user,topic.getUid()));
        return vo;
    }
    private boolean hasInteract(int tid,int uid,String type){
        String key = tid + ":" + uid;
        if(stringRedisTemplate.opsForHash().hasKey(type,key)){
            return Boolean.parseBoolean(Objects.requireNonNull(stringRedisTemplate.opsForHash().get(type, key)).toString());
        }
        return baseMapper.userInteractCount(tid,uid,type) > 0;
    }
    private <T> T fillUserDetailsByPrivacy(T target,int uid){
        AccountDetail accountDetail = accountDetailMapper.selectById(uid);
        AccountDto accountDto = accountMapper.selectById(uid);
        AccountPrivacy accountPrivacy = accountPrivacyMapper.selectById(uid);
        String[] ignores = accountPrivacy.hiddenFields();
        BeanUtils.copyProperties(accountDto,target,ignores);
        BeanUtils.copyProperties(accountDetail,target,ignores);
        return target;
    }
    private TopicPreviewVO resolveToPreview(Topic topic){
        TopicPreviewVO vo = new TopicPreviewVO();
        BeanUtils.copyProperties(accountMapper.selectOne(Wrappers.<AccountDto>query().eq("id",topic.getUid())),vo);
        BeanUtils.copyProperties(topic,vo);
        vo.setLike(baseMapper.interactCount(topic.getId(),"like"));
        vo.setCollect(baseMapper.interactCount(topic.getId(),"collect"));
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
