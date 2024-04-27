package edu.hubu.controller;

import edu.hubu.entity.RestBean;
import edu.hubu.entity.dto.AccountDetail;
import edu.hubu.entity.dto.AccountDto;
import edu.hubu.entity.vo.request.DetailSaveVO;
import edu.hubu.entity.vo.request.ModifyEmailVO;
import edu.hubu.entity.vo.response.AccountDetailVO;
import edu.hubu.entity.vo.response.AccountVO;
import edu.hubu.service.AccountDetailService;
import edu.hubu.service.AccountService;
import edu.hubu.utils.Const;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class AccountController {
    @Resource
    AccountService accountService;
    @Resource
    AccountDetailService accountDetailService;
    @GetMapping("/info")
    public RestBean<AccountVO> info(@RequestAttribute(Const.ATTR_USER_ID) int id){
        AccountDto accountById = accountService.findAccountById(id);
        AccountVO viewObject = accountById.asViewObject(AccountVO.class);
        return RestBean.success(viewObject);
    }
    @GetMapping("/detail")
    public RestBean<AccountDetailVO> detail(@RequestAttribute(Const.ATTR_USER_ID) int id){
        AccountDetail accountDetail = Optional
                .ofNullable(accountDetailService.findAccountDetailById(id))
                .orElseGet(AccountDetail::new);
        return RestBean.success(accountDetail.asViewObject(AccountDetailVO.class));
    }
    @PostMapping("/save-detail")
    public RestBean<Void> saveDetail(@RequestAttribute(Const.ATTR_USER_ID) int id, @RequestBody @Valid DetailSaveVO vo){
        boolean success = accountDetailService.saveAccountDetail(id,vo);
        return success ? RestBean.success(null):RestBean.failure(400,"用户名已被注册");
    }
    @PostMapping("/modify")
    public RestBean<Void> modifyEmail(@RequestAttribute(Const.ATTR_USER_ID) int id, @RequestBody @Valid ModifyEmailVO vo){
        String result = accountService.modifyEmail(id,vo);
        return result==null ? RestBean.success():RestBean.failure(400,result);
    }
}
