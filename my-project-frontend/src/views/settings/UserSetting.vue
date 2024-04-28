<script setup>

import Card from "@/components/Card.vue";
import {Message, Notebook, User, Select, Refresh, EditPen} from "@element-plus/icons-vue"
import {userStore} from "@/store/index.js";
import {computed, reactive,ref} from "vue";
import {post,get} from "@/net/index.js";
import {ElMessage} from "element-plus";

const isEmailValid = computed(()=>/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(emailForm.email))

const store = userStore()
const baseFormRef = ref()
const emailFormRef = ref()
const  desc = ref()
const codeTime = ref(0)
const registerTime = computed(()=> new Date(store.user.registerTime).toLocaleString())
const baseForm = reactive({
  username:'',
  gender:0,
  phone:'',
  qq:'',
  wechat:'',
  desc:''
})
const emailForm = reactive({
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
const rules = {
  username:[
    {validator:validateUsername,trigger:['blur','change']},
    {min:4,max:10,message:"用户民的长度必须在4到10个长度直接",trigger:['blur','change']}
  ],
  email:[
    {required:true,message:"请输入邮箱",trigger:['blur','change']},
    {type:'email',message: "请输入合法的电子邮件",trigger: ['blur','change']}
  ]
}
const loading = reactive({
  form:true,
  base:false

})
function saveDetails(){
  baseFormRef.value.validate(isValid =>{
    if(isValid){
      loading.base=true
      post("/api/user/save-detail", {...baseForm},()=>{
        ElMessage.success("用户名保存成功")
        store.user.username = baseForm.username
        desc.value = baseForm.desc
        loading.base=false
      },(message) =>{
        ElMessage.success(message)
        loading.base=false
      })
    }
  })
}
get("/api/user/detail",data=>{
  baseForm.username = store.user.username
  baseForm.gender = data.gender
  baseForm.phone = data.phone
  baseForm.wechat = data.wechat
  baseForm.qq = data.qq
  baseForm.desc = data.desc
  emailForm.email = store.user.email
  loading.form = false
})
function sendEmailCode(){
  codeTime.value = 60
    get(`/api/auth/ask-code?email=${emailForm.email}&type=modify`,()=>{
      ElMessage.success("验证码已经发送，请注意查收")
      const handle = setInterval(()=>{
        codeTime.value--
        if(codeTime.value===0){ clearInterval(handle)}

      },1000)
    },(message)=>{
      ElMessage.warning(message)
      codeTime.value = 0
    })
}
function modifyEmail(){
  emailFormRef.value.validate((isValid)=>{
    if(isValid){
      post("/api/user/modify",{...emailForm},()=>{
        ElMessage.success("邮件修改成功")
        store.user.email = emailForm.email
        emailForm.code = ''
      })
    }
  })
}
</script>

<template>
  <div style="display: flex;max-width: 950px;justify-content: space-between">
    <div class="setting-left">
      <Card :icon="User" title="账号信息设置" desc="在这里编辑您的个人信息，您可以在隐私设置中选择是否展示这些内容"  v-loading="loading.form">
        <el-form ref="baseFormRef" :rules="rules" :model="baseForm" label-position="top" style="margin: 0 10px 10px 10px">
          <el-form-item label="用户名" prop="username">
            <el-input maxlength="10" v-model="baseForm.username"/>
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="baseForm.gender">
              <el-radio :label="0">男</el-radio>
              <el-radio :label="1">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="手机号"  prop="phone">
            <el-input  v-model="baseForm.phone" maxlength="11"/>
          </el-form-item>
          <el-form-item  label="QQ号"  prop="qq">
            <el-input v-model="baseForm.qq" maxlength="11"/>
          </el-form-item>
          <el-form-item  label="微信号"  prop="wechat">
            <el-input  v-model="baseForm.wechat" maxlength="20"/>
          </el-form-item>
          <el-form-item  label="个人简介"  prop="desc">
            <el-input v-model="baseForm.desc" maxlength="200" type="textarea" :rows6/>
          </el-form-item>
          <div>
            <el-button :loading="loading.base" @click="saveDetails" :icon="Select" type="success">保存用户信息</el-button>
          </div>
        </el-form>
      </Card>
      <Card style="margin-top: 10px;" :icon="Message" title="电子邮件设置" desc="您可以在这里修改默认绑定的电子邮件">
        <el-form ref="emailFormRef"  :rules="rules" :model="emailForm" label-position="top" style="margin: 0 10px 10px 10px">
          <el-form-item label="个人邮件" prop="email">
            <el-input v-model="emailForm.email"/>
          </el-form-item>
          <el-form-item prop="code">
            <el-row style="width: 100%" gutter="10">
              <el-col :span="17">
                <el-input maxlength="6" type="text" placeholder="请输入验证码" v-model="emailForm.code">
                  <template #prefix>
                    <el-icon><EditPen/></el-icon>
                  </template>
                </el-input>
              </el-col>
              <el-col :span="6">
                <el-button  @click="sendEmailCode" type="success" :disabled="!isEmailValid || codeTime">
                  {{codeTime>0?`请稍后在试${codeTime}秒`:"获取验证码"}}
                </el-button>
              </el-col>
            </el-row>
          </el-form-item>
          <div>
            <div>
              <el-button  @click="modifyEmail" :icon="Select" type="success">保存用户信息</el-button>
            </div>
          </div>
        </el-form>
      </Card>
    </div>
    <div class="setting-right">
      <div style="position: sticky;top:20px">
        <Card>
          <div style="text-align: center;padding: 5px 15px 0 15px">
            <el-avatar :size="70" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"></el-avatar>
            <div style="font-weight: bold">你好，{{store.user.username}}</div>
          </div>
          <el-divider style="margin: 10px 0"></el-divider>
          <div style="font-size: 14px;color: grey;padding: 10px">
            {{baseForm.desc || "这个用户很懒，没有填写个人简介"}}
          </div>
        </Card>
        <Card>
          <div style="margin-top: 10px;font-size: 16px">
            账号注册时间:{{registerTime}}
          </div>
          <div style="font-size: 14px;color: grey">
            欢迎加入我们的学习论坛
          </div>
        </Card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.setting-left{
  flex: 1;
  margin: 20px;
}
.setting-right{
  width: 350px;
  margin: 20px;
}
</style>