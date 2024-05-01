package edu.hubu.controller;

import edu.hubu.entity.RestBean;
import edu.hubu.entity.vo.response.WeatherVO;
import edu.hubu.service.WeatherService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/forum")
public class ForumController {
    @Resource
    WeatherService weatherService;
    @GetMapping("/weather")
    public RestBean<WeatherVO> weather(double longitude,double latitude){
        WeatherVO vo = weatherService.fetchWeather(longitude, latitude);
        return vo==null
                ? RestBean.failure(400,"获取地理位置信息失败，请联系管理员")
                : RestBean.success(vo);
    }
}
