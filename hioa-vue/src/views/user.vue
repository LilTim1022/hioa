<template>
	<div v-if="isAuth(['ROOT', 'USER:SELECT'])">
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
				<el-select v-model="dataForm.sex" class="input" placeholder="Gender" size="medium" clearable="clearable">
					<el-option label="Male" value="男" />
					<el-option label="Female" value="女" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-select v-model="dataForm.role" class="input" placeholder="Role" size="medium" clearable="clearable">
					<el-option v-for="one in roleList" :label="one.roleName" :value="one.roleName" />
				</el-select>
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
					v-model="dataForm.status"
					class="input"
					placeholder="Status"
					size="medium"
					clearable="clearable"
				>
					<el-option label="working" value="1" />
					<el-option label="left" value="2" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-button size="medium" type="primary" @click="searchHandle()">Search</el-button>
				<el-button
					size="medium"
					type="primary"
					:disabled="!isAuth(['ROOT', 'USER:INSERT'])"
					@click="addHandle()"
				>
					Add
				</el-button>
				<el-button
					size="medium"
					type="danger"
					:disabled="!isAuth(['ROOT', 'USER:DELETE'])"
					@click="deleteHandle()"
				>
					Multi-Delete
				</el-button>
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
			<el-table-column type="selection" header-align="center" align="center" width="50"/>
			<el-table-column type="index" header-align="center" align="center" width="100" label="No.">
				<template #default="scope">
					<span>{{ (pageIndex - 1) * pageSize + scope.$index + 1 }}</span>
				</template>
			</el-table-column>
			<el-table-column prop="name" header-align="center" align="center" min-width="100" label="Name" />
			<el-table-column prop="sex" header-align="center" align="center" min-width="60" label="Gender" />
			<el-table-column prop="tel" header-align="center" align="center" min-width="130" label="Tel" />
			<el-table-column
				prop="email"
				header-align="center"
				align="center"
				min-width="240"
				label="Email"
				:show-overflow-tooltip="true"
			/>
			<el-table-column prop="hiredate" header-align="center" align="center" min-width="130" label="Entry date" />
			<el-table-column
				prop="roles"
				header-align="center"
				align="center"
				min-width="150"
				label="Role"
				:show-overflow-tooltip="true"
			/>
			<el-table-column prop="dept" header-align="center" align="center" min-width="120" label="Dept" />
			<el-table-column prop="status" header-align="center" align="center" min-width="100" label="Status" />
			<el-table-column header-align="center" align="center" width="150" label="Action">
				<template #default="scope">
					<el-button
						type="text"
						size="medium"
						v-if="isAuth(['ROOT', 'USER:UPDATE'])"
						@click="updateHandle(scope.row.id)"
					>
						Modify
					</el-button>
					<el-button
						type="text"
						size="medium"
						v-if="isAuth(['ROOT', 'USER:UPDATE'])"
						:disabled="scope.row.status == '离职' || scope.row.root"
						@click="dimissHandle(scope.row.id)"
					>
						Sign Out
					</el-button>
					<el-button
						type="text"
						size="medium"
						:disabled="scope.row.root"
						v-if="isAuth(['ROOT', 'USER:DELETE'])"
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
		<dimiss v-if="dimissVisible" ref="dimiss" @refreshDataList="loadDataList"></dimiss>
	</div>
</template>

