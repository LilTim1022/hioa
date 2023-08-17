<template>
	<el-dialog title="提示" v-model="visible" width="25%">
		<el-form :model="dataForm" :rules="dataRule" ref="dataForm" label-width="80px">
			<el-form-item label="原密码" prop="password">
				<el-input type="password" v-model="dataForm.password" size="medium" clearable />
			</el-form-item>
			<el-form-item label="新密码" prop="newPassword">
				<el-input type="password" v-model="dataForm.newPassword" size="medium" clearable />
			</el-form-item>
			<el-form-item label="确认密码" prop="confirmPassword">
				<el-input type="password" v-model="dataForm.confirmPassword" size="medium" clearable />
			</el-form-item>
		</el-form>
		<template #footer>
			<span class="dialog-footer">
				<el-button size="medium" @click="visible = false">取消</el-button>
				<el-button type="primary" size="medium" @click="dataFormSubmit">确定</el-button>
			</span>
		</template>
	</el-dialog>
</template>

<script>
export default {
	data() {
		const validateConfirmPassword = (rule, value, callback) => {
			if (value != this.dataForm.newPassword) {
				callback(new Error('两次输入的密码不一致'));
			} else {
				callback();
			}
		};

		return {
			visible: false,
			dataForm: {
				password: '',
				newPassword: '',
				confirmPassword: ''
			},
			dataRule: {
				password: [{ required: true, pattern: '^[a-zA-Z0-9]{6,20}$', message: '密码格式错误' }],
				newPassword: [{ required: true, pattern: '^[a-zA-Z0-9]{6,20}$', message: '密码格式错误' }],
				confirmPassword: [
					{ required: true, pattern: '^[a-zA-Z0-9]{6,20}$', message: '密码格式错误' },
					{ validator: validateConfirmPassword, trigger: 'blur' }
				]
			}
		};
	},
	methods: {
		init() {
		    this.visible = true;  //显示弹窗
		    //因为清空表单控件是异步的，所以把清空表单控件放在下次DOM更新循环中
		    this.$nextTick(() => {
		        this.$refs['dataForm'].resetFields();
		    });
		},

		dataFormSubmit: function() {
		    let that = this;
		    //前端表单验证
			 // 注意以下若都用that,则参照视频的代码；因为上面声明了；若要用this,下面这里不能用传统方法声明回调函数,而要用lambda表达式?
		    this.$refs['dataForm'].validate(valid => {
				// valid说明前端验证没有问题
		        if (valid) {
		            let data = { 
		                password: that.dataForm.password,
		                newPassword: that.dataForm.newPassword
		            }
					// 发送ajax请求，POST请求类型，走异步请求true，回调函数function（resp）
		            that.$http('user/updatePassword', 'POST', data, true, resp => {
		                // rows=1时证明修改了记录成功（由swagger上次后端测试看出），停留时间1.2s
						if (resp.rows == 1) {
		                    this.$message({
		                        message: '密码修改成功',
		                        type: 'success',
		                        duration: 1200,
		                    });
							// 已经修改成功了,隐藏一下弹窗
		                    this.visible = false;
		                } else {
		                    this.$message({
		                        message: '密码修改失败',
		                        type: 'error',
		                        duration: 1200,
		                    });
		                }
		            });
		        }
		    });
		}

	}
};
</script>

<style></style>
