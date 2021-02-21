<template>
  <div id="aside" v-bind:style="asideStyle">
        <div id="asideTop">
          <div style="display: inline-block">
            <img
              src="../../assets/phone.png"
              align="middle"
              width="30"
              height="35"
            />
            <span style="text-align: center">PE-CL00</span>
          </div>
          <div>
            <span>
              <img
                src="../../assets/portrait.png"
                align="middle"
                width="15"
                height="20"
              />
            </span>
            <span>
              <img
                src="../../assets/landscape.png"
                align="middle"
                width="15"
                height="20"
              />
            </span>
            <span>
              <img
                src="../../assets/eye.png"
                align="middle"
                width="25"
                height="20"
              />
            </span>
            <span>
              <img
                src="../../assets/close.png"
                align="middle"
                width="15"
                height="20"
              />
            </span>
          </div>
        </div>
        <div id="asideMiddle" v-bind:style="asideMiddleStyle">
          <canvas
            id="cv"
            ref="cv"
            v-bind:width="canvas.width"
            v-bind:height="canvas.height"
            v-bind:style="canvasStyle"
          ></canvas>
        </div>
        <div id="asideBottom">
          <span>
            <img
              src="../../assets/menu.png"
              align="middle"
              width="25"
              height="25"
            />
          </span>
          <span>
            <img
              src="../../assets/home.png"
              align="middle"
              width="25"
              height="25"
            />
          </span>
          <span>
            <img
              src="../../assets/switch.png"
              align="middle"
              width="25"
              height="25"
            />
          </span>
          <span>
            <img
              src="../../assets/return.png"
              align="middle"
              width="25"
              height="25"
            />
          </span>
        </div>
  </div>
</template>

