<template>
  <div>
    <el-col>
      <el-row type="flex" style="margin-bottom: 20px">
        <i class="el-icon-top" @click="goToParentDir"></i>
        <span style="margin-left: 20px">{{ currentPath }}</span>
      </el-row>
      <el-row>
        <el-table :data="files" style="width: 100%">
          <el-table-column
            label="名称"
            width="380"
            @click="click(scope.row.name)"
          >
            <template slot-scope="scope">
              <div
                @click="click(scope.row.name, scope.row.right.startsWith('d'))"
              >
                <i
                  v-if="scope.row.right.startsWith('d')"
                  class="el-icon-folder"
                ></i>
                <i v-else class="el-icon-document"></i>
                <span style="margin-left: 10px">{{ scope.row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="size" label="大小" width="180">
          </el-table-column>
          <el-table-column prop="date" label="日期" width="280">
          </el-table-column>
          <el-table-column prop="right" label="权限"> </el-table-column>
        </el-table>
      </el-row>
    </el-col>
  </div>
</template>

<script>
export default {
  name: "AndroidFileSys",

  data() {
    return {
      files: [],
      currentPath: "/",
      cmdWS: null,
    };
  },

  mounted: function () {
    // this.initCmdWS();
  },

  methods: {
    initCmdWS: function () {
      if (!this.cmdWS) {
        this.cmdWS = new WebSocket("ws://localhost:5200/cmd/Q8WDU15817006617");
      }

      let that = this;

      let stringFiles = "";

      this.cmdWS.onopen = () => {
        that.getFiles("/");
      };

      this.cmdWS.onclose = () => {};

      this.cmdWS.onerror = () => {};

      this.cmdWS.onmessage = (event) => {
        if ("EOF" != event.data) {
          stringFiles = stringFiles + event.data;
        } else {
          that.files = that.parseFiles(stringFiles);
          stringFiles = "";
        }
      };
    },

    parseFiles(stringFiles) {
      let files = new Array();

      if (stringFiles && stringFiles != "") {
        let fileItems = stringFiles.split("\n");

        for (const [i, v] of fileItems.entries()) {
          if (v === "") {
            break;
          }

          let fileInfo = v.split(/\s+/);
          let size = fileInfo.length;
          let name = "";

          if (v.substring(0, 1) === "d" || v.substring(0, 1) === "l") {
            files[i] = {
              size: "",
              date: fileInfo[3] + " " + fileInfo[4],
              right: fileInfo[0],
            };

            for (let i = 5; i < size; i++) {
              name = name + " " + fileInfo[i];
            }

            files[i].name = name;
          } else {
            files[i] = {
              size: fileInfo[3],
              date: fileInfo[4] + " " + fileInfo[5],
              right: fileInfo[0],
            };

            for (let i = 6; i < size; i++) {
              name = name + " " + fileInfo[i];
            }

            files[i].name = name;
          }
        }
      }

      return files;
    },

    getFiles(path) {
      this.cmdWS.send("su -c ls -l " + path.trim());
    },

    click(name, isDir) {
      if (false === isDir) {
        return;
      }

      if ("/" === this.currentPath) {
        this.currentPath = this.currentPath + name.trim();
      } else {
        this.currentPath = this.currentPath + "/" + name.trim();
      }

      this.getFiles(this.currentPath.trim());
    },

    goToParentDir() {
      if ("/" != this.currentPath) {
        let index = this.currentPath.lastIndexOf("/");

        if (0 != index) {
          this.currentPath = this.currentPath.substring(0, index);
        } else {
          this.currentPath = "/";
        }

        this.getFiles(this.currentPath);
      }
    },
  },
};
</script>

<style scoped>
.el-table {
  font-size: medium !important;
}
</style>