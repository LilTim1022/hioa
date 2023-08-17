<template>
	<el-dialog title="Archive" width="500px" :close-on-click-modal="false" v-model="visible" :show-close="false">
		<el-upload
			ref="upload"
			:action="url"
			list-type="picture-card"
			accept=".jpg,.jpeg,.png"
			with-credentials="true"
			:before-upload="beforeUploadHandle"
			:on-success="successHandle"
			:on-remove="removeHandle"
		>
			<i class="el-icon-plus"></i>
		</el-upload>
		<template #footer>
			<span class="dialog-footer">
				<el-button size="medium" @click="cancel()">Cancel</el-button>
				<el-button type="primary" @click="archive()" size="medium" :disabled="disableBtn">{{ btn }}</el-button>
			</span>
		</template>
	</el-dialog>
</template>

<script>
export default {
	data: function() {
		return {
			visible: false,
			url: this.$baseUrl + 'cos/uploadCosFile?type=archive',
			taskId: '',
			picList: {},
			disableBtn: false,
			btn: '确定'
		};
	},
	methods: {
		init: function (taskId) {
      let that = this;
      that.visible = true;
      that.taskId = taskId;
      that.btn = "Archive";
      that.disableBtn = false;
      that.$nextTick(() => {
        that.$refs["upload"].clearFiles();
      });
    },
   beforeUploadHandle: function (file) {
      if(file.type !== 'image/jpg' && file.type !== 'image/jpeg' && file.type !== 'image/png') {
        this.$message.error('Only support jpg、png files!');
        return false;
      }
      return true;
   },
    successHandle: function (resp, file, fileList) {
      // console.log(fileList);
      if(resp && resp.code === 200) {
        for (let one of fileList) {
          this.picList[one.response.url] = one.response.path;
        }
      } else {
        this.$message.error('Picture Upload Failed');
      }
    },
    removeHandle: function(file, fileList) {
      let that = this;
      let url = file.response.url;
      let path = this.picList[url];
      that.$http('cos/deleteCosFile', 'POST', { pathes: [path]}, true, function(resp) {
        delete that.picList[url];
      });
    },
    cancel: function() {
      let that = this;
      if (Object.keys(that.picList).length > 0) {
        let pathes = Object.values(that.picList);
        that.$http('cos/deleteCosFile', 'POST', { pathes: pathes}, true, function(resp) {
          that.picList = {};
        });
      }
      that.visible = false;
      that.$refs['upload'].clearFiles();
    },
    archive: function () {
      let that = this;
      //禁用归档按钮，防止归档中用户重复点击归档按钮
      that.btn = 'Archiving';
      that.disableBtn = true;
      //判断用户是否成功上传文件
      if (Object.keys(that.picList).length == 0) {
        that.$message({
          message:'Have not upload picture',
          type: 'error',
          duration: 1200
        });
        return;
      }
      let files = [];
      for (let key in that.picList) {
        files.push({
          url: key,
          path: that.picList[key]
        });
      }
      let data = {
        taskId: that.taskId,
        files: JSON.stringify(files)
      };
      that.$http("approval/archiveTask", 'POST', data, true, function (resp) {
        that.$message({
          message:'Success',
          type:'success',
          duration:1200
        });
        that.visible = false;
        that.$emit('refreshDataList');
      });
    }
	},
};
</script>

<style lang="less"></style>
