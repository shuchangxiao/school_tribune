package edu.hubu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.hubu.entity.dto.Topic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TopicMapper extends BaseMapper<Topic> {
//    @Select("""
//        select * from db_topic left join db_account on uid = db_account.id
//        order by `time` desc limit ${start} ,10
//        """)
//    List<Topic> topicList(int start);
//    @Select("""
//        select * from db_topic left join db_account on uid = db_account.id
//        where type = #{type}
//        order by `time` desc limit ${start} ,10
//        """)
//    List<Topic> topicListByType(int start,int type);
}
