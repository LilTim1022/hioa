<template>
	<el-dialog title="Application for Online Meeting" :close-on-click-modal="false" v-model="visible" width="692px">
		<el-form :model="dataForm" ref="dataForm" :rules="dataRule" label-width="60px">
			<el-form-item label="Theme" prop="title">
				<el-input v-model="dataForm.title" size="medium" style="width:100%" clearable="clearable" />
			</el-form-item>
			<el-form-item label="Description" prop="desc">
				<el-input
					type="textarea"
					:rows="2"
					placeholder="Please type in description"
					v-model="dataForm.desc"
					size="medium"
					resize="none"
					maxlength="200"
					clearable="clearable"
				/>
			</el-form-item>
			<el-form-item label="Date" prop="date">
				<el-date-picker
					v-model="dataForm.date"
					type="date"
					placeholder="Choose Date"
					style="width:34.5%"
					size="medium"
					:disabledDate="disabledDate"
					clearable="clearable"
				></el-date-picker>
				<span class="note">Date could only be in the future</span>
			</el-form-item>
			<el-form-item label="Time">
				<el-col :span="11">
					<el-form-item prop="start" class="inner-item">
						<el-time-select
							placeholder="Start Time"
							v-model="dataForm.start"
							start="00:00"
							step="00:10"
							end="24:00"
							size="medium"
							style="width:96%"
							clearable="clearable"
						></el-time-select>
					</el-form-item>
				</el-col>
				<el-col :span="2">&nbsp;&nbsp;~&nbsp;&nbsp;</el-col>
				<el-col :span="11" prop="end">
					<el-form-item prop="end" class="inner-item">
						<el-time-select
							placeholder="End Time"
							v-model="dataForm.end"
							start="00:00"
							step="00:10"
							end="24:00"
							size="medium"
							style="width:96%"
							clearable="clearable"
							:minTime="dataForm.start"
						></el-time-select>
					</el-form-item>
				</el-col>
				<span class="note">Remind the time scope</span>
			</el-form-item>
			<el-form-item label="Attender" prop="members">
				<el-transfer
					v-model="dataForm.members"
					:data="users"
					:titles="['员工', '参会人']"
					filterable
					filter-placeholder="Please fill in names"
				/>
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
export default {
	data: function() {
		return {
			visible: false,
			dataForm: {
				title: null,
				date: null,
				start: null,
				end: null,
				desc: null,
				members: [],
				type: 1
			},
			disabledDate(date) {
				return date.getTime() < Date.now() - 24 * 60 * 60 * 1000;
			},
			users: [],
			dataRule: {
				title: [{ required: true, pattern: '^[a-zA-Z0-9\u4e00-\u9fa5]{2,30}$', message: 'Meeting subject format error' }],
				desc: [{ required: true, message: 'Meeting content is required' }],
				date: [{ required: true, message: 'date is required' }],
				start: [{ required: true, message: 'Start time is required' }],
				end: [{ required: true, message: 'End time is required' }],
				members: [
					{ required: true, trigger: 'blur', message: 'Participants must be set' },
					{ required: false, trigger: 'change', message: 'Participants must be set' }
				]
			}
		};
	},
	methods: {
		init: function(id) {
			let that = this;
			that.visible = true;
			that.$nextTick(() => {
				that.$refs['dataForm'].resetFields();
				that.$http('user/searchAllUser', 'GET', null, true, function(resp) {
					let temp = [];
					for (let one of resp.list) {
						temp.push({ key: one.id, label: one.name });
					}
					that.users = temp;
				});
			});
		},
		data: function() {
		    return {
		        visible: false,
		        dataForm: {
		            title: null,
		            date: null,
		            start: null,
		            end: null,
		            desc: null,
		            members: [],
		            type: 1
		        },
		        disabledDate(date) {
		            return date.getTime() < Date.now() - 24 * 60 * 60 * 1000;
		        },
		        users: [],
		        dataRule: {
		            title: [{ required: true, pattern: '^[a-zA-Z0-9\u4e00-\u9fa5]{2,30}$', message: 'Meeting subject format error' }],
		            desc: [{ required: true, message: 'Meeting content is required' }],
		            date: [{ required: true, message: 'date is required' }],
		            start: [{ required: true, message: 'Start time is required' }],
		            end: [{ required: true, message: 'End time is required' }],
		            members: [
		                { required: true, trigger: 'blur', message: 'Participants must be set' },
		                { required: false, trigger: 'change', message: 'Participants must be set' }
		            ]
		        }
		    };
		},
		dataFormSubmit: function() {
		    let that = this;
		    let data = JSON.parse(JSON.stringify(that.dataForm));
		    data.date = dayjs(that.dataForm.date).format('YYYY-MM-DD');
		    data.members = JSON.stringify(that.dataForm.members);
		    this.$refs['dataForm'].validate(valid => {
		        if (valid) {
		            that.$http('meeting/insert', 'POST', data, true, function(resp) {
		                if (resp.rows == 1) {
		                    that.visible = false;
		                    that.$message({
		                        message: '操作成功',
		                        type: 'success',
		                        duration: 1200
		                    });
		                    setTimeout(function() {
		                        that.$emit('refresh');
		                    }, 1200);
		                } else {
		                    that.$message({
		                        message: '操作失败',
		                        type: 'error',
		                        duration: 1200
		                    });
		                }
		            });
		        }
		    });
		},
		refresh: function() {
		    this.$refs['dataForm'].resetFields();
		    this.loadDataList();
		}
	}
};
</script>

<style lang="less" scoped="scoped">
</style>
