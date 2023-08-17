<template>
	<el-dialog
		:title="!dataForm.id ? 'Add' : 'Modify'"
		v-if="isAuth(['ROOT', 'ROLE:INSERT', 'ROLE:UPDATE'])"
		:close-on-click-modal="false"
		v-model="visible"
		custom-class="dialog"
		width="692px"
	>
		<el-form :model="dataForm" ref="dataForm" :rules="dataRule" label-width="60px">
			<el-form-item label="roleName" prop="roleName">
				<el-input v-model="dataForm.roleName" size="medium" style="width:50%" :readonly="systemic" clearable />
				<span class="note">（ The character name must be between 2 and 10 characters ）</span>
			</el-form-item>
			<el-form-item label="desc" prop="desc">
				<el-input v-model="dataForm.desc" style="width:50%" size="medium" maxlength="20" clearable />
				<span class="note">（ Note information must be within 20 characters ）</span>
			</el-form-item>
			<el-form-item label="permissions" prop="permissions">
				<el-transfer
					v-model="dataForm.permissions"
					:data="permisionList"
					size="medium"
					:titles="['Permission List', 'Owned Permissions']"
					filterable
					filter-placeholder="Please enter permission"
				/>
			</el-form-item>
		</el-form>
		<template #footer>
			<span class="dialog-footer">
				<el-button size="medium" @click="visible = false">Cancel</el-button>
				<el-button type="primary" size="medium" @click="dataFormSubmit">Ok</el-button>
			</span>
		</template>
	</el-dialog>
</template>

<script>
export default {
	data: function() {
		return {
			visible: false,
			systemic: true,
			dataForm: {
				id: null,
				roleName: null,
				permissions: [],
				desc: null,
				changed: false
			},
			permisionList: [],
			oldPermissions: [],
			dataRule: {
				roleName: [
					{ required: true, pattern: '^[a-zA-Z0-9\u4e00-\u9fa5]{2,10}$', message: 'Wrong character name format' }
				],
				permissions: [
					{ required: true, trigger: 'blur', message: 'Role has no associated privileges' },
					{ required: false, trigger: 'change', message: 'Role has no associated privileges' }
				]
			}
		};
	},

	methods: {
		init: function(id, systemic) {
			let that = this;
			that.dataForm.id = id || 0;
			that.systemic = systemic;
			that.visible = true;
			that.$nextTick(() => {
				that.$refs['dataForm'].resetFields();
				let defaultPermissions = [];
				if (id) {
					that.$http('role/searchById', 'POST', { id: id }, false, function(resp) {
						that.dataForm.roleName = resp.roleName;
						that.dataForm.desc = resp.desc;
						that.dataForm.permissions = JSON.parse(resp.permissions);
						//保存原始权限数据
						that.oldPermissions = JSON.parse(resp.permissions);
						defaultPermissions = resp.defaultPermissions;
					});
				}
				that.$http('permission/searchAllPermission', 'GET', null, true, function(resp) {
					let temp = [];
					//遍历所有系统权限
					for (let one of resp.list) {
						let disabled = false;
						//判断要修改的是否为系统内置角色
						if (that.systemic) {
							disabled = defaultPermissions.includes(one.id);
						}
						//生成左侧数组元素值
						temp.push({ key: one.id, label: `${one.moduleName}（${one.actionName}）`, disabled: disabled });
					}
					//更新左侧数组
					that.permisionList = temp;
				});
			});
		},
		dataFormSubmit: function() {
			let that = this;
			this.$refs['dataForm'].validate(valid => {
				if(valid) {
					that.dataForm.permissions.sort(function(a,b) {
						return a-b;
					});
					if(that.dataForm.permissions.join() != that.oldPermissions.join()) {
						that.dataForm.changed = true;
					} else {
						that.dataForm.changed = false;
					}
					that.$http(`role/${!that.dataForm.id ? 'insert' : 'update'}`, 'POST', that.dataForm, true, function(resp) {
						if(resp.rows==1) {
							that.$message({
								message:'success',
								type:'success',
								duration:1200
							});
							that.visible = false;
							that.$emit('refreshDataList');
						} else {
							that.$message({
								message:'error',
								type:'error',
								duration:1200
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
.note {
	margin-left: 20px;
	color: #999;
}
</style>
