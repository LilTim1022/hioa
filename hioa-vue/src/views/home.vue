<template>
	<div class="home">
		<el-row :gutter="70" type="flex" justify="center">
			<el-col :span="9" class="left">
				<h2>HiOA Office System</h2>
				<div class="desc">
					<p>Online Meeting & efficient office</p>
				</div>
				<div class="bottom">
					<div class="remark-container">
						<div class="ball red">1</div>
						<div class="remark">Organization</div>
					</div>
					<div class="remark-container">
						<div class="ball green">2</div>
						<div class="remark">Doc Generate</div>
					</div>
					<div class="remark-container">
						<div class="ball blue">3</div>
						<div class="remark">Approval</div>
					</div>
				</div>
			</el-col>
			<el-col :span="11" class="right">
				<img src="../assets/home/banner.png" class="banner" /></el-col>
		</el-row>
	</div>
</template>

<script></script>

<style lang="less" scoped="scoped">
@import url('home.less');
</style>
