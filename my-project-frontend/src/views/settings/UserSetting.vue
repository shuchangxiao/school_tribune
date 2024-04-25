<script setup>

import Card from "@/components/Card.vue";
import {Message, Notebook, User, Select, Refresh, EditPen} from "@element-plus/icons-vue"
import {userStore} from "@/store/index.js";
import {computed} from "vue";

const store = userStore()

const registerTime = computed(()=> new Date(store.user.registerTime).toLocaleString())
</script>

<template>
  <div style="display: flex">
    <div class="setting-left">
      <Card :icon="User" title="账号信息设置" desc="在这里编辑您的个人信息，您可以在隐私设置中选择是否展示这些内容">
        <el-form label-position="top" style="margin: 0 10px 10px 10px">
          <el-form-item label="用户名">
            <el-input/>
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group>
              <el-radio label="1">男</el-radio>
              <el-radio label="2">女</el-radio>
              <el-radio label="3">塑料袋</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="手机号">
            <el-input/>
          </el-form-item>
          <el-form-item label="QQ号">
            <el-input/>
          </el-form-item>
          <el-form-item label="微信号">
            <el-input/>
          </el-form-item>
          <el-form-item label="个人简介">
            <el-input type="textarea" :rows6/>
          </el-form-item>
          <div>
            <el-button :icon="Select" type="success">保存用户信息</el-button>
          </div>
        </el-form>
      </Card>
      <Card style="margin-top: 10px;" :icon="Message" title="电子邮件设置" desc="您可以在这里修改默认绑定的电子邮件">
        <el-form label-position="top" style="margin: 0 10px 10px 10px">
          <el-form-item label="个人邮件">
            <el-input/>
          </el-form-item>
          <el-form-item>
            <el-row style="width: 100%" gutter="10">
              <el-col :span="18">
                <el-input maxlength="6" type="text" placeholder="请输入验证码">
                  <template #prefix>
                    <el-icon><EditPen/></el-icon>
                  </template>
                </el-input>
              </el-col>
              <el-col :span="6">
                <el-button type="success" style="width: 100%">
                  获取验证码
                </el-button>
              </el-col>
            </el-row>
          </el-form-item>
          <div>
            <el-button :icon="Refresh" type="success">保存用户信息</el-button>
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