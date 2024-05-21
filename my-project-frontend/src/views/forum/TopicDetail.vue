<script setup>
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import {useRoute, useRouter} from "vue-router";
import {accessHeader, get, post} from "@/net/index.js";
import axios from "axios";
import {reactive,computed,ref} from "vue";
import {
  Plus,
  Female,
  Male,
  ArrowLeft,
  CircleCheck,
  Star,
  EditPen,
  ChatDotSquare,
  Delete,
  ChatSquare
} from "@element-plus/icons-vue"
import {QuillDeltaToHtmlConverter} from 'quill-delta-to-html'
import Card from "@/components/Card.vue";
import TopicTag from "@/components/TopicTag.vue";
import InteractButton from "@/components/InteractButton.vue";
import {ElMessage} from "element-plus";
import {userStore} from "@/store/index.js";
import TopicEditor from "@/components/TopicEditor.vue";
import TopicCommentEditor from "@/components/TopicCommentEditor.vue";
import LightCard from "@/components/LightCard.vue";
import {QuillEditor} from "@vueup/vue-quill";
import {QuillWatch} from "quill-image-super-solution-module";
import CommentCard from "@/components/CommentCard.vue";


const store = userStore()
const route = useRoute()
const router = useRouter()
const topic = reactive({
  data:null,
  like:false,
  collect:false,
  comments:null,
  page:1
})
let checkComment = ref(false)

const comment = reactive({
  show:false,
  content:'',
  quote:-1,
  uploading:false
})
const tid = route.params.tid
const contentLength= computed(()=>deltaToText(comment.content).length)
function deltaToText(delta){
  if(!delta.ops) return ""
  let str = ""
  for (let op of delta.ops) {
    str += op.insert
  }
  return str.replace(/\s/g,"")
}
function convertToHtml(content){
  const ops = JSON.parse(content).ops
  const converter = new QuillDeltaToHtmlConverter(ops,{inlineStyles:true})
  return converter.convert()
}
const edit = ref(false)
const init = () => get(`api/forum/topic?tid=${tid}`,data=>{
  topic.data=data
  topic.like = data.interact.like
  topic.collect = data.interact.collect
  loadComment(1)
})
init()
function interact(type,message){
  get(`/api/forum/interact?tid=${tid}&type=${type}&state=${!topic[type]}`,()=>{
    topic[type] = !topic[type]
    if(topic[type]) ElMessage.success(`${message}成功`)
    else  ElMessage.success(`已取消${message}`)
  })
}
function updateTopic(editor){
  post("/api/forum/update-topic",{
    id:tid,
    type: editor.type,
    title: editor.title,
    content: editor.text
  },()=> {
    ElMessage.success("帖子内容跟新成功")
    edit.value = false
  })
}
function loadComment(page){
  topic.comments = null;
  topic.page = page
  get(`api/forum/comments?tid=${tid}&page=${page-1}`,data=>{
    topic.comments = data
  })
}
function submit(){
  post("/api/forum/add-comment",{
    tid:tid,
    quote: comment.quote,
    content: JSON.stringify(comment.content)
  },()=>{
    ElMessage.success("发表评论成功")
    topic.page = Math.floor(++topic.data.comments/10)+1
    comment.content = null
    loadComment(topic.page)
  })
}
const avatarURL = computed(()=>{
  if(topic.data.user.avatar !== null){
    return axios.defaults.baseURL + '/images' + topic.data.user.avatar
  }
  return "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"

})
const editorOptions = {
  modules:{
    toolbar:{
      container:["link", "image",],
      handlers:{
        'image':function (){
          QuillWatch.emit(this.quill.id)
        }
      }
    },
    imageResize:{
      modules:['Resize',"DisplaySize"]
    },
    ImageExtend: {
      action:  axios.defaults.baseURL + '/api/image/cache',
      name: 'file',
      size: 5,
      loading: true,
      accept: 'image/png, image/jpeg',
      response: (resp) => {
        if(resp.data) {
          return axios.defaults.baseURL + '/images' + resp.data
        } else {
          return null
        }
      },
      methods: 'POST',
      headers: xhr => {
        xhr.setRequestHeader('Authorization', accessHeader().Authorization);
      },
      start: () => comment.uploading = true,
      success: () => {
        ElMessage.success('图片上传成功!')
        comment.uploading = false
      },
      error: () => {
        ElMessage.warning('图片上传失败，请联系管理员!')
        comment.uploading = false
      }
    }
  }
}
</script>

