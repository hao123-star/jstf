<template>
<div class="phone-list">
    <el-row>
        <div>
            <el-checkbox v-model="appleChecked">Apple</el-checkbox>
            <el-checkbox v-model="androidChecked">Android</el-checkbox>
        </div>
    </el-row>
    <el-row>
        <el-table
            :data="phones"
            style="width: 100%">
            <el-table-column
                prop="brand"
                label="品牌"
                width="180">
            </el-table-column>
            <el-table-column
                prop="system"
                label="名称">
            </el-table-column>
            <el-table-column
                prop="system"
                label="系统">
            </el-table-column>
            <el-table-column
                prop="version"
                label="版本">
            </el-table-column>
             <el-table-column
                prop="used"
                label="空闲?">
            </el-table-column>
            <el-table-column
                fixed="right"
                label="操作"
                width="100">
                <template slot-scope="scope">
                    <el-button @click="usePhone(scope.row)" type="text" size="small">使用</el-button>
                </template>
             </el-table-column>
        </el-table>
        <div class="pageination">
            <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleIndexChange"
                :current-page.sync="page.index"
                :page-sizes="[10, 20, 30, 40]"
                :page-size="page.size"
                layout="sizes, prev, pager, next"
                :total="page.total">
            </el-pagination>
        </div>
    </el-row>
</div>
</template>

<script>
export default {
    name: 'Phones',

    data() {
        return {
            'appleChecked': false,
            'androidChecked': false,
            'phones': [
                {
                    'brand': '华为',
                    'name': 'android',
                    'system': 'android',
                    'version': '10.1',
                    'used': '否'
                },
                {
                    'brand': '苹果',
                    'name': 'iphone',
                    'system': 'ios',
                    'version': '10.1',
                    'used': '否'
                }
            ],

            'page': {
                total: 10,
                size: 1,
                index: 1
            }
        }
    },

    methods: {
        usePhone: function(phone) {
            if (phone.system === 'android') {
                this.$router.push({'name': 'androidPhone'})
            } else {
                this.$router.push({'name': 'iosPhone'})
            }
        },

        handleSizeChange: function() {

        },

        handleIndexChange: function() {

        }

    }

}
</script>

<style scoped>
.phone-list {
    margin-left: 5%;

}

.phone-list > div:nth-child(1) {
    margin-bottom: 20px;
}

.pageination {
    text-align: center;
    margin-top: 20px;

}

</style>