<template>
<div class="login">
    <el-form :model="user" status-icon ref="login" label-width="100px" >
        <el-form-item label="姓名：" prop="name">
            <el-input v-model="user.name" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="密码：" prop="password">
            <el-input type="password" v-model="user.password" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item style="text-align: right">
            <el-button type="primary" @click="login">提交</el-button>
            <el-button @click="reset">重置</el-button>
        </el-form-item>
    </el-form>
</div>
</template>

<script>
export default {
    name: 'Login',

    data() {
        return {
            user: {
                'name': '',
                'password': ''
            }
        }
    },

    methods: {
        login: function() {
            this.$router.push({path: '/portal'});
        },

        reset: function() {

        }
    }
}
</script>

<style scoped>

.login {
    width: 30%;
    height: 20%;
    margin: 0 auto;
    margin-top: 10%;
    padding-bottom: 5px;
    padding-right: 10px;
    padding-top: 20px;
    background:ghostwhite;
  
}
</style>