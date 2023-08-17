<template>
	<div>
		<el-form :inline="true" :model="dataForm" :rules="dataRule" ref="dataForm">
			<el-form-item prop="name">
				<el-input
					v-model="dataForm.name"
					class="input"
					placeholder="Name"
					size="medium"
					:disabled="!isAuth(['ROOT', 'LEAVE:SELECT'])"
					clearable="clearable"
				></el-input>
			</el-form-item>
			<el-form-item>
				<el-select
					v-model="dataForm.deptId"
					class="input"
					placeholder="Department"
					size="medium"
					:disabled="!isAuth(['ROOT', 'LEAVE:SELECT'])"
					clearable="clearable"
				>
					<el-option v-for="one in deptList" :key="one.id" :label="one.deptName" :value="one.id" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-date-picker
					v-model="dataForm.date"
					style="width: 160px;"
					type="date"
					size="medium"
					placeholder="Leave Date"
					clearable="clearable"
				></el-date-picker>
			</el-form-item>
			<el-form-item>
				<el-select v-model="dataForm.type" class="input" placeholder="Type" size="medium" clearable="clearable">
					<el-option label="Sick" value="1"></el-option>
					<el-option label="Personal" value="2"></el-option>
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-select
					v-model="dataForm.status"
					class="input"
					placeholder="Status"
					size="medium"
					clearable="clearable"
				>
					<el-option label="Awaiting" value="1"></el-option>
					<el-option label="Not Approved" value="2"></el-option>
					<el-option label="Approved" value="3"></el-option>
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-button @click="searchHandle()" type="primary" size="medium">Search</el-button>
				<el-button type="danger" size="medium" @click="addHandle()">Request to leave</el-button>
			</el-form-item>
		</el-form>
		<el-table
			:data="dataList"
			border="border"
			v-loading="dataListLoading"
			cell-style="padding: 4px 0"
			style="width: 100%;"
			size="medium"
		>
			<el-table-column width="40px" prop="reason" header-align="center" align="center" type="expand">
				<template #default="scope">
					Reason to leave:{{ scope.row.reason }}
				</template>
			</el-table-column>
			<el-table-column type="index" header-align="center" align="center" width="100" label="No.">
				<template #default="scope">
					<span>{{ (pageIndex - 1) * pageSize + scope.$index + 1 }}</span>
				</template>
			</el-table-column>
			<el-table-column prop="name" header-align="center" align="center" label="Name" min-width="150" ></el-table-column>
			<el-table-column prop="deptName" header-align="center" align="center" label="Department" min-width="150"></el-table-column>
			<el-table-column prop="start" header-align="center" align="center" label="Start" min-width="180"></el-table-column>
			<el-table-column prop="end" header-align="center" align="center" label="End" min-width="180"></el-table-column>
			<el-table-column prop="type" header-align="center" align="center" label="Leave Type" min-width="150"></el-table-column>

			<el-table-column prop="status" header-align="center" align="center" label="Leave Status" min-width="120">
				<template #default="scope">
					<span v-if="scope.row.status == 'Awaiting to be Approve'" style="color: orange;">{{ scope.row.status }}</span>
					<span v-if="scope.row.status == 'Approved'" style="color: #17B3A3;">{{ scope.row.status }}</span>
					<span v-if="scope.row.status == 'Not Approved'" style="color: #f56c6c;">{{ scope.row.status }}</span>
				</template>
			</el-table-column>
			<el-table-column header-align="center" align="center" width="120" label="Leave Form" min-width="120">
				<template #default="scope">
					<el-button
						type="text"
						size="medium"
						v-if="scope.row.status == 'Approved'"
						@click="pdfHandle(scope.row.id)"
					>
						Leave Form
					</el-button>
				</template>
			</el-table-column>
			<el-table-column header-align="center" align="center" width="150" label="Action" min-width="120">
				<template #default="scope">
					<el-button
						type="text"
						size="medium"
						:disabled="scope.row.status == 'Approved' || scope.row.mine != true"
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
		<leave-add v-if="addVisible" ref="add" @refreshDataList="loadDataList"></leave-add>
		<leave-pdf v-if="pdfVisible" ref="pdf" @refreshDataList="loadDataList"></leave-pdf>
	</div>
