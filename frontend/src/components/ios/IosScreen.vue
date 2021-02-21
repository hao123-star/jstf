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
          <canvas ref="cv" 
          v-bind:width="canvas.width"
          v-bind:height="canvas.height"
          v-bind:style="canvasStyle"
          />
        </div>
        <div id="asideBottom">
          <span>
            <el-button circle style="width: 55px; height: 55px" @click="simulateHomeScreen"></el-button>
          </span>
        </div>
  </div>
</template>

<script>
export default {
  name: "IosScreen",

  data() {
    return {
      ws: {
        url: 'ws://localhost:5200',
        instance: null
      },

      canvas: {
        width: 0,
        height: 0,
        ready: false,
        element: null,
        ctx: null
      },

      canvasStyle: {
        width: "100%",
        height: "100%",
        background: "dimgray"
      },

      screen: {
        bounds: {},
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

      phone: {
        rotation: -1,
        serial: 'a83d2b9bc137837fa5d7c7bc50656a1db2e3d376',
        width: 0,
        height: 0,
        screenWidth: 0,
        screenHeight: 0,
        ready: false
      },

      mouseDownPoint: {
        x: -1,
        y: -1,
        t: null
      }, 

      mouseMovePoint: {
        x: -1,
        y: -1,
        t: null
      },

      fromPoint: {
        x: -1,
        y: -1,
        t: null
      },

      toPoint: {
        x: -1,
        y: -1,
        t: null
      }
    };
  },

  props: {
    url: {
      type: String
    }
  },

  watch: {
    // 'phone.rotation': function(val) {
    //   if (val === 0) {
    //     this.asideStyle = {
    //       width: "650px"
    //     };

    //     this.asideMiddleStyle = {
    //       "margin-top": "0px",
    //       "margin-bottom": "0px",
    //       "padding-left": "10%",
    //       "padding-right": "10%",
    //       height: "87vh",
    //     };

    //     this.canvas.width = this.phone.screenWidth;
    //     this.canvas.height = this.phone.screenHeight;
    //   } else {
    //     this.asideStyle = {
    //       width: "850px",
    //     };

    //     this.asideMiddleStyle = {
    //       "margin-top": "0px",
    //       "margin-bottom": "0px",
    //       "padding-left": "unset",
    //       "padding-right": "unset",
    //       height: "400px",
    //     };

    //     this.canvas.width = this.phone.screenHeight;
    //     this.canvas.height = this.phone.screenWidth;
    //   }
    // },
  },

  created: function () {},

  computed: {},

  mounted: function () {
    this.canvas.element = this.$refs.cv;
    this.canvas.element.addEventListener("mousedown", this.mouseDownListener);
    this.canvas.ctx = this.canvas.element.getContext("2d");
    this.canvas.ready = true;
    this.initWS();
  },

  updated: function () {
  },

  methods: {
    initWS: function () {
      this.ws.instance = new WebSocket(this.ws.url + "/ios/" + this.phone.serial);

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

    handleMsg: function (msg) {
      let type = msg.type;

      if (type === 'iosInfoMsg') {
        this.phone.screenWidth = msg.realX;
        this.phone.screenHeight = msg.realY;
        this.phone.rotation = msg.rotation;
        return;
      }

      if (type === 'initMsg') {
        this.phone.ready = msg.ready;

        if (msg.type === false) {
          this.$message.error(msg.msg);
        }

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
    touchDown: function (x, y) {
      let scaled = this.coords(
        this.screen.bounds.w,
        this.screen.bounds.h,
        x,
        y,
        this.phone.rotation
      );

      this.mouseDownPoint.x = (scaled.xP * this.phone.screenWidth).toFixed(0);
      this.mouseDownPoint.y = (scaled.yP * this.phone.screenHeight).toFixed(0);
      this.mouseDownPoint.t = (new Date().getTime())/1000; 
      this.mouseMovePoint.x = this.mouseDownPoint.x;
      this.mouseMovePoint.y = this.mouseDownPoint.y;
      this.mouseMovePoint.t = this.mouseDownPoint.t;
      this.fromPoint = this.mouseDownPoint;
      this.toPoint = this.mouseDownPoint;
    },

    touchClick(x, y) {
      let scaled = this.coords(
        this.screen.bounds.w,
        this.screen.bounds.h,
        x,
        y,
        this.phone.rotation
      );

      let msg = {
          'type': 'iosTouchMsg',
          'action': 'mouseClick',
          'x': (scaled.xP * this.phone.screenWidth).toFixed(0),
          'y': (scaled.yP * this.phone.screenHeight).toFixed(0),
      }

      this.ws.instance.send(JSON.stringify(msg));
      console.log('mouse click', msg.x, msg.y);
    },

    // 鼠标move
    touchMove: function (x, y) {
      let scaled = this.coords(
        this.screen.bounds.w,
        this.screen.bounds.h,
        x,
        y,
        this.phone.rotation
      );

      let sx = (scaled.xP * this.phone.screenWidth).toFixed(0);
      let sy = (scaled.yP * this.phone.screenHeight).toFixed(0);
      let st = (new Date().getTime())/1000;

      console.log('mouse move...', sx, sy, st);

      if (st - this.toPoint.t < 0.8) {
        this.toPoint = {
          'x': sx,
          'y': sy,
          't': st
        }
      } else {
        let msg = {
          'type': 'iosTouchMsg',
          'action': 'mouseMove',
          'fromX': this.fromPoint.x,
          'fromY': this.fromPoint.y,
          'toX': this.toPoint.x,
          'toY': this.toPoint.y,
          'duration': this.toPoint.t - this.fromPoint.t
        }

        console.log('mouse move from', this.fromPoint.x, this.fromPoint.y, ' to ', this.toPoint.x, this.toPoint.y);

        this.ws.instance.send(JSON.stringify(msg));

        this.fromPoint = {
          'x': sx,
          'y': sy,
          't': st
        };

        this.toPoint = {
          'x': sx,
          'y': sy,
          't': st
        }
      }

      // if (this.mouseMovePoint.x === sx && this.mouseMovePoint.y === sy) {
      //   this.mouseMovePoint.x = sx;
      //   this.mouseMovePoint.y = sy;
      //   this.mouseMovePoint.t = st; 
      //   return;
      // }

      // if (this.mouseMovePoint.x != sx && this.mouseMovePoint.y != sy) {
      //   this.mouseMovePoint.x = sx;
      //   this.mouseMovePoint.y = sy;
      //   this.mouseMovePoint.t = st; 

      //   let msg = {
      //     'type': 'iosTouchMsg',
      //     'action': 'mouseMove',
      //     'x': this.mouseMovePoint.x,
      //     'y': this.mouseMovePoint.y,
      //     'time': this.mouseMovePoint.t
      //   }

      //   this.ws.instance.send(JSON.stringify(msg));
      //   console.log('mouse move...', sx, sy);
      // }
    },
   
    // 鼠标up
    touchUp: function (x, y) {
       let scaled = this.coords(
        this.screen.bounds.w,
        this.screen.bounds.h,
        x,
        y,
        this.phone.rotation
      );

      let sx = (scaled.xP * this.phone.screenWidth).toFixed(0);
      let sy = (scaled.yP * this.phone.screenHeight).toFixed(0);
      let st = (new Date().getTime())/1000;

      if (sx <= this.phone.realX && sy <= this.phone.realY) {
        this.toPoint = {
          'x': sx,
          'y': sy,
          't': st
        }
      }

      if (this.fromPoint.x != this.toPoint.x || this.fromPoint.y != this.toPoint.y) {
        let msg = {
          'type': 'iosTouchMsg',
          'action': 'mouseMove',
          'fromX': this.fromPoint.x,
          'fromY': this.fromPoint.y,
          'toX': this.toPoint.x,
          'toY': this.toPoint.y,
          'duration': this.toPoint.t - this.fromPoint.t
        }

        console.log('mouse move from', this.fromPoint.x, this.fromPoint.y, ' to ', this.toPoint.x, this.toPoint.y);

        this.ws.instance.send(JSON.stringify(msg));
      }

      this.mouseDownPoint = {
        'x': -1,
        'y': -1,
        't': -1
      };

      this.fromPoint = {
        'x': -1,
        'y': -1,
        't': -1
      };

      this.toPoint = {
        'x': -1,
        'y': -1,
        't': -1
      };
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
      this.mouseDownPoint.x = x;
      this.mouseDownPoint.y = y;
      this.mouseDownPoint.t = (new Date().getTime())/1000;
      this.touchDown(x, y);

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
      this.calculateBounds();
      let x = e.pageX - this.screen.bounds.x;
      let y = e.pageY - this.screen.bounds.y;

      this.touchMove(x, y);
    },

    // 监听鼠标up
    mouseUpListener: function (event) {
      let e = event;

      if (e.originalEvent) {
        e = e.originalEvent;
      }

      e.preventDefault();
      this.calculateBounds();

      let x = e.pageX - this.screen.bounds.x;
      let y = e.pageY - this.screen.bounds.y;

      if ((x === this.mouseDownPoint.x && y === this.mouseDownPoint.y) || new Date().getTime() - 1000 * this.mouseDownPoint.t < 200) { // click
        this.touchClick(x, y);
      } else { // move
        this.touchUp(x, y);
      }

      this.stopMousing();
    },

    stopMousing() {
      this.canvas.element.removeEventListener("mousemove", this.mouseMoveListener);
      document.removeEventListener("mouseup", this.mouseUpListener);
    },

    simulateHomeScreen() {
      let msg = {
        'type': 'iosTouchMsg',
        'action': 'homeScreen'
      };

      this.ws.instance.send(JSON.stringify(msg));
    }
  },
};
</script>

<style scoped>
#asideTop {
  background: whitesmoke;
  display: table-cell;
  vertical-align: middle;
  height: 5vh;
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

 #asideBottom {
  display: flex;
  justify-content: space-around;
  background: whitesmoke;
  height: 8vh;
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

/* .el-main {
  margin-left: 2px;
  background-color: #e9eef3;
  color: #333;
  text-align: center;
} */
</style>