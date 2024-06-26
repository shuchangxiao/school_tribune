import {createRouter, createWebHistory} from "vue-router";
import {isUnAuthorized} from '@/net/index.js'
const router = createRouter({
    history:createWebHistory(import.meta.env.BASE_URL),
    routes:[
        {
            path:'/',
            name:'welcome',
            component:()=>import('@/views/WelcomeView.vue'),
            children:[
                {
                    path:'',
                    name:'welcome-login',
                    component:()=>import("@/views/welcome/LoginPage.vue")
                },
                {
                    path:'/register',
                    name:'welcome-register',
                    component:()=>import("@/views/welcome/RegisterPage.vue")
                }, {
                    path:'/forget',
                    name:'welcome-forget',
                    component:()=>import("@/views/welcome/ForgetPage.vue")
                },
            ]
        },
        {
            name:'index',
            path:'/index',
            component:()=>import('@/views/IndexPage.vue'),
            children:[
                {
                    path: "",
                    name: "topic",
                    component:import("@/views/forum/Forum.vue"),
                    children:[
                        {
                            path: "",
                            name: "topic-list",
                            component:()=>import("@/views/forum/TopicList.vue")
                        },
                        {
                            path: "topic-detail/:tid",
                            name: "topic-detail",
                            component:()=>import("@/views/forum/TopicDetail.vue")
                        },
                    ]
                },

                {
                    path: "user-setting",
                    name: "user-setting",
                    component:()=>import("@/views/settings/UserSetting.vue")
                },
                {
                    path: "privacy-setting",
                    name: "privacy-setting",
                    component:()=>import("@/views/settings/PrivacySetting.vue")
                }
            ]
        }
    ]
})

router.beforeEach((to,from,next)=>{
    const  unAuthorized = isUnAuthorized()
    if(to.name.startsWith('welcome-') && !unAuthorized){
        next('/index')
    }else if(to.fullPath.startsWith('/index') && unAuthorized)
        next('/')
    else {
        next()
    }
})


export default router
