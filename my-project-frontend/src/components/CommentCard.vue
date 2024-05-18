<script setup>
import axios from "axios";
import {QuillDeltaToHtmlConverter} from 'quill-delta-to-html'
import {ChatSquare, Delete} from "@element-plus/icons-vue";
import {userStore} from "@/store/index.js";
import {QuillEditor} from "@vueup/vue-quill";
import {ref} from "vue"
import {post,get} from "@/net/index.js";
import {ElMessage} from "element-plus";
const store = userStore()
const prop = defineProps({
  editorOptions:JSON,
  tid:Number,
  data:JSON
})
const emit = defineEmits(["delete"])
const comment = {
  content:'',
}
const addCheckComment = ref(false)
function convertToHtml(content){
  const ops = JSON.parse(content).ops
  const converter = new QuillDeltaToHtmlConverter(ops,{inlineStyles:true})
  return converter.convert()
}
function deleteComment(){
  get(`api/forum/delete-comments?id=${prop.data.id}`,()=>{
    emit("delete")
    ElMessage.success("删除评论成功")
  })
}
function submit(){
  post("/api/forum/add-comment",{
    tid:prop.tid,
    quote: prop.data.id,
    content: JSON.stringify(comment.content)
  },()=>{
    ElMessage.success("发表评论成功")
  })
}
</script>

<template>
  <div class="comment-class">
    <div>
      <el-avatar :src="axios.defaults.baseURL + '/images' + data.user.avatar" :size="60"></el-avatar>
    </div>
    <div style="flex: 1;margin-left: 20px">
      <div style="display: flex;flex-direction: row">
        <div style="color: #61661D;flex:1">{{data.user.username}}</div>
        <div style="text-align:right;color: #9499A0;font-size: 12px;margin-top: 4px">{{new Date(data.time).toLocaleString()}}</div>
      </div>
      <div class="comment-quote" v-if="data.quote">
        <div>&nbsp;回复：{{data.quote}}</div>
      </div>
      <div v-html="convertToHtml(data.content)"></div>
      <div  style="text-align: right">
        <el-link @click="deleteComment" :icon="Delete" style="margin-right: 20px" type="danger" v-if="store.user.id ===data.user.id">&nbsp;删除评论</el-link>
        <el-link :icon="ChatSquare" @click="addCheckComment = addCheckComment !== true;comment.quote=data.quote">回复评论</el-link>
      </div>
      <div style="display: flex" v-if="addCheckComment">
        <div style="display: flex;justify-content: center;flex: 1;margin-right: 10px;text-align: center">
          <el-avatar style="margin-top: 25px" :size="40" :src="store.avatarUrl"></el-avatar>
        </div>
        <div style="width: 650px;">
        <div style="height:80px;overflow: hidden">
          <quill-editor  style="height: calc(100% - 25px)" v-model:content="comment.content"
                         placeholder="请发表有效的评论，不要使用脏话骂人"
                         :options="editorOptions"></quill-editor>
        </div>
        <div style="margin-top: 10px;text-align: right">
          <el-button plain type="success" @click="submit">发表评论</el-button>
        </div>
      </div>
      </div>
      <el-divider></el-divider>
    </div>
  </div>
</template>

<style scoped>
.comment-quote{
  background-color: rgba(94,94,94,0.1);
  border-radius: 5px;
  font-size: 13px;
  padding: 10px;

}
.comment-class{
  background-color: whitesmoke;
  width: 800px;
  border-radius: 8px;
  display: flex;
  flex-direction: row;

}

:deep(.el-divider--horizontal){
  margin: 5px 0;
}
:deep(img){
  max-width: 700px;
}
</style>