<template>
  <div class="topic-page" v-if="topic.data">
    <div class="topic-main" style="position: sticky;top:0;z-index: 999">
      <card  style="display: flex;width: 100%">
        <el-button plain round @click="router.push('/index')" :icon="ArrowLeft" type="info" size="small">返回列表</el-button>
        <div style="flex: 1;text-align: center">
          <TopicTag :type="topic.data.type"/>
          <span style="font-weight: bold;margin-left: 10px">{{topic.data.title}}</span>
        </div>
      </card>
    </div>
    <div class="topic-main">
      <div class="topic-main-left">
          <el-avatar :src="avatarURL" :size="60"></el-avatar>
          <div>
            <div style="font-size: 18px;font-weight: bold">
              {{topic.data.user.username}}
              <span style="color: hotpink;translate: 0 -2px" v-if="topic.data.user.gender === 1">
                <el-icon><Female/></el-icon>
              </span>
              <span style="color: dodgerblue" v-if="topic.data.user.gender === 0">
                <el-icon><Male/></el-icon>
              </span>
              <div class="desc">{{topic.data.user.email}}</div>
            </div>
            <el-divider style="margin: 10px 0"/>
            <div>
              <div style="text-align: left">
                <div class="desc">QQ号：{{topic.data.user.qq || '已隐藏或未填写'}}</div>
                <div class="desc">微信号：{{topic.data.user.wechat || '已隐藏或未填写'}}</div>
                <div class="desc">手机号：{{topic.data.user.phone || '已隐藏或未填写'}}</div>
              </div>
            </div>
            <el-divider style="margin: 10px 0"/>
            <div class="desc" style="margin: 10px 0;text-align: left">{{topic.data.user.desc || '这个人很懒，没有任何简介'}}</div>
          </div>
      </div>
      <div class="topic-main-right">
        <div class="topic-content" v-html="convertToHtml(topic.data.content)"></div>
        <el-divider></el-divider>
        <div style="font-size: 13px;color: grey;text-align: center">
          <div>发帖日期：{{new Date(topic.data.time).toLocaleString()}}</div>
        </div>
        <div style="text-align: right;margin-top: 30px">
          <interact-button name="评论" color="grey"
                           @click="checkComment = checkComment !== true;" style="margin-right: 30px">
            <el-icon style="transform: translateY(2px)"><ChatDotSquare /></el-icon>
          </interact-button>
          <interact-button name="编辑" color="dodgerblue"
                           @click="edit=true" style="margin-right: 30px" v-if="store.user.id===topic.data.user.id">
            <el-icon style="transform: translateY(2px)"><EditPen/></el-icon>
          </interact-button>
          <interact-button check-name="已点赞" name="点个赞吧" color="pink" :check="topic.like"
                           @click="interact('like','点赞')">
            <el-icon style="transform: translateY(2px)"><CircleCheck/></el-icon>
          </interact-button>
          <interact-button check-name="已收藏" name="收藏一下吧" style="margin-left: 20px"  color="orange"  :check="topic.collect"
                            @click="interact('collect','收藏')">
            <el-icon style="transform: translateY(2px)"><Star/></el-icon>
          </interact-button>
        </div>
      </div>
    </div>
    <div v-if="checkComment">
      <LightCard style="width: 800px;margin: 10px auto;text-align: left">
        <div style="display: flex">
          <div style="display: flex;justify-content: center;flex: 1;border-right: solid 1px #ead4c4;margin-right: 10px;text-align: center">
            <el-avatar style="margin-top: 40px" :size="80" :src="store.avatarUrl"></el-avatar>
          </div>
          <div style="width: 650px">
            <div style="height:120px;overflow: hidden">
              <quill-editor  style="height: calc(100% - 25px)" v-model:content="comment.content"
                             placeholder="请发表有效的评论，不要使用脏话骂人"
                             :options="editorOptions"></quill-editor>
            </div>
            <div style="margin-top: 10px;text-align: right;display: flex">
              <div style="color: green;flex: 1;text-align: left">
                当前字数{{ contentLength }}字(最大支持2000字)
              </div>
              <el-button plain type="success" @click="submit">发表评论</el-button>
            </div>
          </div>
        </div>
      </LightCard>
      <CommentCard style="margin: auto;" v-for="item in topic.comments"
                   :data="item" :tid = "tid" :editorOptions="editorOptions" @delete="loadComment(topic.page);topic.comments--" @add-commit="topic.page = Math.floor(++topic.data.comments/10)+1;loadComment(topic.page)">
      </CommentCard>
      <div style="width: fit-content;margin: 20px auto">
        <el-pagination hide-on-single-page background layout="pre,pager,next" v-model:current-page="topic.page" @current-change="num => loadComment(num)" v-model:total="topic.data.comments" page-size="10"></el-pagination>
      </div>
    </div>
    <topic-editor :show="edit" @close="edit=false"
    :default-type="topic.data.type"
    :default-title="topic.data.title"
    :default-text="topic.data.content"
    default-button="更新帖子内容"
    :submit="updateTopic"/>
  </div>
</template>

<style scoped>

.add-comment{
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  text-align: center;
  line-height: 45px;
  color: var(--el-color-primary);
  background: var(--el-bg-color-overlay);
  box-shadow: var(--el-box-shadow-light);
  &:hover{
    background: var(--el-border-color-extra-light);
    cursor: pointer;
  }
}

.topic-page{
  display: flex;
  flex-direction: column;
  gap:10px;
  padding: 10px 0;
}
.topic-main{
  display: flex;
  border-radius: 7px;
  margin:0 auto;
  background-color: var(--el-bg-color);
  width: 800px;
  .topic-main-left{
    width: 200px;
    padding: 10px;
    text-align: center;
    border-right: solid 1px var(--el-border-color);
    .desc{
      font-size: 12px;
      color: grey;
    }
  }
  .topic-main-right{
    width: 600px;
    padding: 10px 20px;
    display: flex;
    flex-direction: column;
    .topic-content{
      font-size: 14px;
      line-height: 22px;
      opacity: 0.8;
      flex:1
    }
  }
}
:deep(img){
  max-width: 700px;
}
</style>