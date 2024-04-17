package edu.hubu.service.Imp;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.hubu.entity.dto.AccountDto;
import edu.hubu.mapper.AccountMapper;
import edu.hubu.service.AccountService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImp extends ServiceImpl<AccountMapper, AccountDto> implements AccountService {
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
}
