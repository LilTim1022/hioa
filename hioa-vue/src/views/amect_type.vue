<template>
	<div v-if="isAuth(['ROOT'])">
		<el-form :inline="true" :model="dataForm" :rules="dataRule" ref="dataForm">
			<el-form-item prop="type">
				<el-input
					v-model="dataForm.type"
					placeholder="Type Name"
					size="medium"
					class="input"
					clearable="clearable"
				/>
			</el-form-item>
			<el-form-item>
				<el-button size="medium" type="primary" @click="searchHandle()">Search</el-button>
				<el-button size="medium" type="primary" @click="addHandle()">Add</el-button>
				<el-button size="medium" type="danger" @click="deleteHandle()">Multi-Delete</el-button>
			</el-form-item>
		</el-form>
		<el-table
			:data="dataList"
			border
			v-loading="dataListLoading"
			@selection-change="selectionChangeHandle"
			cell-style="padding: 4px 0"
			style="width: 100%;"
			size="medium"
		>
			<el-table-column
				type="selection"
				:selectable="selectable"
				header-align="center"
				align="center"
				width="50"
			/>
			<el-table-column type="index" header-align="center" align="center" width="100" label="No.">
				<template #default="scope">
					<span>{{ (pageIndex - 1) * pageSize + scope.$index + 1 }}</span>
				</template>
			</el-table-column>
			<el-table-column prop="type" header-align="center" align="center" label="Fine Type" />
			<el-table-column header-align="center" align="center" label="Fine Value">
				<template #default="scope">
					<span>{{ scope.row.money }}CNY</span>
				</template>
			</el-table-column>
			<el-table-column header-align="center" align="center" label="Integrated?">
				<template #default="scope">
					<span>{{ scope.row.systemic ? 'Yes' : 'No' }}</span>
				</template>
			</el-table-column>
			<el-table-column header-align="center" align="center" label="Number of outstanding fines">
				<template #default="scope">
					<span>{{ scope.row.notPay == 0 ? '--' : scope.row.notPay + '' }}</span>
				</template>
			</el-table-column>
			<el-table-column fixed="right" header-align="center" align="center" width="150" label="Action">
				<template #default="scope">
					<el-button type="text" size="medium" @click="updateHandle(scope.row.id)">Modify</el-button>
					<el-button
						type="text"
						size="medium"
						:disabled="scope.row.canDelete == 'false'"
						@click="deleteHandle(scope.row.id)"
					>
						Delete
					</el-button>
				</template>
			</el-table-column>
		</el-table>
		<el-pagination
			@size-change="sizeChangeHandle"
			@current-change="currentChangeHandle"
			:current-page="pageIndex"
			:page-sizes="[10, 20, 50]"
			:page-size="pageSize"
			:total="totalCount"
			layout="total, sizes, prev, pager, next, jumper"
		></el-pagination>
		<add-or-update v-if="addOrUpdateVisible" ref="addOrUpdate" @refreshDataList="loadDataList"></add-or-update>
	</div>
</template>

<script>
import AddOrUpdate from './amect_type-add-or-update.vue';
export default {
	components: {
		AddOrUpdate
	},
	data: function() {
		return {
			dataForm: {
				type: null
			},
			dataList: [],
			pageIndex: 1,
			pageSize: 10,
			totalCount: 0,
			dataListLoading: false,
			dataListSelections: [],
			addOrUpdateVisible: false,
			dataRule: {
				type: [{ required: false, pattern: '^[a-zA-Z0-9\u4e00-\u9fa5]{1,10}$', message: '类型名称格式错误' }]
			}
		};
	},
	methods: {
		loadDataList: function () {
      let that = this;
      that.dataListLoading = true;
      let data = {
        type: that.dataForm.type,
        page: that.pageIndex,
        length: that.pageSize
      };
      //发送ajax请求
      that.$http('amect_type/searchAmectTypeByPage', 'POST', data, true,function (resp) {
        let page = resp.page;
        that.dataList = page.list;
        that.totalCount = page.totalCount;
        that.dataListLoading = false;
      });
    },
    //分页函数
    sizeChangeHandle: function (val) {
      this.pageSize = val;
      this.pageIndex = 1;
      this.loadDataList();
    },
    //记录显示记录数量函数
    currentChangeHandle: function (val) {
      this.pageIndex = val;
      this.loadDataList();
    },
    //有条件查询数据
    searchHandle: function () {
      this.$refs['dataForm'].validate(valid => {
        if (valid) {
          this.$refs['dataForm'].clearValidate();
          if (this.dataForm.type == '') {
            this.dataForm.type = null;
          }
          if (this.pageIndex != 1) {
            this.pageIndex = 1;
          }
          this.loadDataList();
        } else {
          return false;
        }
      });
    },
    //新增按钮的回调函数
    addHandle: function () {
      this.addOrUpdateVisible = true;
      this.$nextTick(() => {
        this.$refs.addOrUpdate.init();
      });
    },
    updateHandle: function (id) {
      this.addOrUpdateVisible = true;
      this.$nextTick(() => {
        this.$refs.addOrUpdate.init(id);
      });
    },
    selectionChangeHandle: function (val) {
      this.dataListSelections = val;
    },
    selectable: function (row, index) {
      if (row.canDelete == 'true') {
        return true;
      }
      return false;
    },
    deleteHandle: function (id) {
      let that = this;
      let ids = id
          ? [id]
          :that.dataListSelections.map(item => {
            return item.id;
          });
      if (ids.length == 0) {
        that.$message({
          message:'no record selected',
          type:'warning',
          duration:1200
        });
      } else {
        that.$confirm(`Are you sure you want to delete the selected records？`,'hint', {
          confirmButtonText:'Confirm',
          cancelButtonText:'Cancel',
          type:'warning'
        }).then(() => {
          //发起ajax请求
          that.$http(`amect_type/deleteAmectTypeByIds`, 'POST',{ids:ids}, true, function (resp) {
            if (resp.rows > 0) {
              that.$message({
                message:'success',
                type:'success',
                duration:1200,
                onClose:() => {
                  that.loadDataList();
                }
              });
            } else {
              that.$message({
                message: 'cant delete',
                type: 'warning',
                duration: 1200
              });
            }
          });
        });
      }
    }
	},
	created: function() {
    //自动加载第一页罚款类型记录
    this.loadDataList();
	}
};
</script>

<style lang="less" scoped="scoped"></style>
