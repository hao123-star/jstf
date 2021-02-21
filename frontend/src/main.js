import Vue from 'vue'
import ElementUI from "element-ui";
import "element-ui/lib/theme-chalk/index.css";
import App from './App.vue'
import axios from 'axios';
import QS from 'qs'; 
import router from './router/index'
Vue.prototype.$axios =axios;
Vue.prototype.qs = QS;

Vue.use(ElementUI);

Vue.config.productionTip = false

new Vue({
  router,
  render: h => h(App),
}).$mount('#app')
