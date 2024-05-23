<script setup>

import {logout} from '@/net/index.js'
import router from "@/router";
import {userStore} from "@/store/index.js";
import {ref,reactive} from "vue";
import {get} from "@/net/index.js";
import {
  Back,
  Message,
  Operation,
  Search,
  Location,
  ChatDotSquare,
  Bell,
  Notification,
  School,
  Umbrella,
  Check
} from '@element-plus/icons-vue';
import LightCard from "@/components/LightCard.vue";
import {ElMessage} from "element-plus";

const loading = ref(true)
const store = userStore()
const notification = ref([])
const searchInput = reactive({
  type:'1',
  text:''
})
get("/api/user/info",(data)=>{
  store.user = data
  loading.value = false
})
function userLogout(){
  logout(()=>router.push("/"))
}
const loadNotification = ()=>{
  get("/api/notification/list",(data)=>{
    notification.value = data
  })
}
function deleteAllNotification(){
  get("/api/notification/delete-all",()=>{
    ElMessage.success("删除所有消息成功")
    notification.value = []
  })
}
function confirmNotification(id,url){
  get(`/api/notification/delete?id=${id}`,()=>{
    loadNotification()
    window.open(url)
  })
}
loadNotification()
</script>

<template>
  <div class="main-content" v-loading="loading" element-loading-text="正在加载，请稍后...">
    <el-container style="height: 100%;" v-if="!loading">
      <el-header class="main-content-height">
        <el-image class="logo" src="https://www.itbaima.cn/image/logo/logo-light.png"></el-image>
        <div style="flex: 1;padding: 0 20px;text-align: center">
          <el-input v-model="searchInput.text" style="width: 100%;max-width: 500px;" placeholder="搜索论坛相关内容">
            <template #prefix>
              <el-icon><Search/></el-icon>
            </template>
            <template #append>
              <el-select style="width: 120px" v-model="searchInput.type">
                <el-option value="1" label="帖子广场"></el-option>
                <el-option value="2" label="失物招领"></el-option>
                <el-option value="3" label="校园活动"></el-option>
                <el-option value="4" label="表白墙"></el-option>
              </el-select>
            </template>
          </el-input>
        </div>
        <div class="user-info">
          <el-popover placement="bottom" :width="350" trigger="click">
            <template #reference>
              <el-badge  is-dot :hidden="!notification.length" style="margin-right: 14px">
                <div class="notification">
                  <el-icon><Bell/></el-icon>
                  <div>通知</div>
                </div>
              </el-badge>
            </template>
            <el-empty :image-size="80" description="暂时没有未读消息哦" v-if="!notification.length"/>
            <el-scrollbar :max-height="500" v-else>
              <light-card v-for="item in notification" class="notification-item" @click="confirmNotification(item.id,item.url)">
                <div>
                  <el-tag :type="item.type" size="small">消息</el-tag>
                  <span style="font-weight: bold">{{item.title}}</span>
                </div>
                <el-divider style="margin: 7px 0 3px 0"/>
                <div style="font-size: 13px">
                  {{item.content}}
                </div>
              </light-card>

            </el-scrollbar>
            <div style="margin-top: 10px">
              <el-button size="small" type="info" :icon="Check" @click="deleteAllNotification"
                         plain style="width: 100%">
                清除全部未读消息
              </el-button>
            </div>
          </el-popover>
          <div class="profile">
            <div>{{ store.user.username }}</div>
            <div>{{ store.user.email }}</div>
          </div>
          <el-dropdown>
            <el-avatar :src="store.avatarUrl"></el-avatar>
            <template #dropdown>
              <el-dropdown-item>
                <el-icon><Operation/></el-icon>
                个人设置
              </el-dropdown-item>
              <el-dropdown-item>
                <el-icon><Message/></el-icon>
                消息列表
              </el-dropdown-item>
              <el-dropdown-item @click="userLogout" divided>
                <el-icon><Back/></el-icon>
                退出登录
              </el-dropdown-item>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px">
          <el-scrollbar style="height: calc(100vh - 55px)">
            <el-menu router :default-active="$route.path" :default-openeds="['1','2','3']">
              <el-sub-menu index="1">
                <template #title>
                  <el-icon><Location/></el-icon>
                  <span>校园论坛</span>
                </template>
                <el-menu-item index="/index">
                  <template #title>
                    <el-icon><ChatDotSquare/></el-icon>
                    帖子广场
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon><Bell/></el-icon>
                    失物招领
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon><Notification/></el-icon>
                    校园活动
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon><Umbrella/></el-icon>
                    表白墙
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon><School/></el-icon>
                    海文考研
                    <el-tag style="margin-left: 5px;" size="small">合作机构</el-tag>
                  </template>
                </el-menu-item>
              </el-sub-menu>
              <el-sub-menu index="2">
                <template #title>
                  <el-icon><Location/></el-icon>
                  <span>探索与发现</span>
                </template>
                <el-menu-item>
                  <template #title>
                    <el-icon><ChatDotSquare/></el-icon>
                    成绩查询
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon><Bell/></el-icon>
                    班级课程表
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon><Notification/></el-icon>
                    教务通知
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon><Umbrella/></el-icon>
                    在线图书管
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon><School/></el-icon>
                    预约教室
                    <el-tag style="margin-left: 5px;" size="small">合作机构</el-tag>
                  </template>
                </el-menu-item>
              </el-sub-menu>
              <el-sub-menu index="3">
                <template #title>
                  <el-icon><Location/></el-icon>
                  <span>个人设置</span>
                </template>
                <el-menu-item index="/index/user-setting">
                  <template #title>
                    <el-icon><ChatDotSquare/></el-icon>
                    个人信息设置
                  </template>
                </el-menu-item>
                <el-menu-item index="/index/privacy-setting">
                  <template #title>
                    <el-icon><Bell/></el-icon>
                    账号安全设置
                  </template>
                </el-menu-item>
              </el-sub-menu>
            </el-menu>
          </el-scrollbar>
        </el-aside>
        <el-main class="main-content-page">
          <el-scrollbar style="height: calc(100vh - 55px)">
            <router-view  v-slot="{ Component }">
              <transition name="el-fade-in-linear" mode="out-in">
                <component :is="Component"/>
              </transition>
            </router-view>
          </el-scrollbar>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>


<style lang="less" scoped>
.notification-item{
  transition: .3s;
  &:hover{
    cursor: pointer;
    opacity: 0.8;
  }
}
.notification{
  font-size: 22px;
  line-height: 14px;
  text-align: center;
  transition: color 0.3s;
  &:hover{
    color: grey;
    cursor: pointer;
  }

}
.main-content{
  height: 100vh;
  width: 100vw;
}
.main-content-height{
  border-bottom: solid 1px var(--el-border-color);
  height: 55px;
  display: flex;
  align-items: center;
  box-sizing: border-box;
  .logo{
    height: 32px;
  }
  .user-info{
    display: flex;
    justify-content: flex-end;
    .el-avatar:hover{
      cursor: pointer;
    }

    .profile{
      text-align: right;
      margin-right: 20px;
      :first-child{
        font-size: 18px;
        font-weight:bold;
        line-height:20px;
      }
      :last-child{
        font-size: 12px;
        color: grey;
      }
    }
  }
}
.main-content-page{
  padding: 0;
  background-color: #f7f8fa;
}
//.dark .main-content-page{
//  background-color: #242627;
//}
</style>