<template>
	<el-dialog width="800px" :close-on-click-modal="false" v-model="visible" :show-close="false" center>
		<div id="pdfDom">
			<img :src="qrCodeBase64" class="qrCode">
			<h2 class="title">Expense Reimbursement Form</h2>
			<div class="top-info-container">
				<span class="info">Reimbursement Department:{{ dept }}</span>
				<span class="info">Reimbursement Person:{{ name }}</span>
				<span class="info">Reimbursement Date:{{ date }}</span>
			</div>
			<div class="reim-table-container">
				<table class="reim-table">
					<tr align="center">
						<th width="30%">Reimbursement Items</th>
						<th width="34%">Remarks</th>
						<th width="20%">Category</th>
						<th width="16%">Amount</th>
					</tr>
					<tr align="center" v-for="one in content">
						<td align="left">{{one.title}}</td>
						<td align="left">{{one.desc}}</td>
						<td>{{one.type}}</td>
						<td align="left">{{one.money!=""?one.money+"元":""}}</td>
					</tr>
					<tr>
						<th align="center">Total Reimbursement</th>
						<td colspan="3">{{ amount }}CNY (Chinese Yuan) </td>
					</tr>
					<tr>
						<th align="center">人民币大写</th>
						<td colspan="3">{{ smalltoBIG(amount) }}</td>
					</tr>
					<tr>
						<td colspan="5">
							<div class="info-container">
								<span class="info">Loan Amount:{{ anleihen }}CNY </span>
								<span class="info">Amount to be Refunded:{{ money_1 }}CNY </span>
								<span class="info">Amount to be Paid:{{ money_2 }}CNY </span>
							</div>
						</td>
					</tr>
				</table>
			</div>
			<div class="bottom-info-container"></div>
			<div class="bottom-info-container">
				<span class="sig">Accounting Supervisor:</span>
				<span class="sig">Review:</span>
				<span class="sig">Cashier:</span>
				<span class="sig">Reimbursement Person:</span>
			</div>
		</div>
		<template #footer>
			<span class="dialog-footer">
				<el-button type="primary" @click="getPdf('#pdfDom')" size="medium">Download Reimbursement Form</el-button>
				<el-button size="medium" @click="cancel()">Close Window</el-button>
			</span>
		</template>
	</el-dialog>
</template>

<script>
export default {
	data: function() {
		return {
			visible: false,
			htmlTitle: 'Expense Reimbursement Form',
			dept: null,
			name: null,
			date: null,
			amount: null,
			balance: null,
			anleihen: null,
			money_1: 0,
			money_2: 0,
			content:[],
			qrCodeBase64:null
		};
	},
	methods: {
		init: function(id) {
      let that = this;
      that.visible = true;
      that.dept = null;
      that.name = null;
      that.date = null;
      that.amount = null;
      that.balance = null;
      that.anleihen = null;
      that.money_1 = null;
      that.money_2 = null;
      that.content = [];
      that.$http("reim/searchReimById", "POST", {id:id}, true, function (resp) {
        that.dept = resp.dept;
        that.name = resp.name;
        that.date = resp.date;
        that.amount = resp.amount;
        that.balance = resp.balance;
        that.anleihen = resp.anleihen;
        //给money1或2赋值
        //借款金额大于报销总金额，给money1赋值
        if (that.anleihen > that.amount) {
          that.money_1 = that.anleihen - that.amount;
        } else if(that.anleihen < that.amount) {
          that.money_2 = that.amount - that.anleihen;
        }
        //给content变量赋值
        let content = JSON.parse(resp.content);
        let temp = 5 - content.length;  //循环次数
        for (let i = 0; i < temp; i++) {
          //往数组添加空的元素
          content.push({tittle:"", desc:"", type: "", money: ""});
        }
        //content赋值给模型层的content
        that.content = content;
        //把查出来的base64给模型层赋值
        that.qrCodeBase64 = resp.qrCodeBase64;
      });
    },
		smalltoBIG: function(n) {
			var fraction = ['角', '分'];
			var digit = ['零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖'];
			var unit = [['元', '万', '亿'], ['', '拾', '佰', '仟']];
			var head = n < 0 ? '欠' : '';
			n = Math.abs(n);

			var s = '';

			for (var i = 0; i < fraction.length; i++) {
				s += (digit[Math.floor(n * 10 * Math.pow(10, i)) % 10] + fraction[i]).replace(/零./, '');
			}
			s = s || '整';
			n = Math.floor(n);

			for (var i = 0; i < unit[0].length && n > 0; i++) {
				var p = '';
				for (var j = 0; j < unit[1].length && n > 0; j++) {
					p = digit[n % 10] + unit[1][j] + p;
					n = Math.floor(n / 10);
				}
				s = p.replace(/(零.)*零$/, '').replace(/^$/, '零') + unit[0][i] + s;
			}
			return (
				head +
				s
					.replace(/(零.)*零元/, '元')
					.replace(/(零.)+/g, '零')
					.replace(/^整$/, '零元整')
			);
		},
    cancel: function () {
      this.visible = false;
    }
	}
};
</script>

<style lang="less" scoped="scoped">
@import url('reim_pdf.less');
</style>
