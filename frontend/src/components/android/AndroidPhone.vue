<template>
  <div>
    <el-container>
      <el-aside>
        <AndroidScreen/>
      </el-aside>
      <el-main>
        <AndroidDetail/>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import AndroidDetail from "./AndroidDetail.vue"
import AndroidScreen from "./AndroidScreen.vue"

export default {
  name: "AndroidPhone",

  components: { AndroidDetail, AndroidScreen },
  
}
</script>


<style>

.el-aside {
    width: auto !important;
}

.el-main {
    overflow: scroll;
}

</style>
