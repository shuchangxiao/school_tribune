import axios from "axios";
import {ElMessage} from "element-plus";

const authItemName= "access_token"
const defaultFailure = (message,code,url)=>{
    console.warn(`请求地址：${url},状态码${code},信息${message}`)
    ElMessage.warning(message)
}
const defaultError = (error)=>{
    console.error(error)
    ElMessage.warning("发生了一些错误，请练习管理员")

}
function storeAccessToke(token,remember,expire){
    const authObj = {token:token,expire:expire}
    const str = JSON.stringify(authObj)
    if(remember)
        localStorage.setItem(authItemName,str)
    else
        sessionStorage.setItem(authItemName,str)
}
function takeAccessToken(){
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName)
    if(!str) return null
    const  authObj = JSON.parse(str)
    if(authObj.expire <=new Date()){
        deleteAccessToke()
        ElMessage.warning("登录状态已过期，请重新登录")
        return null
    }
    return authObj.token

}
function deleteAccessToke(){
    localStorage.removeItem(authItemName)
    sessionStorage.removeItem(authItemName)
}
function accessHeader(){
    return {
        'Authorization':`Bearer ${takeAccessToken()}`
    }
}
function isUnAuthorized(){
    return !takeAccessToken()
}
function internalPost(url,data,headers,success,failure=defaultFailure,error=defaultError){
    axios.post(url,data, {headers: headers})
        .then(({data})=>{
            if(data.code === 200){
                success(data.data)
            }else {
                failure(data.message,data.code,url)
            }
        })
        .catch(err => error(err))
}
function get(url,success,failure=defaultFailure){
    internalGet(url,accessHeader(),success,failure)

}
function post(url,data,success,failure=defaultFailure){
    internalPost(url,data,accessHeader(),success,failure)

}
function internalGet(url,header,success,failure=defaultFailure,error=defaultError){
    axios.get(url, {headers: header})
        .then(({data})=>{
            if(data.code === 200){
                success(data.data)
            }else {
                failure(data.message,data.code,url)
            }
        })
        .catch(err => error(err))
}
function  login(username,password,remember,success,failure=defaultFailure){
    internalPost("/api/auth/login",
        {
            username:username,
            password:password,
            remember:remember
        },{
            "Content-Type":"application/x-www-form-urlencoded"
        },(data)=>{
            storeAccessToke(data.token,remember,data.expire)
            ElMessage.success(`登录成功，欢迎${data.username}`)
            success(data)
        })
}
function logout(success){
    internalGet("/api/auth/logout",accessHeader(),()=>{
        deleteAccessToke()
        ElMessage.success("退出登录成功，欢迎您再次使用！")
        success()
    })
}
export {login,get,post,logout,isUnAuthorized}