<template>
  <div>
    <el-row>
      <el-col :span="4" class="row-left">
        <el-row type="flex" align="middle">
          <div class="camera">
            <i class="el-icon-camera"></i>
            <span>屏幕截图</span>
          </div>
        </el-row>
      </el-col>
      <el-col :span="10" :offset="10">
        <el-row type="flex" align="middle">
          <el-col :span="2">
            <el-button
              type="primary"
              icon="el-icon-minus"
              size="mini"
              circle
            ></el-button>
          </el-col>
          <el-col :span="16">
            <el-slider v-model="value2"></el-slider>
          </el-col>
          <el-col :span="2">
            <el-button
              type="primary"
              icon="el-icon-plus"
              size="mini"
              circle
            ></el-button>
          </el-col>
          <el-col :span="4">
            <div class="delete">
              <i class="el-icon-delete"></i>
              <span>清除</span>
            </div>
          </el-col>
        </el-row>
      </el-col>
    </el-row>
    <el-row>
      <div>
        <canvas
          id="cv"
          v-bind:img="img.src"
          v-bind:width="img.width"
          v-bind:height="img.height"
        ></canvas>
      </div>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "AndroidScreenShot",

  data() {
    return {
      value2: 20,
      img: {
        src: "../assets/android/android.jpeg",
        width: 400,
        height: 600,
      },
    };
  },
};
</script>

<style scoped>
.row-left {
  text-align: left;
  padding-left: 5px;
}

.el-icon-camera {
  margin-right: 5px;
}

.el-icon-delete {
  margin-right: 5px;
}

.camera {
  padding: 5px;
  border-radius: 5px;
  color: #7ca7dd;
}

.camera:hover {
  background: #167ffc;
  cursor: pointer;
}

.delete {
  padding: 5px;
  border-radius: 5px;
  color: #b80943;
}

.delete:hover {
  background: rgb(248, 49, 49);
  cursor: pointer;
}
</style>