</template>

<script>
import dayjs from 'dayjs';
import LeaveAdd from './leave-add.vue';
import LeavePdf from './leave_pdf.vue';
export default {
	components: { LeaveAdd, LeavePdf },
	data: function() {
		return {
			pageIndex: 1,
			pageSize: 10,
			totalPage: 0,
			dataListLoading: false,
			dataListSelections: [],
			dataForm: {
				name: null,
				deptId: null,
				date: null,
				type: null,
				status: null
			},
			dataList: [],
			deptList: [],
			addVisible: false,
			pdfVisible: false,
			dataRule: {
				name: [{ required: false, pattern: '^[\u4e00-\u9fa5]{1,10}$', message: 'Wrong name format' }]
			}
		};
	},
	methods: {
		loadDeptList: function() {
			let that = this;
			that.$http('dept/searchAllDept', 'GET', null, true, function(resp) {
				that.deptList = resp.list;
			});
		},
    loadDataList: function () {
      let that = this;
      //设置滚动进度条
      that.dataListLoading = true;
      //模型层
      let data = {
        name: that.dataForm.name,
        deptId: that.dataForm.deptId,
        date: that.dataForm.date,
        type: that.dataForm.type,
        status: that.dataForm.status,
        page: that.pageIndex,
        length: that.pageSize
      };
      if (that.dataForm.date != null && that.dataForm.date != "") {
        //日期对象转换成日期字符串，塞到data.date对象上
        data.date = dayjs(that.dataForm.date).format('YYYY-MM-DD');
      }
      //发送ajax
      that.$http("leave/searchLeaveByPage", "POST", data, true, function(resp) {
          let page = resp.page;
          for (let one of page.list) {
            if (one.type == 1) {
              one.type = 'Sick Leave';
            } else if (one.type == 2) {
              one.type = 'Personal Leave';
            }
            if (one.status == 1) {
              one.status = 'Awaiting to be Approve';
            } else if (one.status == 2) {
              one.status = 'Not Approved';
            } else if (one.status == 3) {
              one.status = 'Approved';
            }
          }
          //变量赋值
          that.dataList = page.list;
          that.totalCount = page.totalCount;
          //隐藏进度条
          that.dataListLoading = false;
      });
    },
    //分页的两个函数
    sizeChangeHandle: function(val) {
      this.pageSize = val;
      this.pageIndex = 1;
      this.loadDataList();
    },
    currentChangeHandle: function(val) {
      this.pageIndex = val;
      this.loadDataList();
    },
    //有条件查询搜索条件回调函数
    searchHandle: function() {
      this.$refs['dataForm'].validate(valid => {
        if (valid) {
          this.$refs["dataForm"].clearValidate();
          if (this.dataForm.name == "") {
              this.dataForm.name = null;
          }
          if (this.pageIndex != 1) {
            this.pageIndex = 1;
          }
          //调用封装函数
          this.loadDataList();
        } else {
          return false;
        }
      });
    },
    addHandle: function() {
      this.addVisible = true;
      this.$nextTick(() => {
        this.$refs.add.init();
      });
    },
    deleteHandle: function (id) {
        let that = this;
        that.$confirm(`Confirm to deleve selected record?`, 'Tips', {
          confirmButtonText:'Confirm',
          cancelButtonText:'Cancel',
          type:'warning'
        }).then(() => {
          let data = {
            id: id
          }
          //发送http请求
          that.$http("leave/deleteLeaveById","POST", data, true, function (resp) {
            if(resp.rows > 0) {
              that.$message({
                message:'Success',
                type:'success',
                duration:1200
              });
              that.loadDataList();
            } else {
              that.$message({
                message:'Unable to delete',
                type:'warning',
                duration:1200
              });
            }
          });
        });
    },
    pdfHandle: function(id) {
      this.pdfVisible = true;
      this.$nextTick(() => {
        this.$refs.pdf.init(id);
      });
    }
	},
	created: function() {
		this.loadDeptList();
    this.loadDataList();
	}
};
</script>

<style lang="less" scoped=""></style>
