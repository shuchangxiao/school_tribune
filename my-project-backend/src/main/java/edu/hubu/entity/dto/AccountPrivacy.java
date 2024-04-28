package edu.hubu.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.hubu.entity.BaseData;
import lombok.Data;

@Data
@TableName("db_account_privacy")
public class AccountPrivacy implements BaseData {
    @TableId
    final Integer id;
    boolean phone = true;
    boolean email = true;
    boolean wechat = true;
    boolean qq = true;
    boolean gender = true;
}
