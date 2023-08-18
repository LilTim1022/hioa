<template>
	<el-dialog
		:title="!dataForm.id ? 'Add fine record' : 'Modify fine record'"
		:close-on-click-modal="false"
		v-model="visible"
		width="692px"
	>
		<el-form :model="dataForm" ref="dataForm" :rules="dataRule" label-width="60px">
			<el-form-item label="Type" prop="typeId">
				<el-select v-model="dataForm.typeId" placeholder="FineType" size="medium" style="width:40%">
					<el-option v-for="one in amectTypeList" :label="one.type" :value="one.id" />
				</el-select>
				<span class="desc">A penalty type must be selected, and the penalty amount can be automatically generated</span>
			</el-form-item>
			<el-form-item label="Amount" prop="amount">
				<el-input v-model="dataForm.amount" size="medium" style="width:40%" placeholder="Fine Value" clearable />
				<span class="desc">CNY</span>
			</el-form-item>
			<el-form-item label="reason" prop="reason">
				<el-input
					type="textarea"
					:rows="2"
					placeholder="Reason for fine"
					v-model="dataForm.reason"
					size="medium"
					resize="none"
					maxlength="200"
					show-word-limit
					clearable="clearable"
				/>
			</el-form-item>
			<el-form-item label="members" prop="members" v-if="dataForm.id == 0">
				<el-transfer
					v-model="dataForm.members"
					:data="users"
					:titles="['员工', '当事人']"
					filterable
					filter-placeholder="Please type in name"
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
				id: null,
				typeId: null,
				amount: null,
				reason: null,
				members: []
			},
			amectTypeList: [],
			users: [],
			dataRule: {
				typeId: [{ required: true, message: 'Penalty type is required' }],
				amount: [
					{
						required: true,
						pattern: '(^[1-9]([0-9]+)?(\.[0-9]{1,2})?$)|(^(0){1}$)|(^[0-9]\.[0-9]([0-9])?$)',
						message: 'Fine format error'
					}
				],
				reason: [{ required: true, message: 'Reason for penalty is required' }],
				members: [
					{ required: true, trigger: 'blur', message: 'Fine Members must be set' },
					{ required: false, trigger: 'change', message: 'Fine Members must be set' }
				]
			}
		};
	},
	methods: {
		init: function(id) {
			let that = this;
			that.dataForm.id = id || 0;
			that.visible = true;
			that.$nextTick(() => {
				that.$refs['dataForm'].resetFields();
				that.$http('amect_type/searchAllAmectType', 'GET', {}, true, function(resp) {
					that.amectTypeList = resp.list;
				});
				that.$http('user/searchAllUser', 'GET', null, true, function(resp) {
					let temp = [];
					for (let one of resp.list) {
						temp.push({ key: one.id, label: one.name });
					}
					that.users = temp;
				});
				if (id) {
					that.$http('amect/searchById', 'POST', { id: id }, true, function(resp) {
						that.dataForm.typeId = resp.typeId;
						that.dataForm.amount = resp.amount+"";
						that.dataForm.reason = resp.reason;
					});
				}
			});
		},
    dataFormSubmit: function() {
      let that = this;
      let data = {
        userId: that.dataForm.members,
        amount: that.dataForm.amount,
        typeId: that.dataForm.typeId,
        reason: that.dataForm.reason
      };
      if (that.dataForm.id) {
        data.id = that.dataForm.id;
      }
      this.$refs['dataForm'].validate(valid => {
        if (valid) {
          that.$http(`amect/${!that.dataForm.id ? 'insert' : 'update'}`, 'POST', data, true, function(resp) {
            if (resp.rows > 0) {
              that.visible = false;
              that.$emit('refreshDataList');
              that.$message({
                message: 'success',
                type: 'success',
                duration: 1200
              });
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

<style>
.desc {
	margin-left: 15px;
}
</style>
