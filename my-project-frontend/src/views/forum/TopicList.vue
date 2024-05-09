<script setup>
import {Collection,Link,Edit,Clock,Document,Compass,Picture,Microphone} from "@element-plus/icons-vue"
import LightCard from "@/components/LightCard.vue";
import Weather from "@/components/Weather.vue";
import {computed, reactive,ref,watch} from "vue";
import {get} from "@/net/index.js";
import {ElMessage} from "element-plus";
import TopicEditor from "@/components/TopicEditor.vue";
import {userStore} from "@/store/index.js";
import axios from "axios";
import ColorDot from "@/components/ColorDot.vue";
import router from "@/router/index.js";
import TopicTag from "@/components/TopicTag.vue";

const editor = ref(false)
const topics = reactive({
  list:[],
  type:0,
  page:0,
  end:false,
  top:[]
})
const store = userStore()
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

get("/api/forum/top-topic",data=>{
  if(data) topics.top=data
})

function updateList(){
  if(topics.end) {
    return
  }
  get(`/api/forum/list-topic?page=${topics.page}&type=${topics.type}`, data=> {
    if(data != null) {
      data.forEach(d => topics.list.push(d))
      topics.page++
    }
    if(!data || data.length<10) topics.end =true
  })
}
function onTopicCreate(){
  editor.value=false
  reset()
  updateList()
}
function reset(){
  topics.page=0
  topics.end=false
  topics.list=[]
}

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
watch(()=>topics.type,()=> {
  reset()
  updateList()
},{immediate:true})
</script>

<template>
  <div style="display: flex;margin: 20px auto;gap: 20px;max-width: 900px">
    <div style="flex: 1">
      <LightCard>
        <div class="edit-topic" @click="editor=true">
          <el-icon style="translate: 0 3px"><Edit/></el-icon>
          点击发表主题。。。
        </div>
        <div style="margin-top: 10px;display: flex;font-size: 18px;color: grey;gap: 10px">
          <el-icon><Edit/></el-icon>
          <el-icon><Document/></el-icon>
          <el-icon><Compass/></el-icon>
          <el-icon><Picture/></el-icon>
          <el-icon><Microphone/></el-icon>
        </div>
      </LightCard>
      <LightCard style="margin-top: 10px;display: flex;flex-direction: column;gap: 10px">
        <div v-for="item in topics.top" class="top-topic" @click="router.push(`index/topic-detail/${item.id}`)">
          <el-tag type="info" size="small">置顶</el-tag>
          <div>{{item.title}}</div>
          <div>{{new Date(item.time).toLocaleString()}}</div>
        </div>
      </LightCard>
      <div style="margin-top: 10px;">
        <LightCard style="margin-top: 10px;display: flex;gap: 7px">
            <div :class="`type-select-card ${topics.type === item.id ? 'active' :''}`" v-for="item in store.forum.types" @click="topics.type=item.id">
              <color-dot :color="item.color"></color-dot>
              <span>{{item.name}}</span>
            </div>
        </LightCard>
      </div>
      <transition name="el-fade-in" mode="out-in">
        <div v-if="topics.list.length">
          <div style="margin-top: 10px;display: flex;flex-direction: column;gap: 10px" v-infinite-scroll="updateList">
            <LightCard class="topic-card" v-for="item in topics.list" @click="router.push('/index/topic-detail/'+item.id)">
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
                <TopicTag :type="item.type"/>
                <span style="font-weight: bold;margin-left: 7px">{{item.title}}</span>
              </div>
              <div class="topic-content">{{item.text}}</div>
              <div style="display: grid;grid-template-columns: repeat(3,1fr);grid-gap: 10px" >
                <el-image class="topic-image" v-for="img in item.image" :src="img" fit="cover"></el-image>
              </div>

            </LightCard>
          </div>
        </div>
      </transition>
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
    <topic-editor :show="editor" @success="onTopicCreate" @close="editor=false"/>
  </div>
</template>

<style lang="less" scoped>
.top-topic{
  display: flex;
  div:first-of-type{
    font-size: 14px;
    margin-left: 5px;
    font-weight: bold;
    opacity: 0.8;
    transition: color .3s;
    &:hover{
      color:grey;
    }
  }
  div:nth-of-type(2){
    flex: 1;
    color: grey;
    font-size: 13px;
    text-align: right;
  }
  &:hover{
    cursor: pointer;
  }
}
.type-select-card{
  background-color: #f5f5f5;
  padding: 2px 7px;
  font-size: 14px;
  border-radius: 5px;
  box-sizing: border-box;
  transition: background-color .3s;
  &.active{
    border:solid 1px #ead4c4;
  }
  &:hover{
    cursor: pointer;
    background-color: #dadada;
  }
}
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