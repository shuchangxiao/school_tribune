<script setup>
import {QuillEditor} from "@vueup/vue-quill";
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import {ref} from "vue";
import {post} from "@/net/index.js";
import {ElMessage} from "element-plus";

const props = defineProps({
  show:Boolean,
  tid:String,
  quote:Number
})
const content = ref()
const emit = defineEmits(['close'])
function submit(){
  post("/api/forum/add-comment",{
    tid:props.tid,
    quote: props.quote,
    content: JSON.stringify(content.value)
  },()=>{
    ElMessage.success("发表评论成功")
    content.value=""
    emit('close')
  })
}
</script>

<template>
  <div>
    <el-drawer :model-value="show" @close="emit('close')"
               title="发表评论" direction="btt"
               :size="270" :close-on-click-modal="false">
      <div>
        <div>
          <quill-editor style="height: 120px" v-model:content="content"
          placeholder="请发表有效的评论，不要使用脏话骂人"></quill-editor>
        </div>
        <div style="margin-top: 10px;text-align: right">
          <el-button plain type="success" @click="submit">发表评论</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
:deep(.el-drawer){
  width: 800px;
  margin: 20px auto;
  border-radius: 8px 8px 0 0;
}
:deep(.el-drawer__header){
  margin: 0;
}
:deep(.el-drawer__body){
  padding: 10px;
}
</style>