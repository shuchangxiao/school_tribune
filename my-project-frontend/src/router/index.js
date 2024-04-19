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
                }
            ]
        },
        {
            name:'index',
            path:'/index',
            component:()=>import('@/views/IndexPage.vue')
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
