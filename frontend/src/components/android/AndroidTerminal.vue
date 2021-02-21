<template>
  <div class="container">
    <p
      style="font-size: 18px; line-height: 1; color: #909399; margin: 0 0 5px"
    ></p>
    <div ref="xterm" class="xterm" />
  </div>
</template>

<script>
import "xterm/css/xterm.css";
import { Terminal } from "xterm";
import { FitAddon } from "xterm-addon-fit";
import { AttachAddon } from "xterm-addon-attach";
import "xterm/css/xterm.css";
import "xterm/lib/xterm.js";
import "../js/websocket.js";

export default {
  name: "AndroidTerminal",

  props: {
    ws: Object,
  },

  data() {
    return {
      xterm: null,
      xtermWS: null,
    };
  },

  mounted() {
    this.xtermWS = new WebSocket("ws://localhost:5200/cmd/Q8WDU15817006617");
    this.xtermWS.onopen = this.onOpenWS;
    this.xtermWS.onmessage = this.onMessageWS;
    this.onerror = this.onErrorWS;
    this.onclose = this.onCloseWS;

    this.xterm = new Terminal({
      rendererType: "canvas",
      rows: 40, //行数
      scrollback: 50, //终端中的回滚量
      cursorBlink: true, //光标闪烁
      disableStdin: false, //是否应禁用输入
      convertEol: true, //启用时，光标将设置为下一行的开头
      cursorStyle: "underline", //光标样式
      theme: {
        foreground: "yellow", //字体
        background: "#060101", //背景色
        // cursor: "help", //设置光标
        // lineHeight: 16,
      },
    });

    let fitAddon = new FitAddon();
    this.xterm.loadAddon(fitAddon);

    let attachAddon = new AttachAddon(this.xtermWS);
    this.xterm.loadAddon(attachAddon);

    this.xterm.AttachAddon;
    this.xterm.open(this.$refs.xterm);
    fitAddon.fit();

    this.xterm.focus();

    // this.xterm.write("\r");

    // let _this = this;

    // this.xterm.onData(function (key) {
    //   let printable =
    //     !ev.altKey && !ev.altGraphKey && !ev.ctrlKey && !ev.metaKey;
    //   _this.xterm.write(key);

    //   console.log(printable);
    //   console.log(key);
    // });
    this.runFakeTerm();
  },

  beforeDestroy() {
    if (this.xterm) {
      this.xterm.dispose();
    }
  },

  methods: {
    onOpenWS: function () {
      this.xtermWS.send("\r");
    },

    onMessageWS: function () {},

    onErrorWS: function () {},

    onCloseWS: function () {},

    prompt: function () {
      this.xterm.write("\r\n");
    },

    runFakeTerm: function () {
      if (this.xterm._initialized) {
        return;
      }

      this.xterm._initialized = true;

      this.xterm.onKey((e) => {
        const ev = e.domEvent;

        const printable =
          !ev.altKey && !ev.altGraphKey && !ev.ctrlKey && !ev.metaKey;

        if (ev.keyCode === 13) {
          this.prompt();
        } else if (ev.keyCode === 8) {
          // Do not delete the prompt
          if (this.xterm._core.buffer.x > 2) {
            this.xterm.write("\b \b");
          }
        } else if (printable) {
          this.xterm.write(e.key);
        }
      });
    },
  },
};
</script>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.xterm {
  flex: 1;
}
</style>