<script>
export default {
  name: "AndroidScreen",
  
  data() {
    return {
      ws: {
        url: 'ws://localhost:5200',
        instance: null
      },
      stfWS: null,
      capWS: null,
      touchWS: null,

      canvas: {
        width: 1920,
        height: 1080,
        ready: true,
        element: null,
        ctx: null,
      },

      canvasStyle: {
        width: "100%",
        height: "100%",
        background: "dimgray",
      },

      screen: {
        bounds: {},
      },

      phone: {
        rotation: -1,
        serial: 'Q8WDU15817006617',
        width: 0,
        height: 0,
        screenWidth: 0,
        screenHeight: 0,
        ready: false
      },

      asideMiddleStyle: {
        "margin-top": "0px",
        "margin-bottom": "0px",
        "padding-left": "10%",
        "padding-right": "10%",
        height: "100%",
      },

      asideStyle: {
        width: "650px",
      },

      msg: null,

      touchMsg: "",

      rotation: 0,
    };
  },

  watch: {},

  created: function () {},

  computed: {},

  mounted: function () {
    this.canvas.element = this.$refs.cv;
    this.canvas.element.addEventListener("mousedown", this.mouseDownListener);
    this.canvas.ctx = document.getElementById("cv").getContext("2d");
    // this.initStfWS();
    this.initWS();
  },

  updated: function () {
    this.canvas.ready = true;
  },

  methods: {
    initWS: function () {
      this.ws.instance = new WebSocket(this.ws.url + "/android/" + this.phone.serial);

      let that = this;  

      this.ws.instance.onopen = () => {
        let msg = {type: 'initMsg'};
        this.ws.instance.send(JSON.stringify(msg));
      };

      let img = new Image();
      let imgRotation = -1;

      this.ws.instance.onmessage = (event) => {
        if (that.canvas.ready === false) {
          return;
        }

        if (typeof(event.data) != 'string') {
          img.src = URL.createObjectURL(event.data);

          if (img.naturalHeight > img.naturalWidth) {
            if (imgRotation != 0) {
                imgRotation = 0;

                that.asideStyle = {
                  width: "650px"
                };

                that.asideMiddleStyle = {
                  "margin-top": "0px",
                  "margin-bottom": "0px",
                  "padding-left": "10%",
                  "padding-right": "10%",
                  height: "87vh",
                };

                that.canvas.width = that.phone.screenWidth;
                that.canvas.height = that.phone.screenHeight;
            }
          } else {
            if (imgRotation != 1) {
                imgRotation = 1;

                that.asideStyle = {
                  width: "850px",
                };

                that.asideMiddleStyle = {
                  "margin-top": "0px",
                  "margin-bottom": "0px",
                  "padding-left": "unset",
                  "padding-right": "unset",
                  height: "400px",
                };

                that.canvas.width = that.phone.screenHeight;
                that.canvas.height = that.phone.screenWidth;
            }
          }

          img.onload = () => {
            that.canvas.ctx.drawImage(
              img,
              0,
              0,
              that.canvas.width,
              that.canvas.height
            );
          };
        } else {
          this.handleMsg(JSON.parse(event.data));
        }
      };

      this.ws.instance.onclose = () => {
        let el = this.canvas.element;
        el.removeEventListener("mousedown", this.mouseDownListener);
        document.removeEventListener("mouseup", this.mouseUpListener);
      };

      this.ws.instance.onerror = () => {
        let el = this.canvas.element;
        el.removeEventListener("mousedown", this.mouseDownListener);
        document.removeEventListener("mouseup", this.mouseUpListener);
      };
    },

    // initStfWS: function () {
    //   this.stfWS = new WebSocket("ws://localhost:5200/stf/Q8WDU15817006617");

    //   this.stfWS.onopen = () => {
    //     this.stfWS.send("Rotation");
    //   };

    //   this.stfWS.onclose = () => {};

    //   this.stfWS.onerror = () => {};

    //   let that = this;
    //   this.stfWS.onmessage = (event) => {
    //     let stfMsg = JSON.parse(event.data);
    //     let type = stfMsg.type;

    //     switch (type) {
    //       case "Rotation":
    //         that.rotation = stfMsg.rotation;

    //         if (that.rotation === 0) {
    //           that.asideStyle = {
    //             width: "650px",
    //           };
    //           that.asideMiddleStyle = {
    //             "margin-top": "0px",
    //             "margin-bottom": "0px",
    //             "padding-left": "10%",
    //             "padding-right": "10%",
    //             height: "100%",
    //           };
    //           that.canvas.width = 1080;
    //           that.canvas.height = 1920;
    //         } else {
    //           that.asideStyle = {
    //             width: "850px",
    //           };
    //           that.asideMiddleStyle = {
    //             "margin-top": "0px",
    //             "margin-bottom": "0px",
    //             "padding-left": "unset",
    //             "padding-right": "unset",
    //             height: "400px",
    //           };
    //           that.canvas.width = 1920;
    //           that.canvas.height = 1080;
    //         }
    //         break;
    //       case "Battery":
    //         that.batteryInfo = stfMsg;
    //         break;
    //       case "DisPlay":
    //         that.displayInfo = stfMsg;
    //         break;
    //       case "SIM":
    //         that.SIMInfo = stfMsg;
    //         break;
    //       case "NetWork":
    //         that.networkInfo = stfMsg;
    //         break;
    //       default:
    //         break;
    //     }

    //     if (!that.capWS) {
    //       that.initCapWS();
    //     }
    //   };
    // },

    // initCapWS: function () {
    //   this.capWS = new WebSocket("ws://localhost:5200/cap/Q8WDU15817006617");

    //   this.capWS.onopen = () => {
    //     this.initTouchWS();
    //   };

    //   this.capWS.onclose = () => {
    //     let el = this.canvas.element;
    //     el.removeEventListener("mousedown", this.mouseDownListener);
    //     el.removeEventListener("mousewheel", this.mouseWheelListener);
    //   };

    //   this.capWS.onerror = () => {
    //     let el = this.canvas.element;
    //     el.removeEventListener("mousedown", this.mouseDownListener);
    //     el.removeEventListener("mousewheel", this.mouseWheelListener);
    //   };

    //   //3. 新建一个Image对象
    //   let img = new Image();
    //   let that = this;
    //   this.capWS.onmessage = (event) => {
    //     if (that.canvas.ready === false) {
    //       return;
    //     }

    //     img.src = URL.createObjectURL(event.data);

    //     img.onload = () => {
    //       that.canvas.ctx.drawImage(
    //         img,
    //         0,
    //         0,
    //         that.canvas.width,
    //         that.canvas.height
    //       );
    //     };
    //   };
    // },

    // initTouchWS: function () {
    //   this.touchWS = new WebSocket(
    //     "ws://localhost:5200/touch/Q8WDU15817006617"
    //   );

    //   this.touchWS.onopen = () => {};

    //   this.touchWS.onclose = () => {
    //     let el = this.canvas.element;
    //     el.removeEventListener("mousedown", this.mouseDownListener);
    //     el.removeEventListener("mousewheel", this.mouseWheelListener);
    //   };

    //   this.touchWS.onerror = () => {};

    //   this.touchWS.onmessage = (event) => {
    //     this.rotation = event.data;
    //   };
    // },

     handleMsg: function (msg) {
        let type = msg.type;

        if (type === 'displayMsg') {
          this.phone.screenWidth = msg.width;
          this.phone.screenHeight = msg.height;
          this.phone.ready = true;
          return;
        }

        if (type === 'rotationMsg') {
          this.phone.rotation = msg.rotation;
          return;
        }
       
     },

    calculateBounds: function () {
      let el = this.canvas.element;
      this.screen.bounds.w = el.offsetWidth;
      this.screen.bounds.h = el.offsetHeight;
      this.screen.bounds.x = 0;
      this.screen.bounds.y = 0;

      while (el.offsetParent) {
        this.screen.bounds.x += el.offsetLeft;
        this.screen.bounds.y += el.offsetTop;
        el = el.offsetParent;
      }
    },

    coords: function (boundingW, boundingH, relX, relY, rotation) {
      let w, h, x, y;

      switch (rotation) {
        case 0:
          w = boundingW;
          h = boundingH;
          x = relX;
          y = relY;
          break;
        case 90:
          w = boundingH;
          h = boundingW;
          x = boundingH - relY;
          y = relX;
          break;
        case 180:
          w = boundingW;
          h = boundingH;
          x = boundingW - relX;
          y = boundingH - relY;
          break;
        case 270:
          w = boundingH;
          h = boundingW;
          x = relY;
          y = boundingW - relX;
          break;
      }

      return {
        xP: x / w,
        yP: y / h,
      };
    },

    // 鼠标down
    touchDown: function (index, x, y, pressure) {
      let scaled = this.coords(
        this.screen.bounds.w,
        this.screen.bounds.h,
        x,
        y,
        this.phone.rotation
      );

      let width = 0;
      let height = 0;

      if (this.phone.rotation === 0) {
        width = this.canvas.width;
        height = this.canvas.height;
      } else {
        width = this.canvas.height;
        height = this.canvas.width;
      }

      let action = "d ".concat(
        index,
        " ",
        (scaled.xP * width).toFixed(0),
        " ",
        (scaled.yP * height).toFixed(0),
        " ",
        pressure,
        "\n"
      );

      let msg = {
        'type': 'touchMsg',
        'action': action
      }

      this.ws.instance.send(JSON.stringify(msg));

      // this.touchWS.send(this.touchMsg);
    },

    // 鼠标move
    touchMove: function (index, x, y, pressure) {
      let scaled = this.coords(
        this.screen.bounds.w,
        this.screen.bounds.h,
        x,
        y,
        this.phone.rotation
      );

      let width = 0;
      let height = 0;

      if (this.phone.rotation === 0) {
        width = this.canvas.width;
        height = this.canvas.height;
      } else {
        width = this.canvas.height;
        height = this.canvas.width;
      }

      let action = "m ".concat(
        index,
        " ",
        (scaled.xP * width).toFixed(0),
        " ",
        (scaled.yP * height).toFixed(0),
        " ",
        pressure,
        "\n"
      );

      let msg = {
        'type': 'touchMsg',
        'action': action
      }

      this.ws.instance.send(JSON.stringify(msg));
    },

    //
    touchWait: function (millseconds) {
      let action = "w ".concat(millseconds, "\n");
      
      let msg = {
        'type': 'touchMsg',
        'action': action
      }

      this.ws.instance.send(JSON.stringify(msg));
    },

    // 鼠标up
    touchUp: function (index) {
      let action = "u ".concat(index, "\n");

      let msg = {
        'type': 'touchMsg',
        'action': action
      }

      this.ws.instance.send(JSON.stringify(msg));
    },

    touchReset: function () {
      let action = "r \n";
      
      let msg = {
        'type': 'touchMsg',
        'action': action
      }

      this.ws.instance.send(JSON.stringify(msg));
    },

    touchCommit: function () {
      let action = "c\n";
      
      let msg = {
        'type': 'touchMsg',
        'action': action
      }

      this.ws.instance.send(JSON.stringify(msg));
    },

    // 监听鼠标down
    mouseDownListener: function (event) {
      let e = event;

      if (e.originalEvent) {
        e = e.originalEvent;
      }

      e.preventDefault();

      this.calculateBounds();

      let x = e.pageX - this.screen.bounds.x;
      let y = e.pageY - this.screen.bounds.y;
      let pressure = 50;

      this.touchDown(0, x, y, pressure);
      this.touchCommit();

      this.canvas.element.addEventListener("mousemove", this.mouseMoveListener);
      document.addEventListener("mouseup", this.mouseUpListener);
    },

    // 监听鼠标move
    mouseMoveListener: function (event) {
      let e = event;

      if (e.originalEvent) {
        e = e.originalEvent;
      }

      e.preventDefault();

      let pressure = 50;
      let x = e.pageX - this.screen.bounds.x;
      let y = e.pageY - this.screen.bounds.y;

      this.touchMove(0, x, y, pressure);
      this.touchCommit();
    },

    // 监听鼠标up
    mouseUpListener: function (event) {
      let e = event;

      if (e.originalEvent) {
        e = e.originalEvent;
      }

      e.preventDefault();
      this.touchUp(0);
      this.touchCommit();
      this.stopMousing();
    },

    // 监听鼠标
    mouseWheelListener: function (event) {
      console.log(event.data);
    },

    stopMousing() {
      this.canvas.element.removeEventListener(
        "mousemove",
        this.mouseMoveListener
      );
      document.removeEventListener("mouseup", this.mouseUpListener);
    },
  },
};
</script>

<style scoped>
#asideTop {
  background: whitesmoke;
  display: table-cell;
  vertical-align: middle;
}

#asideTop > div:nth-child(2) {
  display: flex;
  float: right;
  justify-content: space-around;
  width: 25%;
}

#asideTop > div:nth-child(2) > span {
  width: 22px;
  height: 30px;
  border-radius: 5px;
  text-align: center;
  margin: auto;
}

.el-aside > div:nth-child(3) {
  display: flex;
  justify-content: space-around;
  background: #167ffc;
  height: 60px;
}

#asideBottom {
  display: flex;
  justify-content: space-around;
  background: #167ffc;
  height: 60px;
}

#asideBottom > span {
  text-align: center;
  margin: auto;
}

#aside {
  background-color: #d3dce6;
  color: #333;
  height: 100vh; 
  /* height: auto; */
  flex-direction: column;
  display: flex;
  justify-content: space-between;
}

.el-aside {
  background-color: #d3dce6;
  color: #333;
  height: 100vh;
  flex-direction: column;
  display: flex;
  justify-content: space-between;
}

.el-main {
  margin-left: 2px;
}

.el-main {
  background-color: #e9eef3;
  color: #333;
  text-align: center;
}
</style>