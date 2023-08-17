import {
	createRouter,
	createWebHashHistory
}
from 'vue-router'
import Login from '../views/login.vue'
import Main from "../views/main.vue"
import Home from "../views/home.vue"
import Role from "../views/role.vue"
import User from "../views/user.vue"
import Dept from "../views/dept.vue"
import MeetingRoom from '../views/meeting_room.vue'
import OfflineMeeting from "../views/offline_meeting.vue"
import OnlineMeeting from "../views/online_meeting.vue"
import MeetingVideo from "../views/meeting_video.vue"
import Approval from "../views/approval.vue"
import Leave from "../views/leave.vue"
import Amect from "../views/amect.vue"
import AmectType from "../views/amect_type.vue"
import AmectReport from "../views/amect_report.vue"
import Reim from "../views/reim.vue"
import NotFound from "../views/404.vue"


const routes = [{
		path: '/login',
		name: 'Login',
		component: Login
	},
	{
		path: '/',
		name: 'Main',
		component: Main,
		children: [{
				path: '/home',
				name: 'Home',
				component: Home,
				meta: {
					title: 'Home',
				}
			},
			{
				path: "/role",
				name: "Role",
				component: Role,
				meta: {
					title: "Role Management",
					isTab: true
				}
			},
			{
				path: '/user',
				name: 'User',
				component: User,
				meta: {
					title: 'User Management',
					isTab: true
				}
			},
			{
				path: '/dept',
				name: 'Dept',
				component: Dept,
				meta: {
					title: 'Department Management',
					isTab: true
				}
			},
			{
				path: '/meeting_room',
				name: 'MeetingRoom',
				component: MeetingRoom,
				meta: {
					title: 'Meeting Room',
					isTab: true
				}
			},
			{
				path: '/offline_meeting',
				name: 'OfflineMeeting',
				component: OfflineMeeting,
				meta: {
					title: 'Offline Meeting',
					isTab: true
				}
			},
			{
				path: '/online_meeting',
				name: 'OnlineMeeting',
				component: OnlineMeeting,
				meta: {
					title: 'Online Meeting',
					isTab: true
				}
			},
			{
				path: '/meeting_video/:meetingId/:uuid',
				name: 'MeetingVideo',
				component: MeetingVideo,
				meta: {
					title: 'MeetingVideo',
					isTab: true
				}
			}, {
				path: '/approval',
				name: 'Approval',
				component: Approval,
				meta: {
					title: 'Approval',
					isTab: true
				}
			},
			{
				path: '/leave',
				name: 'Leave',
				component: Leave,
				meta: {
					title: 'Leave',
					isTab: true
				}
			},
			{
				path: '/amect',
				name: 'Amect',
				component: Amect,
				meta: {
					title: 'Amect',
					isTab: true
				}
			},
			{
				path: '/amect_type',
				name: 'AmectType',
				component: AmectType,
				meta: {
					title: 'AmectType',
					isTab: true
				}
			},
			{
				path: '/amect_report',
				name: 'AmectReport',
				component: AmectReport,
				meta: {
					title: 'AmectReport',
					isTab: true
				}
			},
			{
				path: '/reim',
				name: 'Reim',
				component: Reim,
				meta: {
					title: 'Reimbursement',
					isTab: true
				}
			}
		]
	},
	{
		path: "/404",
		name: "NotFound",
		component: NotFound
	},
	{
		path: '/:pathMatch(.*)*',
		redirect: "/404"
	}
]

const router = createRouter({
	history: createWebHashHistory(),
	routes
})
router.beforeEach((to, from, next) => {
	if (to.name != "Login") {
		let permissions = localStorage.getItem("permissions")
		let token = localStorage.getItem("token")
		if (permissions == null || permissions == ""||token == null || token == "") {
			next({
				name: 'Login'
			})
		}
	}
	return next()
})

export default router
