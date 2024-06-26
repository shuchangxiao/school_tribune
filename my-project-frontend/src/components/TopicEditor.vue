<script setup>
import {Document,Check} from "@element-plus/icons-vue"
import {reactive,computed,ref} from "vue";
import {Delta, Quill, QuillEditor} from "@vueup/vue-quill";
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import ImageResize from 'quill-image-resize-vue'
import {ImageExtend,QuillWatch} from "quill-image-super-solution-module"
import axios from "axios";
import {accessHeader, post} from "@/net/index.js";
import {ElMessage} from "element-plus";
import {userStore} from "@/store/index.js";
import {get} from "@/net/index.js";

import ColorDot from "@/components/ColorDot.vue";
const store = userStore()
const prop = defineProps({
  show:Boolean,
  defaultTitle:{
    default:'',
    type:String
  },
  defaultText:{
    default:'',
    type:String
  },
  defaultType:{
    default:null,
    type:Number
  },
  defaultButton:{
    default:"立即发表主题",
    type:String
  },
  submit :{
    default:(editor,success)=> {
      post("/api/forum/creat-topic", {
        type: editor.type,
        title: editor.title,
        content: editor.text
      }, () => {
        ElMessage.success("帖子发表成功！")
        success()
      })
    },
    type:Function
  }
})
const refEditor = ref()
const editor = reactive({
  type:null,
  title:'',
  text:'',
  loading:false,

})
function initEditor(){
  if(prop.defaultText) editor.text = new Delta(JSON.parse(prop.defaultText))
  refEditor.value.setContents("","user")
  editor.title=prop.defaultTitle
  editor.type = prop.defaultType
}

const emit = defineEmits(['close','success'])
// const types = [
//   {id:1,name:"日常闲聊",desc:"在这里分享你的日常"},
//   {id:2,name:"真挚交友",desc:"在校园里寻找与你志同道合的朋友"},
//   {id:3,name:"踩坑记录",desc:"在这里分享你的踩坑记录，防止他人在收伤害"},
// ]
function deltaToText(delta){
  if(!delta.ops) return ""
  let str = ""
  for (let op of delta.ops) {
    str += op.insert
  }
  return str.replace(/\s/g,"")
}
const typePlaceholder = computed(()=> {
  if(editor.type==null) return "请输入您要发表帖子的主题"
  return store.forum.types[editor.type-1]["desc"]
})
const contentLength= computed(()=>deltaToText(editor.text).length)
function submitTopic(){
  const text = deltaToText(editor.text)
  if(text.length === 0){
    ElMessage.warning("请填写内容")
    return
  }
  if(text.length>20000){
    ElMessage.warning("字数超出限制，无法发布")
    return
  }
  if(!editor.title){
    ElMessage.warning("请填写标题")
    return
  }
  if(!editor.type){
    ElMessage.warning("请选择合适的帖子类型")
    return
  }
  prop.submit(editor,()=>emit("success"))
}
const editorOptions = {
  modules:{
    toolbar:{
      container:[
        "bold", "italic", "underline", "strike","clean",
        {color: []}, {'background': []},
        {size: ["small", false, "large", "huge"]},
        { header: [1, 2, 3, 4, 5, 6, false] },
        {list: "ordered"}, {list: "bullet"}, {align: []},
        "blockquote", "code-block", "link", "image",
        { indent: '-1' }, { indent: '+1' }
      ],
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
      start: () => editor.uploading = true,
      success: () => {
        ElMessage.success('图片上传成功!')
        editor.uploading = false
      },
      error: () => {
        ElMessage.warning('图片上传失败，请联系管理员!')
        editor.uploading = false
      }
    }

  }
}
Quill.register("modules/imageResize",ImageResize)
Quill.register("modules/ImageExtend",ImageExtend)
</script>

<template>
  <div>
    <el-drawer
        @open="initEditor"
        :model-value="show"
        :direction="'btt'"
        :size="550"
        :close-on-click-modal="false"
        @close="emit('close')">
      <template #header>
        <div>
          <div style="font-weight: bold;">发表新的帖子</div>
          <div style="font-size: 13px;">发表新的帖子之前，请遵守相关法律，不用出现不文明现象</div>
        </div>
      </template>
      <div style="display: flex;gap: 10px">
        <div style="width: 150px">
          <el-select placeholder="请选择主题类型" v-model="editor.type" :disabled="!store.forum.types.length">
            <el-option v-for="item in store.forum.types.filter(type => type.id>0)" :value="item.id" :label="item.name" :style="{color:item.color}">
              <div>
                <color-dot :color="item.color"/>
                <span style="margin-left: 5px">{{item.name}}</span>
              </div>
            </el-option>

          </el-select>
        </div>
        <div style="flex: 1">
          <el-input v-model="editor.title" minlength="1" i maxlength="30" type="text" :placeholder="typePlaceholder" :prefix-icon="Document"></el-input>
        </div>
      </div>
      <div style="margin-top: 15px;height: 370px;overflow: hidden"
           v-loading="editor.loading" element-loading-text="正在上传图片，请稍后">
        <quill-editor v-model:content="editor.text"
                      style="height: calc(100% - 45px)"
                      placeholder="今天想分享点什么呢？"
                      content-type="delta"
                      ref="refEditor"
                      :options="editorOptions"/>
      </div>
      <div style="margin-top:10px;display: flex;justify-content:space-between;gap: 10px">
        <div style="color: green">
          当前字数{{ contentLength }}字(最大支持20000字)
        </div>
        <div>
          <el-button @click="submitTopic" type="success" :icon="Check" plain>{{ prop.defaultButton }}</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>

:deep(.el-drawer){
  width: 800px;
  margin: auto;
  border-radius: 8px 8px 0 0;
}
:deep(.el-drawer__header){
  margin: 0;
}
</style>