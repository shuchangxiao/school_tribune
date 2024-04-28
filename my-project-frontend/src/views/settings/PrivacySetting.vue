<script setup>

import Card from "@/components/Card.vue";
import {Lock, Setting} from "@element-plus/icons-vue"
import {reactive,ref} from "vue";
import {post,get} from "@/net/index.js";
import {ElMessage} from "element-plus";

const form = reactive({
  password:'',
  new_password:'',
  repeat_new_password:''
})
const validatePasswordRepeat = (rule,value,callback)=>{
  if(value===""){
    callback(new Error("请再此输入密码"))
  }else if(value !== form.new_password) {
    callback(new Error("两次输入密码不一致"))
  }else{
    callback()
  }
}
const rule = {
  password:[
    {required:true,message:"请输入原先密码",trigger:"blur"}
  ],
  new_password:[
    {required:true,message:"请输入新密码",trigger:"blur"},
    {min:6,max:6,message: "密码长度必须在6-16之间",trigger: ["blur"]}
  ],
  repeat_new_password:[
    {validator:validatePasswordRepeat,rigger:['blur','change']},
  ]
}
const formRef = ref()
const valid = ref(false)
const onValidate = (prop,isValid) =>{
  valid.value = isValid
}
function resetPassword(){
  formRef.value.validate(valid=>{
    if(valid){
      post("/api/user/change-password",{...form},()=>{
        ElMessage.success("修改密码成功")
        formRef.value.resetFields()
      })
    }
  })
}
const privacy = reactive({
  phone:false,
  wechat:false,
  qq:false,
  email:false,
  gender:false,
})
const  save = ref(true)
get("api/user/privacy",(data)=>{
  privacy.email = data.email
  privacy.qq = data.qq
  privacy.wechat = data.wechat
  privacy.gender = data.gender
  privacy.phone = data.phone
  save.value = false
})
function savePrivacy(type,status){
  save.value = true
  post('api/user/save-privacy',{
    type:type,
    status: status
  },()=>{
    ElMessage.success("隐私设置修改成功")
    save.value = false
  })
}
</script>

<template>
  <div>
    <div style="margin:auto;max-width: 600px">
      <div style="margin-top: 20px">
        <Card v-loading="save" :icon="Setting" title="隐私设置" desc="在这里你可以设置那些内容可以被其他人看到">
          <div class="checkbox-list">
            <el-checkbox @change="savePrivacy('phone',privacy.phone)" v-model="privacy.phone">公开展示我的手机号</el-checkbox>
            <el-checkbox @change="savePrivacy('email',privacy.email)" v-model="privacy.email">公开展示我的电子邮件</el-checkbox>
            <el-checkbox @change="savePrivacy('wechat',privacy.wechat)" v-model="privacy.wechat">公开展示我的微信号</el-checkbox>
            <el-checkbox @change="savePrivacy('qq',privacy.qq)" v-model="privacy.qq">公开展示我的qq号</el-checkbox>
            <el-checkbox @change="savePrivacy('gender',privacy.gender)" v-model="privacy.gender">公开展示我的性别</el-checkbox>
          </div>
        </Card>
        <Card style="margin-top: 20px" :icon="Setting" title="修改密码" desc="修改密码请在这里操作，请务必牢记你的密码">
          <el-form :rules="rule" :model="form" label-width="100px" ref="formRef" @validate="onValidate">
            <el-form-item label="当前密码" prop="password">
              <el-input type="password" v-model="form.password" :prefix-icon="Lock" placeholder="当前密码" maxlength="16"></el-input>
            </el-form-item>
            <el-form-item label="新密码" prop="new_password">
              <el-input  type="password" v-model="form.new_password" :prefix-icon="Lock" placeholder="输入新密码" maxlength="16"></el-input>
            </el-form-item>
            <el-form-item label="重复新密码" prop="repeat_new_password">
              <el-input  type="password" v-model="form.repeat_new_password" :prefix-icon="Lock" placeholder="重新输入新密码" maxlength="16"></el-input>
            </el-form-item>
          </el-form>
          <div  style="text-align: center">
            <el-button @click="resetPassword" type="success">立即重置密码</el-button>
          </div>
        </Card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.checkbox-list{
  margin:10px 0 0 10px;
  display: flex;
  flex-direction: column;
}
</style>