package edu.hubu.controller;

import edu.hubu.entity.RestBean;
import edu.hubu.entity.dto.AccountDto;
import edu.hubu.entity.vo.response.AccountVO;
import edu.hubu.service.AccountService;
import edu.hubu.utils.Const;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class AccountController {
    @Resource
    AccountService accountService;

    @GetMapping("/info")
    public RestBean<AccountVO> info(@RequestAttribute(Const.ATTR_USER_ID) int id){
        AccountDto accountById = accountService.findAccountById(id);
        AccountVO viewObject = accountById.asViewObject(AccountVO.class);
        return RestBean.success(viewObject);
    }
}
