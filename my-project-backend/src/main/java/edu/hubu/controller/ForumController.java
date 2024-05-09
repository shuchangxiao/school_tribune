package edu.hubu.controller;

import edu.hubu.entity.RestBean;
import edu.hubu.entity.dto.TopicType;
import edu.hubu.entity.vo.request.TopicCreateVO;
import edu.hubu.entity.vo.response.TopicPreviewVO;
import edu.hubu.entity.vo.response.TopicTopVO;
import edu.hubu.entity.vo.response.TopicTypeVO;
import edu.hubu.entity.vo.response.WeatherVO;
import edu.hubu.service.TopicService;
import edu.hubu.service.WeatherService;
import edu.hubu.utils.ControllerUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import edu.hubu.utils.Const;

@RestController
@RequestMapping("api/forum")
public class ForumController {
    @Resource
    WeatherService weatherService;
    @Resource
    TopicService topicService;
    @Resource
    ControllerUtils controllerUtils;
    @GetMapping("/weather")
    public RestBean<WeatherVO> weather(double longitude,double latitude){
        WeatherVO vo = weatherService.fetchWeather(longitude, latitude);
        return vo==null
                ? RestBean.failure(400,"获取地理位置信息失败，请联系管理员")
                : RestBean.success(vo);
    }
    @GetMapping("/types")
    public RestBean<List<TopicTypeVO>> topicType(){
        return RestBean.success(topicService.topicType()
                .stream()
                .map(type -> type.asViewObject(TopicTypeVO.class))
                .toList());
    }
    @PostMapping("/creat-topic")
    public RestBean<Void> createTopic(@Valid @RequestBody TopicCreateVO vo ,@RequestAttribute(Const.ATTR_USER_ID) int id){
        return controllerUtils.messageHandle(()->topicService.createTopic(id,vo));
    }
    @GetMapping("/list-topic")
    public RestBean<List<TopicPreviewVO>> listTopicPreview(@RequestParam @Min(0) @Max(100) int page, @RequestParam @Min(0) int type){
        return RestBean.success(topicService.listTopicPreview(page,type));
    }
    @GetMapping("/top-topic")
    public RestBean<List<TopicTopVO>> topTopic(){
        return RestBean.success(topicService.listTopTopics());
    }
}
