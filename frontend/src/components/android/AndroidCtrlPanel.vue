<template>
  <div>
    <el-row>
      <el-col :span="12">
        <el-card shadow="hover">
          <div class="el-card-ctrl-panel">
            <div><span>输入</span></div>
            <div>
              <el-input
                type="textarea"
                :rows="2"
                placeholder="请输入内容"
                v-model="textarea"
                id="input"
              >
              </el-input>
            </div>
          </div>
        </el-card>
        <el-card shadow="hover">
          <div class="el-card-ctrl-panel">
            <div><span>上传APP</span></div>
            <div>
              <el-upload
                class="upload-demo"
                drag
                action="https://jsonplaceholder.typicode.com/posts/"
                multiple
              >
                <i class="el-icon-upload"></i>
                <div class="el-upload__text">
                  将文件拖到此处，或<em>点击上传</em>
                </div>
                <div class="el-upload__tip" slot="tip">不超过500kb</div>
              </el-upload>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <div class="el-card-ctrl-panel">
            <div><span>维护</span></div>
            <div>
              <el-button type="danger" @click="restart">重启设备</el-button>
              <el-button type="primary" @click="releaseWakeLock"
                >唤醒屏幕</el-button
              >
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "AndroidCtrlPanel",

  data() {
    return {
      textarea: "",
      cmdWS: null,
    };
  },

  mounted: function () {
    this.initCmdWS();
    let el = document.getElementById("input");
    el.addEventListener("keydown", this.keyDown);
  },

  methods: {
    initCmdWS: function () {
      if (!this.cmdWS) {
        this.cmdWS = new WebSocket("ws://localhost:5200/cmd/Q8WDU15817006617");
      }

      this.cmdWS.onopen = () => {};

      this.cmdWS.onclose = () => {};

      this.cmdWS.onerror = () => {};

      this.cmdWS.onmessage = () => {};
    },

    restart: function () {
      this.cmdWS.send("reboot");
    },

    releaseWakeLock: function () {
      this.cmdWS.send("input keyevent 26");
    },

    keyDown: function (event) {
      this.cmdWS.send("input text " + String.fromCharCode(event.keyCode));
    },
  },
};
</script>