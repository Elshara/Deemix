import Vue from 'vue'
import VueRouter from 'vue-router'

import TracklistTab from '@components/TracklistTab.vue'

Vue.use(VueRouter)

const routes = [
	{
		path: '/tracklist/:id',
		component: TracklistTab
	},
	// 404
	{
		path: '*',
		component: TracklistTab
	}
]

const router = new VueRouter({
	mode: 'history',
	// linkActiveClass: 'open',
	routes,
	scrollBehavior(to, from, savedPosition) {
		return { x: 0, y: 0 }
	}
})

router.beforeEach((to, from, next) => {
	next()
})

export default router
