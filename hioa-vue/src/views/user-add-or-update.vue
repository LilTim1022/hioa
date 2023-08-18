<template>
	<el-dialog
		:title="!dataForm.id ? 'Add' : 'Modify'"
		v-if="isAuth(['ROOT', 'USER:INSERT', 'USER:UPDATE'])"
		:close-on-click-modal="false"
		v-model="visible"
		width="450px"
	>
		<el-form :model="dataForm" ref="dataForm" :rules="dataRule" label-width="80px">
			<el-form-item label="username" prop="username">
				<el-input v-model="dataForm.username" size="medium" clearable />
			</el-form-item>
			<el-form-item label="password" prop="password">
				<el-input type="password" v-model="dataForm.password" size="medium" clearable />
			</el-form-item>
			<el-form-item label="name" prop="name">
				<el-input v-model="dataForm.name" size="medium" clearable />
			</el-form-item>
			<el-form-item label="sex" prop="sex">
				<el-select v-model="dataForm.sex" size="medium" style="width: 100%;" clearable>
					<el-option label="Male" value="男"></el-option>
					<el-option label="Female" value="女"></el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="tel" prop="tel">
				<el-input v-model="dataForm.tel" size="medium" clearable />
			</el-form-item>
			<el-form-item label="email" prop="email">
				<el-input v-model="dataForm.email" size="medium" clearable />
			</el-form-item>
			<el-form-item label="hiredate" prop="hiredate">
				<el-date-picker
					v-model="dataForm.hiredate"
					type="date"
					placeholder="Choose Date"
					size="medium"
					:editable="false"
					format="YYYY-MM-DD"
					value-format="YYYY-MM-DD"
					style="width: 100%;"
				/>
			</el-form-item>
			<el-form-item label="role" prop="role">
				<el-select
					v-model="dataForm.role"
					size="medium"
					placeholder="Choose Role"
					style="width: 100%;"
					multiple
					clearable
				>
					<el-option
						v-for="one in roleList"
						:key="one.id"
						:label="one.roleName"
						:value="one.id"
						:disabled="one.roleName == '超级管理员'"
					></el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="deptId" prop="deptId">
				<el-select
					v-model="dataForm.deptId"
					size="medium"
					placeholder="Choose Dept"
					style="width: 100%;"
					clearable
				>
					<el-option v-for="one in deptList" :key="one.id" :label="one.deptName" :value="one.id" />
				</el-select>
			</el-form-item>
		</el-form>
		<template #footer>
			<span class="dialog-footer">
				<el-button size="medium" @click="visible = false">Cancel</el-button>
				<el-button type="primary" size="medium" @click="dataFormSubmit">Confirm</el-button>
			</span>
		</template>
	</el-dialog>
</template>

<script>
import dayjs from 'dayjs';
export default {
	data: function() {
		return {
			visible: false,
			dataForm: {
				id: null,
				username: null,
				password: null,
				name: null,
				sex: null,
				tel: null,
				email: null,
				hiredate: new Date(),
				role: null,
				deptId: null,
				status: 1
			},
			roleList: [],
			deptList: [],
			dataRule: {
				username: [{ required: true, pattern: '^[a-zA-Z0-9]{5,20}$', message: 'username format error' }],
				password: [{ required: true, pattern: '^[a-zA-Z0-9]{6,20}$', message: 'wrong password format' }],
				name: [{ required: true, pattern: '^[\u4e00-\u9fa5]{2,10}$', message: 'wrong name format' }],
				sex: [{ required: true, message: 'Gender cannot be empty' }],
				tel: [{ required: true, pattern: '^1\\d{10}$', message: 'phone format error' }],
				email: [
					{
						required: true,
						pattern: '^([a-zA-Z]|[0-9])(\\w|\\-)+@[a-zA-Z0-9]+\\.([a-zA-Z]{2,4})$',
						message: 'E-mail format error'
					}
				],
				hiredate: [{ required: true, trigger: 'blur', message: 'Entry date cannot be empty' }],
				role: [{ required: true, message: 'role cannot be empty' }],
				deptId: [{ required: true, message: 'Department cannot be empty' }],
				status: [{ required: true, message: 'status cannot be empty' }]
			}
		};
	},
	methods: {
		init: function(id) {
			let that = this;
			that.dataForm.id = id || 0;
			that.visible = true;
			// 加载列表数据的ajax请求放在下次DOM更新来执行
			that.$nextTick(() => {
				that.$refs['dataForm'].resetFields();
				// 加载角色
				that.$http('role/searchAllRole', 'GET', null, true, function(resp) {
					that.roleList = resp.list;
				});
				// 加载部门数据
				that.$http('dept/searchAllDept', 'GET', null, true, function(resp) {
					that.deptList = resp.list;
				});
				// 修改用户业务中,还要给表单加载用户信息
				if (that.dataForm.id) {
					that.$http('user/searchById', 'POST', { userId: id }, true, function(resp) {
						that.dataForm.username = resp.username;
						that.dataForm.password = resp.password;
						that.dataForm.name = resp.name;
						that.dataForm.sex = resp.sex;
						that.dataForm.tel = resp.tel;
						that.dataForm.email = resp.email;
						that.dataForm.hiredate = resp.hiredate;
						that.dataForm.role = JSON.parse(resp.role);
						that.dataForm.deptId = resp.deptId;
						that.dataForm.status = resp.status;
					});
				}
			});
		},
		dataFormSubmit: function() {
		    let that = this;
		    this.$refs['dataForm'].validate(valid => {
		        if (valid) {
		            let data = {
		                userId: that.dataForm.id,
		                username: that.dataForm.username,
		                password: that.dataForm.password,
		                name: that.dataForm.name,
		                sex: that.dataForm.sex,
		                tel: that.dataForm.tel,
		                email: that.dataForm.email,
		                hiredate: dayjs(that.dataForm.hiredate).format('YYYY-MM-DD'),
		                role: that.dataForm.role,
		                deptId: that.dataForm.deptId,
		                status: that.dataForm.status
		            };
		            that.$http(`user/${!that.dataForm.id ? 'insert' : 'update'}`, 'POST', data, true, resp => {
		                if (resp.rows == 1) {
		                    that.$message({
		                        message: 'success',
		                        type: 'success',
		                        duration: 1200
		                    });
		                    that.visible = false;
		                    that.$emit('refreshDataList');
		                } else {
		                    that.$message({
		                        message: 'error',
		                        type: 'error',
		                        duration: 1200
		                    });
		                }
		            });
		        }
		    });
		}
	}
};
</script>

<style lang="less" scoped="scoped">
</style>
