package edu.hubu.service.Imp;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.hubu.entity.dto.AccountDto;
import edu.hubu.entity.vo.request.EmailRegisterVO;
import edu.hubu.mapper.AccountMapper;
import edu.hubu.service.AccountService;
import edu.hubu.utils.Const;
import edu.hubu.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Wrapper;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AccountServiceImp extends ServiceImpl<AccountMapper, AccountDto> implements AccountService, InitializingBean {
    @Resource
    AmqpTemplate amqpTemplate;
    @Resource
    RabbitTemplate rabbitTemplate;
    @Resource
    RedisTemplate redisTemplate;
    @Resource
    FlowUtils flowUtils;
    @Resource
    BCryptPasswordEncoder encoder;
    @Override
    public String registerEmailAccount(EmailRegisterVO vo) {
        String email = vo.getEmail();
        String code = (String) redisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA+email);
        if (code == null) return "请先获取验证码";
        if(!code.equals(vo.getCode())) return "验证码输入错误，请重新输入";
        if(this.existsAccountByEmail(email)) return "此电子邮箱已经被注册";
        if(this.existsAccountByUsername(vo.getUsername())) return "此用户名已经被注册";
        String password = encoder.encode(vo.getPassword());
        AccountDto accountDto = new AccountDto(null,vo.getUsername(),password, vo.getEmail(), "user",new Date());
        if(this.save(accountDto)) {
            redisTemplate.delete(Const.VERIFY_EMAIL_DATA+email);
            return null;
        }
        else return "内部出现错误，请联系管理员";

    }

    private boolean existsAccountByEmail(String email){
        return this.baseMapper.exists(Wrappers.<AccountDto>query().eq("email",email));
    }
    private boolean existsAccountByUsername(String username){
        return this.baseMapper.exists(Wrappers.<AccountDto>query().eq("username",username));
    }
    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        synchronized (ip.intern()){
            if(!this.verifyLimit(ip)) return "请求频繁，请稍后再试";
            Random random = new Random();
            int code = random.nextInt(899999)+100000;
            Map<String, Object> data = Map.of("type",type,"email",email,"code",code);
            amqpTemplate.convertAndSend("mail",data);
            redisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA + email,String.valueOf(code),5, TimeUnit.MINUTES);
            return null;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AccountDto accountDto = this.findAccountByNameOrEmail(username);
        if(accountDto ==null)
            throw new UsernameNotFoundException("用户名或密码错误");
//        return User
//                .withUsername(username)
//                .password(accountDto.getPassword())
//                .roles(accountDto.getRole())
//                .build();
        // 新写法
        MyUserDetail myUserDetail = new MyUserDetail();
        myUserDetail.setAccountDto(accountDto);
        return myUserDetail;
    }

    @Override
    public AccountDto findAccountByNameOrEmail(String text){
        return  this.query()
                .eq("username",text).or()
                .eq("email",text)
                .one();

    }
    private boolean verifyLimit(String ip){
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
        return flowUtils.limitOneCheck(key,60);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    }
}
