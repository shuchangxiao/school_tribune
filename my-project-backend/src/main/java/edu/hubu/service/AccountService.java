package edu.hubu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.hubu.entity.dto.AccountDto;
import edu.hubu.entity.vo.request.ConfirmResetVO;
import edu.hubu.entity.vo.request.EmailRegisterVO;
import edu.hubu.entity.vo.request.EmailResetVO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends IService<AccountDto>, UserDetailsService {
    AccountDto findAccountByNameOrEmail(String text);
    String registerEmailVerifyCode(String type,String email,String ip);
    String registerEmailAccount(EmailRegisterVO vo);
    String resetConfirm(ConfirmResetVO vo);
    String resetPassword(EmailResetVO vo);
}
