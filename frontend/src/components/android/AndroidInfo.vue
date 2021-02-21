<template>
  <div>
    <el-row :gutter="12">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="el-card-info">
            <div>物理设备</div>
            <table>
              <tr>
                <td>位置</td>
                <td>ubeanos</td>
              </tr>
            </table>
          </div>
        </el-card>
        <el-card shadow="hover">
          <div class="el-card-info">
            <div>SIM</div>
            <table>
              <tr>
                <td>信号</td>
                <td>ubeanos</td>
              </tr>
              <tr>
                <td>网络</td>
                <td>{{ SIMInfo.network }}</td>
              </tr>
              <tr>
                <td>数字</td>
                <td>ubeanos</td>
              </tr>
              <tr>
                <td>IMEI</td>
                <td>{{ SIMInfo.imei }}</td>
              </tr>
              <tr>
                <td>IMSI</td>
                <td>{{ SIMInfo.imsi }}</td>
              </tr>
              <tr>
                <td>集成电路识别码</td>
                <td>{{ SIMInfo.iccid }}</td>
              </tr>
            </table>
          </div>
        </el-card>
        <el-card shadow="hover">
          <div class="el-card-info">
            <div>CPU</div>
            <table>
              <tr>
                <td>名称</td>
                <td>ubeanos</td>
              </tr>
              <tr>
                <td>核</td>
                <td>ubeanos</td>
              </tr>
              <tr>
                <td>频率</td>
                <td>ubeanos</td>
              </tr>
            </table>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="el-card-info">
            <div>电池</div>
            <table>
              <tr>
                <td>健康</td>
                <td>{{ batteryInfo.health }}</td>
              </tr>
              <tr>
                <td>电源来源</td>
                <td>{{ batteryInfo.source }}</td>
              </tr>
              <tr>
                <td>状态</td>
                <td>{{ batteryInfo.status }}</td>
              </tr>
              <tr>
                <td>等级</td>
                <td>{{ batteryInfo.level }}</td>
              </tr>
              <tr>
                <td>温度</td>
                <td>{{ batteryInfo.temp }}</td>
              </tr>
              <tr>
                <td>电压</td>
                <td>{{ batteryInfo.voltage }}</td>
              </tr>
            </table>
          </div>
        </el-card>
        <el-card shadow="hover">
          <div class="el-card-info">
            <div>硬件</div>
            <table>
              <tr>
                <td>制造商</td>
                <td>ubeanos</td>
              </tr>
              <tr>
                <td>产品</td>
                <td>ubeanos</td>
              </tr>
              <tr>
                <td>型号</td>
                <td>ubeanos</td>
              </tr>
              <tr>
                <td>串行</td>
                <td>ubeanos</td>
              </tr>
              <tr>
                <td>释放</td>
                <td>ubeanos</td>
              </tr>
            </table>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="el-card-info">
            <div>Display</div>
            <table>
              <tr>
                <td>大小</td>
                <td>ubeanos</td>
              </tr>
              <tr>
                <td>密度</td>
                <td>{{ displayInfo.density }}</td>
              </tr>
              <tr>
                <td>FPS</td>
                <td>{{ displayInfo.fps }}</td>
              </tr>
              <tr>
                <td>跨度</td>
                <td>{{ displayInfo.width }}</td>
              </tr>
              <tr>
                <td>高度</td>
                <td>{{ displayInfo.height }}</td>
              </tr>
              <tr>
                <td>ID</td>
                <td>ubeanos</td>
              </tr>
              <tr>
                <td>屏幕方向</td>
                <td>{{ displayInfo.rotation }}</td>
              </tr>
              <tr>
                <td>是否加密</td>
                <td>{{ displayInfo.secure }}</td>
              </tr>
              <tr>
                <td>X DPI</td>
                <td>{{ displayInfo.xdpi }}</td>
              </tr>
              <tr>
                <td>Y DPI</td>
                <td>{{ displayInfo.ydpi }}</td>
              </tr>
            </table>
          </div>
        </el-card>
        <el-card shadow="hover">
          <div class="el-card-info">
            <div>内存</div>
            <table>
              <tr>
                <td>RAM</td>
                <td>ubeanos</td>
              </tr>
              <tr>
                <td>ROM</td>
                <td>ubeanos</td>
              </tr>
              <tr>
                <td>SD Card Mounted</td>
                <td>ubeanos</td>
              </tr>
            </table>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="el-card-info">
            <div>网络</div>
            <table>
              <tr>
                <td>已连接</td>
                <td>{{ networkInfo.connected }}</td>
              </tr>
              <tr>
                <td>飞行模式</td>
                <td>ubeanos</td>
              </tr>
              <tr>
                <td>使用回退</td>
                <td>{{ networkInfo.failover }}</td>
              </tr>
              <tr>
                <td>漫游状态</td>
                <td>{{ networkInfo.roaming }}</td>
              </tr>
              <tr>
                <td>类型</td>
                <td>{{ networkInfo.networkType }}</td>
              </tr>
              <tr>
                <td>子类型</td>
                <td>{{ networkInfo.subNetworkType }}</td>
              </tr>
            </table>
          </div>
        </el-card>
        <el-card shadow="hover">
          <div class="el-card-info">
            <div>平台</div>
            <table>
              <tr>
                <td>操作系统</td>
                <td>ubeanos</td>
              </tr>
              <tr>
                <td>版本</td>
                <td>ubeanos</td>
              </tr>
              <tr>
                <td>SDK</td>
                <td>ubeanos</td>
              </tr>
              <tr>
                <td>ABI</td>
                <td>ubeanos</td>
              </tr>
            </table>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "AndroidInfo",

  props: {
    batteryInfo: {
      type: Object,
      default: function () {
        return {
          health: null,

          level: null,

          scale: null,

          source: null,

          status: null,

          temp: null,

          voltage: null,
        };
      },
    },

    displayInfo: {
      type: Object,
      default: function () {
        return {
          success: null,

          width: null,

          height: null,

          xdpi: null,

          ydpi: null,

          fps: null,

          density: null,

          rotation: null,

          secure: null,

          status: null,

          mounted: null,
        };
      },
    },

    SIMInfo: {
      type: Object,
      default: function () {
        return {
          imei: null,

          network: null,

          imsi: null,

          phoneNumber: null,

          iccid: null,

          operator: null,
        };
      },
    },

    networkInfo: {
      type: Object,

      default: function () {
        return {
          connected: null,

          airPlaneMode: null,

          failover: null,

          roaming: null,

          networkType: null,

          subNetworkType: null,
        };
      },
    },
  },
};
</script>

<style scoped>
</style>