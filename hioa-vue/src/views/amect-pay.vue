<template>
	<el-dialog title="缴纳罚款" :close-on-click-modal="false" v-model="visible" width="305px" center>
		<img :src="qrCode" class="qrcode" v-if="!result" />
		<div v-if="result" class="pay-success">
			<el-result icon="success" title="付款成功" subTitle="请根据提示进行操作"></el-result>
		</div>
		<div class="dialog-footer-style">
			<el-button type="danger" size="medium" v-if="!result" @click="cancelHandle">取消支付</el-button>
			<el-button type="primary" size="medium" v-if="!result" @click="successHandle">支付成功</el-button>
			<el-button type="primary" size="medium" v-if="result" @click="closeHandle">关闭窗口</el-button>
		</div>
	</el-dialog>
</template>

<script>
export default {
	data: function() {
		return {
			visible: false,
			dataForm: {
				id: null
			},
			result: false,
			qrCode: null
		};
	},
	methods: {
		init: function(id) {
      let that = this;
      that.visible = true;
      that.dataForm.id = id;
      that.result = false;
      that.$nextTick(() => {
        //使用WebSocket接受后端推送的付款结果
        //cookie得到token字符串
        let token = that.$cookies.get('token');
        that.$socket.sendObj({opt: "pay_amect", token});
        //回调函数
        that.$options.sockets.onmessage = function(resp) {
          let data = resp.data;
          //判断data字符串结果
          if (data == "收款成功") {
            that.result = true;
          }
        };
        that.$http("amect/createNativeAmectPayOrder", "POST", {amectId:id}, true, function(resp) {
          //取出qrcode的base64，赋值给模型层的qrcode
          that.qrCode = resp.qrCodeBase64;
        });
      });
    },
    cancelHandle: function () {
      //取消支付隐藏弹窗
      this.visible = false;
    },
    successHandle: function () {
      let that = this;
      //隐藏弹窗
      that.visible = false;
      //发起ajax请求查询支付结果
      that.$http("amect/searchNativeAmectPayResult", "POST", {amectId: that.dataForm.id}, true, function (resp) {
        //刷新amect页面
        that.$emit("refreshDataList");
      });
    },
    closeHandle: function() {
      //隐藏弹窗
      this.visible = false;
      //刷新amect页面
      this.$emit("refreshDataList");
    }
	}
};
</script>

<style scoped>
.qrCode {
	width: 255px;
	height: 255px;
}
.pay-success {
	width: 255px;
	height: 255px;
}
.dialog-footer-style {
	padding-bottom: 30px;
	padding-top: 10px;
	text-align: center;
}
</style>
