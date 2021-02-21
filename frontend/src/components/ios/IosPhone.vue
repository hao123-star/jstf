<template>
    <el-container>
        <el-aside>
            <IosScreen/>
        </el-aside>
        <el-main>
        </el-main>
    </el-container>
</template>

<script>
import IosScreen from './IosScreen.vue'

export default {
    name: "IosPhone",
    components: { IosScreen }
}
</script>

<style>

.el-aside {
    /* height: 100vh; */
    /* overflow: scroll; */
    width: auto !important;
}

.el-main {
    overflow: scroll;
    /* height: 100vh; */
}

</style>