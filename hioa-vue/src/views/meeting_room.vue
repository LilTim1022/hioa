<template>
	<div>
		<el-form :inline="true" :model="dataForm" :rules="dataRule" ref="dataForm">
			<el-form-item prop="name">
				<el-input
					v-model="dataForm.name"
					placeholder="meeting room name"
					size="medium"
					class="input"
					clearable="clearable"
				/>
			</el-form-item>
			<el-form-item>
				<el-select v-model="dataForm.canDelete" class="input" placeholder="Condition" size="medium">
					<el-option label="全部" value="all" />
					<el-option label="可删除" value="true" />
					<el-option label="不可删除" value="false" />
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-button size="medium" type="primary" @click="searchHandle()">Search</el-button>
				<el-button
					size="medium"
					type="primary"
					:disabled="!isAuth(['ROOT', 'MEETING_ROOM:INSERT'])"
					@click="addHandle()"
				>
					Add
				</el-button>
				<el-button
					size="medium"
					type="danger"
					:disabled="!isAuth(['ROOT', 'MEETING_ROOM:DELETE'])"
					@click="deleteHandle()"
				>
					Multi-Delete
				</el-button>
			</el-form-item>
		</el-form>
		<el-table
			:data="dataList"
			border
			v-loading="dataListLoading"
			@selection-change="selectionChangeHandle"
			cell-style="padding: 4px 0"
			style="width: 100%;"
			size="medium"
		>
			<el-table-column type="selection" header-align="center" align="center" width="50" />
			<el-table-column type="index" header-align="center" align="center" width="100" label="No.">
				<template #default="scope">
					<span>{{ (pageIndex - 1) * pageSize + scope.$index + 1 }}</span>
				</template>
			</el-table-column>
			<el-table-column prop="name" header-align="center" align="center" min-width="150" label="MeetingRoom Name" />
			<el-table-column header-align="center" align="center" min-width="120" label="People Limit">
				<template #default="scope">
					<span>{{ scope.row.max }}</span>
				</template>
			</el-table-column>
			<el-table-column header-align="center" align="center" min-width="100" label="Status">
				<template #default="scope">
					<span>{{ scope.row.status == 1 ? 'Usable' : 'Unusable' }}</span>
				</template>
			</el-table-column>
			<el-table-column prop="desc" header-align="center" align="center" label="Description" min-width="400" />
			<el-table-column header-align="center" align="center" width="150" label="Action">
				<template #default="scope">
					<el-button
						type="text"
						size="medium"
						:disabled="!isAuth(['ROOT', 'MEETING_ROOM:UPDATE']) || scope.row.id == 0"
						@click="updateHandle(scope.row.id)"
					>
						Modify
					</el-button>
					<el-button
						type="text"
						size="medium"
						:disabled="!isAuth(['ROOT', 'MEETING_ROOM:DELETE']) || scope.row.id == 0"
						@click="deleteHandle(scope.row.id)"
					>
						Delete
					</el-button>
				</template>
			</el-table-column>
		</el-table>
		<el-pagination
			@size-change="sizeChangeHandle"
			@current-change="currentChangeHandle"
			:current-page="pageIndex"
			:page-sizes="[10, 20, 50]"
			:page-size="pageSize"
			:total="totalCount"
			layout="total, sizes, prev, pager, next, jumper"
		></el-pagination>
		<add-or-update v-if="addOrUpdateVisible" ref="addOrUpdate" @refreshDataList="loadDataList"></add-or-update>
	</div>
</template>

<script>
import AddOrUpdate from './meeting_room-add-or-update.vue';
export default {
	components: {
		AddOrUpdate
	},
	data: function() {
		return {
			dataForm: {
				name: null,
				canDelete: null
			},
			dataList: [],
			pageIndex: 1,
			pageSize: 10,
			totalCount: 0,
			dataListLoading: false,
			dataListSelections: [],
			addOrUpdateVisible: false,
			dataRule: {
				name: [{ required: false, pattern: '^[a-zA-Z0-9\u4e00-\u9fa5]{1,20}$', message: '会议室名称格式错误' }]
			}
		};
	},
	methods: {
		deleteHandle: function(id) {
		    let that = this;
		    let ids = id
		        ? [id]
		        : that.dataListSelections.map(item => {
		              return item.id;
		          });
		    if (ids.length == 0) {
		        that.$message({
		            message: '没有选中记录',
		            type: 'warning',
		            duration: 1200
		        });
		    } else {
		        that.$confirm(`确定要删除选中的记录？`, '提示', {
		            confirmButtonText: '确定',
		            cancelButtonText: '取消',
		            type: 'warning'
		        }).then(() => {
		            that.$http('meeting_room/deleteMeetingRoomByIds', 'POST', { ids: ids }, true, function(resp) {
		                if (resp.rows > 0) {
		                    that.$message({
		                        message: '操作成功',
		                        type: 'success',
		                        duration: 1200
		                    });
		                    that.loadDataList();
		                } else {
		                    that.$message({
		                        message: '未能删除记录',
		                        type: 'warning',
		                        duration: 1200
		                    });
		                }
		            });
		        });
		    }
		},
		selectionChangeHandle: function(val) {
		    this.dataListSelections = val;
		},
		updateHandle: function(id) {
		    this.addOrUpdateVisible = true;
		    this.$nextTick(() => {
		        this.$refs.addOrUpdate.init(id);
		    });
		},
		addHandle: function() {
		    this.addOrUpdateVisible = true;
		    this.$nextTick(() => {
		        this.$refs.addOrUpdate.init();
		    });
		},
		searchHandle: function() {
		    this.$refs['dataForm'].validate(valid => {
		        if (valid) {
		            this.$refs['dataForm'].clearValidate();
		            if (this.dataForm.name == '') {
		                this.dataForm.name = null;
		            }
		            if (this.pageIndex != 1) {
		                this.pageIndex = 1;
		            }
		            this.loadDataList();
		        } else {
		            return false;
		        }
		    });
		},
		sizeChangeHandle: function(val) {
		    this.pageSize = val;
		    this.pageIndex = 1;
		    this.loadDataList();
		},
		currentChangeHandle: function(val) {
		    this.pageIndex = val;
		    this.loadDataList();
		},
		loadDataList: function() {
			let that = this;
			that.dataListLoading = true;
			let data = {
				name: that.dataForm.name,
				canDelete: that.dataForm.canDelete == 'all' ? null : that.dataForm.canDelete,
				page: that.pageIndex,
				length: that.pageSize
			};
			that.$http('meeting_room/searchMeetingRoomByPage', 'POST', data, true, function(resp) {
				let page = resp.page;
				that.dataList = page.list;
				that.totalCount = page.totalCount;
				that.dataListLoading = false;
			});
		},
		selectionChangeHandle: function(val) {
			this.dataListSelections = val;
		},
		sizeChangeHandle: function(val) {
			this.pageSize = val;
			this.pageIndex = 1;
			this.loadDataList();
		},
		currentChangeHandle: function(val) {
			this.pageIndex = val;
			this.loadDataList();
		},
		
	},
	created: function() {
		this.loadDataList();
	}
};
</script>

<style lang="less"></style>