<script>
import AddOrUpdate from './user-add-or-update.vue';
import Dimiss from './dimiss.vue';
export default {
	components: {
		AddOrUpdate,
		Dimiss
	},
	data() {
		return {
			dataForm: {
				name: '',
				sex: '',
				role: '',
				deptId: '',
				status: ''
			},
			dataList: [],
			roleList: [],
			deptList: [],
			pageIndex: 1,
			pageSize: 10,
			totalCount: 0,
			dataListLoading: false,
			// 复选框的id
			dataListSelections: [],
			addOrUpdateVisible: false,
			// 员工离职弹窗
			dimissVisible: false,
			dataRule: {
				name: [{ required: false, pattern: '^[\u4e00-\u9fa5]{1,10}$', message: '姓名格式错误' }]
			}
		};
	},
	methods: {
		loadDataList: function() {
		    let that = this;
		    //请求后端数据的时候，让表格出现循环滚动的等待图标
		    that.dataListLoading = true;
		    let data = {
		        page: that.pageIndex,
		        length: that.pageSize,
		        name: that.dataForm.name,
		        sex: that.dataForm.sex,
		        role: that.dataForm.role,
		        deptId: that.dataForm.deptId,
		        status: that.dataForm.status
		    };
		    that.$http('user/searchUserByPage', 'POST', data, true, function(resp) {
		        let page = resp.page;
		        let list = page.list;
		        for (let one of list) {
		            if (one.status == 1) {
		                one.status = 'Working';
		            } else if (one.status == 2) {
		                one.status = 'Left';
		            }
		        }
		        that.dataList = list;
		        that.totalCount = page.totalCount;
		        that.dataListLoading = false;
		    });
		},
		sizeChangeHandle(val) {
		    this.pageSize = val;
		    //更改每页显示记录数量后，都从第一页开始查询
		    this.pageIndex = 1;
		    this.loadDataList();
		},
		currentChangeHandle(val) {
		    this.pageIndex = val;
		    this.loadDataList();
		},
		loadRoleList: function() {
			let that = this;
			that.$http('role/searchAllRole', 'GET', null, true, function(resp) {
				that.roleList = resp.list;
			});
		},
		loadDeptList: function() {
			let that = this;
			that.$http('dept/searchAllDept', 'GET', null, true, function(resp) {
				that.deptList = resp.list;
			});
		},
		selectionChangeHandle: function(val) {
			this.dataListSelections = val;
		},
		searchHandle: function() {
		    //先执行表单验证
		    this.$refs['dataForm'].validate(valid => {
		        if (valid) {
		            //清理页面上的表单验证结果
		            this.$refs['dataForm'].clearValidate();
		            //不允许上传空字符串给后端，但是可以传null值
		            if (this.dataForm.name == '') {
		                this.dataForm.name = null;
		            }
		            //如果当前页面不是第一页，则跳转到第一页显示查询的结果
		            if (this.pageIndex != 1) {
		                this.pageIndex = 1;
		            }
		            this.loadDataList();
		        } else {
		            return false;
		        }
		    });
		},
		addHandle: function() {
		    this.addOrUpdateVisible = true;
		    this.$nextTick(() => {
		        this.$refs.addOrUpdate.init();
		    });
		},
		updateHandle: function(id) {
		    this.addOrUpdateVisible = true;
		    this.$nextTick(() => {
		        this.$refs.addOrUpdate.init(id);
		    });
		},
		deleteHandle: function(id) {
		    let that = this;
		    let ids = id
		        ? [id]
		        : that.dataListSelections.map(item => {
		              return item.id;
		          });
		    if (ids.length == 0) {
		        that.$message({
		            message: 'no record selected',
		            type: 'warning',
		            duration: 1200
		        });
		    } else {
		        that.$confirm(`Are you sure you want to delete the selected records？`, 'Tips', {
		            confirmButtonText: 'Confirm',
		            cancelButtonText: 'Cancel',
		            type: 'warning'
		        }).then(() => {
		            that.$http('user/deleteUserByIds', 'POST', { ids: ids }, true, function(resp) {
		                if (resp.rows > 0) {
		                    that.$message({
		                        message: 'success',
		                        type: 'success',
		                        duration: 1200
		                    });
		                    that.loadDataList();
		                } else {
		                    that.$message({
		                        message: 'warning',
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
		this.loadDataList();
		this.loadRoleList();
		this.loadDeptList();
	}
};
</script>

<style lang="less" scoped="scoped"></style>
