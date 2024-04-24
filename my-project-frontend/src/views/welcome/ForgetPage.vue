<script setup>

import {EditPen, Lock, Message, User} from "@element-plus/icons-vue";
import {computed, reactive, ref} from "vue";
import {get, post} from "@/net/index.js";
import {ElMessage} from "element-plus";
import router from "@/router";
const codeTime = ref(0)
const isEmailValid = computed(()=>/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(form.email))
const formRef1 = ref()
const formRef2 = ref()
const form = reactive({
  email:"",
  password:"",
  password_repeat:"",
  code:""
})
const active = ref(0)
const validatePasswordRepeat = (rule,value,callback)=>{
  if(value===""){
    callback(new Error("请再此输入密码"))
  }else if(value !== form.password) {
    callback(new Error("两次输入密码不一致"))
  }else{
    callback()
  }
}
const rule = {
  password:[
    {required:true,message:"请输入密码",trigger:['blur','change']},
    {min:6,max:16,message:"密码长度需在6-16个长度之间",trigger:['blur','change']}
  ],
  password_repeat:[
    {validator:validatePasswordRepeat,rigger:['blur','change']},
  ],
  email:[
    {required:true,message:"请输入邮箱",trigger:['blur','change']},
    {type:'email',message: "请输入合法的电子邮件",trigger: ['blur','change']}
  ],
  code:[
    {required:true,message:"请输入验证码",trigger:['blur','change']}
  ]
}
function askCode(){
  codeTime.value=60
  get(`api/auth/ask-code?email=${form.email}&type=reset`,()=>{
    ElMessage.success("验证码获取成功")
    const timerId = setInterval(()=> {
      codeTime.value--
      if (codeTime.value <= 0) {
        clearInterval(timerId);
      }
    },1000)
  },()=>{
    ElMessage.warning("验证码获取失败")
    codeTime.value=0
  })
}
function nextStep(){
  formRef1.value.validate((valid)=>{
    if(valid){
      active.value = 1
    }
  })
}
function reset(){
  formRef2.value.validate((valid)=>{
    if(valid){
      post('api/auth/reset-password',{...form},()=>{
        ElMessage.success("更改密码成功")
        router.push("/")
      })
    }
  })
}
</script>

<template>
  <div>
    <div style="margin: 30px 20px">
      <el-steps style="max-width: 600px" :active="active" finish-status="success" align-center>
        <el-step title="验证码发送" />
        <el-step title="重置密码" />
      </el-steps>
    </div>
    <div style="text-align: center;margin: 0 20px" v-if="active===0">
      <div style="margin-top: 100px">
        <div style="font-size: 25px;font-weight: bold">重置密码</div>
        <div style="font-size: 14px;color: grey;margin-top: 20px">在进入原神之前,请输入需要重置密码的电子邮件</div>
      </div>
      <div style="margin-top: 50px">
        <el-form :model="form" :rules="rule" ref="formRef1">
          <el-form-item prop="email">
            <el-input v-model="form.email" type="text" placeholder="请输入注册邮箱">
              <template #prefix>
                <el-icon><Message/></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="code">
            <el-row :gutter="10" style="width: 100%;">
              <el-col :span="17">
                <el-input v-model="form.code"  maxlength="6" type="text" placeholder="请输入验证码">
                  <template #prefix>
                    <el-icon><EditPen/></el-icon>
                  </template>
                </el-input>
              </el-col>
              <el-col :span="5">
                <el-button  @click="askCode" type="success" :disabled="!isEmailValid || codeTime">
                  {{codeTime>0?`请稍后在试${codeTime}秒`:"获取验证码"}}
                </el-button>
              </el-col>
            </el-row>
          </el-form-item>
        </el-form>
      </div>
      <div style="margin-top: 70px">
        <el-button @click="nextStep" type="danger" style="width: 270px">进入下一步</el-button>
      </div>
      <div style="margin-top:20px;text-align: center">
        <el-link @click="router.push('/')" style="font-size: 14px;color: grey">已有账号，立即登录</el-link>
      </div>
    </div>
    <div style="text-align: center;margin: 0 20px" v-if="active===1">
      <div style="margin-top: 100px">
        <div style="font-size: 25px;font-weight: bold">重置密码</div>
        <div style="font-size: 14px;color: grey;margin-top: 20px">在进入原神之前,重新输入密码</div>
        <div style="margin-top: 50px">
          <el-form :model="form" :rules="rule" ref="formRef2">
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" placeholder="请输入密码">
                <template #prefix>
                  <el-icon><Lock/></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="password_repeat">
              <el-input v-model="form.password_repeat" type="password" placeholder="请重新输入密码">
                <template #prefix>
                  <el-icon><Lock/></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-form>
        </div>
        <div style="margin-top: 70px">
          <el-button @click="reset" type="warning" style="width: 270px">立即重置密码</el-button>
        </div>
        <div style="margin-top:20px;">
          <el-link @click="active=0" style="font-size: 14px;color: grey">返回上一步</el-link>
        </div>
      </div>
    </div>
  </div>

</template>

<style scoped>

</style>