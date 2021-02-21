<template>
  <div>
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane label="控制面版" name="panel">
        <android-ctrl-panel></android-ctrl-panel>
      </el-tab-pane>
      <el-tab-pane label="屏幕截图" name="screen">
        <android-screen-shot></android-screen-shot>
      </el-tab-pane>
      <el-tab-pane label="自动化" name="auto"></el-tab-pane>
      <!-- <el-tab-pane label="Terminal" name="terminal">
        <android-terminal v-bind:ws="ws"></android-terminal>
      </el-tab-pane> -->
      <el-tab-pane label="文件管理器" name="file">
        <android-file-sys></android-file-sys>
      </el-tab-pane>
      <el-tab-pane label="信息" name="info">
        <android-info
          v-bind:batteryInfo="batteryInfo"
          v-bind:displayInfo="displayInfo"
          v-bind:SIMInfo="SIMInfo"
          v-bind:networkInfo="networkInfo"
        ></android-info>
      </el-tab-pane>
      <el-tab-pane label="日志" name="log"></el-tab-pane>
      <el-tab-pane label="监控" name="monitor"></el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import AndroidInfo from "./AndroidInfo.vue";
import AndroidFileSys from "./AndroidFileSys.vue";
import AndroidScreenShot from "./AndroidScreenShot.vue";
import AndroidCtrlPanel from "./AndroidCtrlPanel.vue";

export default {
  name: "AndroidDetail",

  components: {
    AndroidInfo,
    AndroidFileSys,
    AndroidScreenShot,
    AndroidCtrlPanel,
  },

  props: {
    ws: Object,
    batteryInfo: Object,
    displayInfo: Object,
    SIMInfo: Object,
    networkInfo: Object,
  },

  data() {
    return {
      activeName: "panel",
    };
  },

  methods: {
    handleClick: function () {},
  },
};
</script>

<style scoped>
.el-card-info {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.el-card-info:first-child {
  text-align: left;
  margin-bottom: 30px;
}

.el-card-info:last-child td:first-child {
  text-align: right;
  width: 50%;
  line-height: 40px;
}
.el-card-ctrl-panel {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.el-card-ctrl-panel > div:first-child {
  text-align: left;
  margin-bottom: 20px;
  font-size: large;
}

.el-card-ctrl-panel > div:last-child {
  text-align: left;
}

div[role="tab"] {
  font-size: large !important;
}
</style>