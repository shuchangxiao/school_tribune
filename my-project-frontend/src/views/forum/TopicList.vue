<script setup>
import {Collection,Link,Edit,Clock} from "@element-plus/icons-vue"
import Card from "@/components/Card.vue";
import LightCard from "@/components/LightCard.vue";
import Weather from "@/components/Weather.vue";
import {computed, reactive,ref} from "vue";
import {get} from "@/net/index.js";
import {ElMessage} from "element-plus";
import TopicEditor from "@/components/TopicEditor.vue";
import {userStore} from "@/store/index.js";
import axios from "axios";

get("/api/forum/types",(data)=> {
  store.forum.types = data
})
const editor = ref(false)
const list = ref(null)
const store = userStore()
function updateList(){
  get("/api/forum/list-topic?page=0&type=0",data=>list.value=data)
}
updateList()
const today = computed(()=>{
  const data = new Date()
  return `${data.getFullYear()} 年 ${data.getMonth()+1} 月 ${data.getDate()} 日`
})
const weather = reactive({
  location:{},
  now:{},
  hourly:[],
  success:false
})
navigator.geolocation.getCurrentPosition((position)=>{
  const longitude = position.coords.longitude
  const latitude = position.coords.latitude
  get("/api/forum/weather?longitude="+longitude+"&latitude="+latitude,(data)=>{
    Object.assign(weather,data)
    weather.success=true
  })
},error=>{
  console.info(error)
  ElMessage.warning("位置信息获取失败")
  get("/api/forum/weather?longitude=116.40529&latitude=39.904",(data)=>{
    Object.assign(weather,data)
    weather.success=true
  })
},{
  timeout:3000,
  enableHighAccuracy:true
})
function success(){

}
</script>

<template>
  <div style="display: flex;margin: 20px auto;gap: 20px;max-width: 900px">
    <div style="flex: 1">
      <LightCard>
        <div class="edit-topic" @click="editor=true">
          <el-icon style="translate: 0 3px"><Edit/></el-icon>
          点击发表主题。。。
        </div>
      </LightCard>
      <div style="margin-top: 10px">
        <LightCard style="height: 30px">

        </LightCard>
      </div>
      <div style="margin-top: 10px" v-for="item in list">
        <LightCard class="topic-card">
          <div style="display: flex">
            <div>
              <el-avatar :size="30" :src="`${axios.defaults.baseURL}/images${item.avatar}`"/>
            </div>
            <div style="margin-left: 7px">
              <div style="font-size: 13px">{{item.username}}</div>
              <div  style="font-size: 12px;color: grey">
                <el-icon><Clock></Clock></el-icon>
                <span style="margin-left: 2px;display:inline-block;transform: translateY(-1.5px)">{{new Date(item.time).toLocaleString()}}</span>
              </div>
            </div>
          </div>
          <div>
            <div class="topic-type" :style="{color:store.findTypeById(item.type)?.color,'border-color':store.findTypeById(item.type)?.color+'77','background':store.findTypeById(item.type)?.color+'33'}"
                 >{{ store.findTypeById(item.type)?.name }}</div>
            <span style="font-weight: bold;margin-left: 7px">{{item.title}}</span>
          </div>
          <div class="topic-content">{{item.text}}</div>
          <div style="display: grid;grid-template-columns: repeat(3,1fr);grid-gap: 10px" >
            <el-image class="topic-image" v-for="img in item.image" :src="img" fit="cover"></el-image>
          </div>

        </LightCard>
      </div>
    </div>
    <div style="width: 280px">
      <div style="position: sticky;top:20px">
        <LightCard>
          <div style="font-weight: bold">
            <el-icon style="translate: 0 2px"><Collection/></el-icon>
            论坛公告
          </div>
          <el-divider style="margin:10px 0"/>
          <div style="font-size: 14px;margin: 10px;color: gray">
            为认真学习宣传贯彻党的二十大精神,深入贯彻习近平强军思想,
            作为迎接办学70周年系列学术活动之一,国防科技大学将于2022年11月24日至26日在长沙举办“国防科技
          </div>
        </LightCard>
        <LightCard style="margin-top: 20px">
          <div style="font-weight: bold">
            <el-icon style="translate: 2px"><Collection/></el-icon>
            天气信息
          </div>
          <el-divider style="margin:10px 0"/>
          <weather :data="weather"/>
        </LightCard>
        <LightCard style="margin-top: 10px">
          <div  class="info-text">
            <div>当前日期</div>
            <div>{{ today }}</div>
          </div>
          <div  class="info-text">
            <div>当前ip地址</div>
            <div>127.0.0.1</div>
          </div>
        </LightCard>
        <div style="font-size: 14px;margin-top: 10px;color: grey">
          <el-icon><Link/></el-icon>
          友情链接
          <el-divider style="margin: 10px 0"></el-divider>
        </div>
<!--        <div style="display: grid;grid-template-columns: repeat(2,1fr);grid-gap: 10px">-->
<!--          <div class="friend-link">-->
<!--            <el-image style="height: 100%"  src="	https://cn.vuejs.org/assets/sponsor-placement-1.mcLYTS-J.png"></el-image>-->
<!--          </div>-->
<!--          <div class="friend-link">-->
<!--            <el-image style="height: 100%" src="		https://cn.vuejs.org/images/partners/herodevs-hero.png"></el-image>-->
<!--          </div>-->
<!--        </div>-->
      </div>
    </div>
    <topic-editor :show="editor" @success="editor=false;updateList()" @close="editor=false"/>
  </div>
</template>

<style lang="less" scoped>
.topic-card{
  padding: 15px;
  transition: scale .3s;
  &:hover{
    scale: 1.05;
    cursor: pointer;
  }
  .topic-content{
    font-size: 13px;
    color:grey;
    margin-top: 5px;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 3;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .topic-type{
    display: inline-block;
    border:solid 0.5px grey;
    border-radius: 5px;
    font-size: 12px;
    padding: 0 5px;
  }
  .topic-image{
    width: 100%;
    height: 100%;
    border-radius: 5px;
    max-height: 110px;
  }
}
.info-text{
  display: flex;
  margin-top: 20px;
  justify-content: space-between;
  color: grey;
  font-size: 14px;
}
.friend-link{
  border-radius: 5px;
  overflow: hidden;
}
.edit-topic{
  background-color: #efefef;
  color:grey;
  border-radius: 5px;
  height: 40px;
  font-size: 14px;
  line-height: 40px;
  margin: 10px;
  &:hover{
    cursor: pointer;
  }
}
</style>