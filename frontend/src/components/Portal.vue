<template>
<div>
    <el-header>
        <el-row style="width: 100%; height: 100%; margin-top: auto; margin-bottom: auto" type="flex" align="middle">
            <el-col :span="1">
                <div>
                    <span>JSTF</span>
                </div>
            </el-col>
            <el-col :span="2" :offset="21"> 
                <el-dropdown>
                     <span class="el-dropdown-link">
                        {{user.name}}<i class="el-icon-arrow-down el-icon--right"></i>
                    </span>
                    <el-dropdown-menu slot="dropdown">
                        <el-dropdown-item>退出登录</el-dropdown-item>
                    </el-dropdown-menu>
                </el-dropdown>
            </el-col>
        </el-row>
    </el-header>
    <el-main>
        <router-view></router-view>
    </el-main>
</div>
</template>

<script>

export default {
    name: 'Portal',

    data() {
        return {
            user: {
                'name': '工程卓越'
            }
        }

    },

    methods: {

    }
}
</script>

<style scoped>
.el-header {
    background:rgb(171, 178, 190);
    /* display: flex; */
    /* height: 20% !important; */
}

.el-main {
    padding: 0px !important;
}
</style>