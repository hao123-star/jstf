import Vue from 'vue'
import Router from 'vue-router'
import Login from '../components/Login.vue'
import Portal from '../components/Portal.vue'
import Phones from '../components/Phones.vue'
import IosPhone from '../components/ios/IosPhone.vue'
import AndroidPhone from '../components/android/AndroidPhone'

Vue.use(Router)

export default new Router({
    routes: [
        {
            name: 'login',
            path: '/',
            component: Login
        },
        {
            // name: 'portal',
            path: '/portal',
            component: Portal,
            children: [
                {
                    name: 'phones',
                    path: '',
                    component: Phones,
                },
                {
                    name: 'iosPhone',
                    path: 'ios',
                    component: IosPhone,
                }, 
                {
                    name: 'androidPhone',
                    path: 'android',
                    component: AndroidPhone,
                }
            ]
        }
    ]
})
