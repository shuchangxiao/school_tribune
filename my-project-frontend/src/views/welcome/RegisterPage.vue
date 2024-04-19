<script setup>
import {reactive,ref,computed} from "vue";
import {User,Lock,Message,EditPen} from "@element-plus/icons-vue";
import router from "@/router";
import {ElMessage} from "element-plus";
import {post,get} from "@/net/index.js";
const codeTime = ref(0)
const formRef = ref()
const form = reactive({
  username:'',
  password:'',
  password_repeat:'',
  email:'',
  code:''
})
const validateUsername = (rule,value,callback) =>{
  if(value===""){
    callback(new Error("请输入用户名"))
  }else if(!/^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(value)) {
    callback(new Error("用户名不能包含特殊符号"))
  }else{
    callback()
  }
}
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
  username:[
    {validator:validateUsername,trigger:['blur','change']},
    {min:4,max:10,message:"用户民的长度必须在4到10个长度直接",trigger:['blur','change']}
  ],
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
const isEmailValid = computed(()=>/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(form.email))
function askCode(){
  codeTime.value=60
  get(`api/auth/ask-code?email=${form.email}&type=register`,()=>{
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
function register(){
  formRef.value.validate((valid)=>{
    if(valid){
      post('api/auth/register',{...form},()=>{
        ElMessage.success("注册成功，欢迎加入我们")
        router.push("/")
      })
    }else {
      ElMessage.warning("请先填写注册表单")
    }
  })
}
</script>

<template>
  <div style="text-align: center;margin:0 20px">
    <div style="margin-top: 100px">
      <div style="font-size: 25px;font-weight: bold">注册成为新用户</div>
      <div style="margin-top:20px; font-size: 14px;color: grey">欢迎注册我们的平台，请在下方填写相关信息</div>
    </div>
    <div style="margin-top: 50px">
      <el-form :model="form" :rules="rule" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" maxlength="10" type="text" placeholder="请输入用户名">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
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
      <div style="margin-top: 50px">
        <el-button @click="register" type="warning" style="width: 270px">立即注册</el-button>
      </div>
      <div style="margin-top:20px">
        <el-link @click="router.push('/')" style="font-size: 14px;color: grey">已有账号，立即登录</el-link>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>