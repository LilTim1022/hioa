<template>
	<div>
		<el-form :inline="true" :model="dataForm" :rules="dataRule" ref="dataForm">
			<el-form-item prop="name">
				<el-input
					v-model="dataForm.name"
					placeholder="Name"
					size="medium"
					class="input"
					clearable="clearable"
				/>
			</el-form-item>
			<el-form-item>
				<el-select
					v-model="dataForm.deptId"
					class="input"
					placeholder="Department"
					size="medium"
					clearable="clearable"
				>
					<el-option v-for="one in deptList" :label="one.deptName" :value="one.id" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-select
					v-model="dataForm.typeId"
					class="input"
					placeholder="Type"
					size="medium"
					clearable="clearable"
				>
					<el-option label="Ordinary Reimbursement" value="1" />
					<el-option label="Travel Reimbursement" value="2" />
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
					<el-option label="Awaiting" value="1" />
					<el-option label="Declined" value="2" />
					<el-option label="Approved" value="3" />
					<el-option label="Archived" value="4" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-date-picker
					v-model="dataForm.date"
					type="daterange"
					range-separator="~"
					start-placeholder="Start Date"
					end-placeholder="End Date"
					size="medium"
				></el-date-picker>
			</el-form-item>
			<el-form-item>
				<el-button size="medium" type="primary" @click="searchHandle()">Search</el-button>
				<el-button size="medium" type="primary" @click="addHandle()">Add</el-button>
			</el-form-item>
		</el-form>
		<el-table
			:data="dataList"
			border
			v-loading="dataListLoading"
			cell-style="padding: 4px 0"
			style="width: 100%;"
			size="medium"
		>
			<el-table-column
				width="40px"
				prop="content"
				header-align="center"
				align="center"
				type="expand"
			>
				<template #default="scope">
					<div class="reim-table">
						<div class="row">
							<div class="title">No</div>
							<div class="title">Reimbursement Type</div>
							<div class="title">Reimbursement Items</div>
							<div class="title">Remarks</div>
							<div class="title">Reimbursement Amount</div>
						</div>
						<div class="row" v-for="(one, $index) in scope.row.content">
							<div class="col">{{ $index + 1 }}</div>
							<div class="col">{{ one.type }}</div>
							<div class="col">{{ one.title }}</div>
							<div class="col">{{ one.desc }}</div>
							<div class="col">￥{{ one.money }}</div>
						</div>
					</div>
				</template>
			</el-table-column>
			<el-table-column
				type="index"
				header-align="center"
				align="center"
				width="100"
				label="No."
			>
				<template #default="scope">
					<span>{{ (pageIndex - 1) * pageSize + scope.$index + 1 }}</span>
				</template>
			</el-table-column>
			<el-table-column
				prop="type"
				header-align="center"
				align="center"
				label="Reimbursement Type"
				min-width="150"
			/>
			<el-table-column
				prop="name"
				header-align="center"
				align="center"
				label="Applier"
				min-width="150"
			/>
			<el-table-column
				prop="deptName"
				header-align="center"
				align="center"
				label="Department"
				width="150"
			/>
			<el-table-column header-align="center" align="center" label="Reimbursement Amount" min-width="120">
				<template #default="scope">
					<span>{{ scope.row.amount }}元</span>
				</template>
			</el-table-column>
			<el-table-column header-align="center" align="center" label="Loan Amount:" min-width="120">
				<template #default="scope">
					<span>{{ scope.row.anleihen }}元</span>
				</template>
			</el-table-column>
			<el-table-column header-align="center" align="center" label="Actual Reimbursement" min-width="120">
				<template #default="scope">
					<span>{{ scope.row.balance }}元</span>
				</template>
			</el-table-column>
			<el-table-column
				prop="status"
				header-align="center"
				align="center"
				label="Status"
				min-width="100"
			/>
			<el-table-column
				prop="createTime"
				header-align="center"
				align="center"
				label="Apply Time"
				width="150"
			/>
			<el-table-column header-align="center" align="center" min-width="150" label="Action">
				<template #default="scope">
					<el-button 
						type="text" 
						size="medium"
						@click="pdfHandle(scope.row.id)">
            Reimbursement Foam
					</el-button>
					<el-button
						type="text"
						size="medium"
						:disabled="!(['Awaiting', 'Declined'].includes(scope.row.status) && scope.row.mine == 'true')"
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
	</div>
	<add v-if="addVisible" ref="add" @refreshDataList="loadDataList"></add>
	<reim-pdf v-if="pdfVisible" ref="pdf" @refreshDataList="loadDataList"></reim-pdf>
</template>

<script>
import Add from './reim-add.vue';
import dayjs from 'dayjs';
import ReimPdf from './reim_pdf.vue';
export default {
	components: { Add, ReimPdf },
	data: function() {
		return {
			dataForm: {
				name: null,
				deptId: null,
				status: null,
				typeId: null,
				date: null
			},
			deptList: [],
			dataList: [],
			pageIndex: 1,
			pageSize: 10,
			totalCount: 0,
			dataListLoading: false,
			dataRule: {
				name: [
					{ required: false, pattern: '^[\u4e00-\u9fa5]{1,10}$', message: '姓名格式错误' }
				]
			},
			addVisible: false,
			pdfVisible: false
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
      that.dataListLoading = true;
      //声明要提交的json数据
      let data = {
        name: that.dataForm.name,
        deptId: that.dataForm.deptId,
        typeId: that.dataForm.typeId,
        status: that.dataForm.status,
        page: that.pageIndex,
        length: that.pageSize
      };
      if (that.dataForm.date != null && that.dataForm.date.length == 2) {
        // 取出日期对象
        let startDate = that.dataForm.date[0];
        let endDate = that.dataForm.date[1];
        //转换成日期字符串，保存在data里
        data.startDate = dayjs(startDate).format('YYYY-MM-DD');
        data.endDate = dayjs(endDate).format('YYYY-MM-DD');
      }
      //发起ajax请求
      that.$http("reim/searchReimByPage", "POST", data, true, function (resp) {
        let page = resp.page;
        let status = {
          1: 'Awaiting',
          2: 'Declined',
          3: 'Approved',
          4: 'Archived'
        };
        let type = {1:'Ordinary Reimbursement', 2:'Travel Reimbursement'};
        for (let one of page.list) {
          //传入的是数字，把数字的value取出来，对status进行赋值，
          one.status = status[one.status];
          one.type = status[one.typeId];
          //对json对象进行赋值
          one.content = JSON.parse(one.content);
        }
        //对模型层的datalist数组进行赋值
        that.dataList = page.list;
        that.totalCount = page.totalCount;
        that.dataListLoading = false;
      });
    },
    //分页函数
    sizeChangeHandle: function(val) {
      this.pageSize = val;
      this.pageIndex = 1;
      this.loadDataList();
    },
    currentChangeHandle: function(val) {
      this.pageIndex = val;
      this.loadDataList();
    },
    searchHandle: function () {
      //表单验证
      this.$refs['dataForm'].validate(valid => {
        if (valid) {
          this.$refs['dataForm'].clearValidate();
          //判断文本框数据是否为空
          if (this.dataForm.name == "") {
            this.dataForm.name = null;
          }
          //判断页数是否为第一页
          if (this.pageIndex != 1) {
            this.pageIndex = 1;
          }
          //加载数据
          this.loadDataList();
        }
        else {
          return false;
        }
      });
    },
    addHandle: function () {
      this.addVisible = true;
      this.$nextTick(() => {
        this.$refs.add.init();
      });
    },
    pdfHandle: function (id) {
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

<style lang="less" scoped="scoped">
@import url('reim.less');
</style>
