package edu.hubu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.hubu.entity.dto.Topic;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TopicMapper extends BaseMapper<Topic> {
}
