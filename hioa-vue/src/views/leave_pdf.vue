<template>
	<el-dialog width="780px" :close-on-click-modal="false" v-model="visible" :show-close="false">
		<div id="pdfDom">
			<h2 class="title">Employee Leave Request Form</h2>
			<table class="leave-table">
				<tr align="center">
					<td width="14%">Name</td>
					<td width="16%">{{ name }}</td>
					<td width="14%">Gender</td>
					<td width="16%">{{ sex }}</td>
					<td width="14%">Department</td>
					<td>{{ dept }}</td>
				</tr>
				<tr>
					<td align="center">Leave Category</td>
					<td colspan="5">{{ type }}</td>
				</tr>
				<tr>
					<td align="center">Leave Period</td>
					<td colspan="5">{{ start }}&nbsp;&nbsp;~&nbsp;&nbsp;{{ end }}</td>
				</tr>
				<tr>
					<td align="center">Reason for Leave</td>
					<td colspan="5">{{ reason }}</td>
				</tr>
				<tr>
					<td align="center">Signature Here</td>
					<td colspan="2"></td>
					<td align="center">HR Seal</td>
					<td colspan="3"></td>
				</tr>
			</table>

			<p class="desc">Remarks: The employee assumes full responsibility during the leave period and should promptly return to their work position after the leave ends; otherwise, it will be treated as absenteeism.</p>
		</div>
		<template #footer>
			<span class="dialog-footer">
				<el-button type="primary" @click="getPdf('#pdfDom')" size="medium">Download Leave Form</el-button>
				<el-button size="medium" @click="cancel()">Cancel</el-button>
				
			</span>
		</template>
	</el-dialog>
</template>

<script>
export default {
	data: function() {
		return {
			visible: false,
			htmlTitle: 'Employee Leave Request Form',
			name: null,
			sex: null,
			dept: null,
			reason: null,
			start: null,
			end: null,
			type: null
		};
	},
	methods: {
		init: function(id) {
      let that = this;
      that.visible = true;
      that.name =  null;
      that.sex =  null;
      that.dept =  null;
      that.reason =  null;
      that.start =  null;
      that.end =  null;
      that.type =  null;
      //发送ajax
      that.$http("leave/searchLeaveById", "POST", {id: id}, true, function(resp) {
        that.name =  resp.name;
        that.sex =  resp.sex;
        that.dept =  resp.dept;
        that.reason =  resp.reason;
        if (resp.type == 1) {
          that.type = 'Sick Leave';
        } else if (resp.type == 2) {
          that.type = 'Personal Leave';
        }
        that.start = resp.start;
        that.end = resp.end;
      });
    },
    //弹窗页面关闭
    cancel: function () {
      this.visible = false;
    }
	}
};
</script>

<style lang="less" scoped="scoped">
@import url('leave_pdf.less');
</style>
