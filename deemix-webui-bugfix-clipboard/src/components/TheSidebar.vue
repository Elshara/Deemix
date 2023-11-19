<template>
	<aside id="sidebar" role="navigation" @click="handleSidebarClick">
		<span id="main_home_tablink" class="main_tablinks" role="link" aria-label="home">
			<i class="material-icons side_icon">home</i>
			<span class="main_tablinks_text">{{ $t('sidebar.home') }}</span>
		</span>
		<span id="main_search_tablink" class="main_tablinks" role="link" aria-label="search">
			<i class="material-icons side_icon">search</i>
			<span class="main_tablinks_text">{{ $t('sidebar.search') }}</span>
		</span>
		<span id="main_charts_tablink" class="main_tablinks" role="link" aria-label="charts">
			<i class="material-icons side_icon">bubble_chart</i>
			<span class="main_tablinks_text">{{ $t('sidebar.charts') }}</span>
		</span>
		<span id="main_favorites_tablink" class="main_tablinks" role="link" aria-label="favorites">
			<i class="material-icons side_icon">album</i>
			<span class="main_tablinks_text">{{ $t('sidebar.favorites') }}</span>
		</span>
		<span id="main_analyzer_tablink" class="main_tablinks" role="link" aria-label="link analyzer">
			<i class="material-icons side_icon">link</i>
			<span class="main_tablinks_text">{{ $t('sidebar.linkAnalyzer') }}</span>
		</span>
		<span id="main_settings_tablink" class="main_tablinks" role="link" aria-label="settings">
			<i class="material-icons side_icon">settings</i>
			<span class="main_tablinks_text">{{ $t('sidebar.settings') }}</span>
		</span>
		<span id="main_about_tablink" class="main_tablinks" role="link" aria-label="info">
			<i class="material-icons side_icon">info</i>
			<span class="main_tablinks_text">{{ $t('sidebar.about') }}</span>
		</span>
		<span id="theme_selector" class="main_tablinks" role="link" aria-label="theme selector">
			<i class="material-icons side_icon side_icon--theme">palette</i>
			<div id="theme_togglers">
				<div
					v-for="theme of themes"
					:key="theme"
					class="theme_toggler "
					:class="[{ 'theme_toggler--active': activeTheme === theme }, `theme_toggler--${theme}`]"
					@click="changeTheme(theme)"
				></div>
			</div>
		</span>
		<div id="network-status" :class="{ online: appOnline, offline: !appOnline }">
			<i v-if="appOnline" class="material-icons">wifi</i>
			<i v-else class="material-icons">
				<!-- wifi_off icon not working, maybe need to include it? -->
				<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
					<path d="M24 .01c0-.01 0-.01 0 0L0 0v24h24V.01zM0 0h24v24H0V0zm0 0h24v24H0V0z" fill="none" />
					<path
						d="M22.99 9C19.15 5.16 13.8 3.76 8.84 4.78l2.52 2.52c3.47-.17 6.99 1.05 9.63 3.7l2-2zm-4 4c-1.29-1.29-2.84-2.13-4.49-2.56l3.53 3.53.96-.97zM2 3.05L5.07 6.1C3.6 6.82 2.22 7.78 1 9l1.99 2c1.24-1.24 2.67-2.16 4.2-2.77l2.24 2.24C7.81 10.89 6.27 11.73 5 13v.01L6.99 15c1.36-1.36 3.14-2.04 4.92-2.06L18.98 20l1.27-1.26L3.29 1.79 2 3.05zM9 17l3 3 3-3c-1.65-1.66-4.34-1.66-6 0z"
					/>
				</svg>
			</i>
		</div>
	</aside>
</template>

<style scoped>
#network-status {
	display: flex;
	justify-content: center;
	align-items: center;
	position: relative;
	margin-top: auto;
	bottom: 0;
}

#network-status.online i.material-icons {
	color: hsl(151, 100%, 31%);
}

#network-status.offline i.material-icons svg {
	fill: red;
	width: 1em;
	height: 1em;
}
</style>

<script>
import { changeTab } from '@js/tabs.js'

export default {
	name: 'the-sidebar',
	data: () => ({
		appOnline: null,
		activeTheme: 'light',
		themes: ['purple', 'dark', 'light']
	}),
	mounted() {
		/* === Online status handling === */
		this.appOnline = navigator.onLine

		window.addEventListener('online', () => {
			this.appOnline = true
		})

		window.addEventListener('offline', () => {
			this.appOnline = false
		})

		/* === Current theme handling === */
		this.activeTheme = localStorage.getItem('selectedTheme') || 'light'
	},
	methods: {
		changeTheme(newTheme) {
			if (newTheme === this.activeTheme) return

			this.activeTheme = newTheme
			document.documentElement.setAttribute('data-theme', newTheme)
			localStorage.setItem('selectedTheme', newTheme)

			// Animating everything to have a smoother theme switch
			document.querySelectorAll('*').forEach(el => {
				el.style.transition = 'all 200ms ease-in-out'
			})

			document.documentElement.addEventListener('transitionend', function transitionHandler() {
				document.querySelectorAll('*').forEach(el => {
					el.style.transition = ''
				})

				document.documentElement.removeEventListener('transitionend', transitionHandler)
			})
		},
		/**
		 * Handles click Event on the sidebar and changes tab
		 * according to clicked icon.
		 * Uses event delegation
		 * @param		{Event}		event
		 */
		handleSidebarClick(event) {
			const { target } = event

			const wantToChangeTab = target.matches('.main_tablinks') || target.parentElement.matches('.main_tablinks')

			if (!wantToChangeTab) return

			let sidebarEl = target.matches('.main_tablinks') ? target : target.parentElement
			let targetID = sidebarEl.id
			let selectedTab = null

			switch (targetID) {
				case 'main_search_tablink':
					selectedTab = 'search_tab'
					break
				case 'main_home_tablink':
					selectedTab = 'home_tab'
					break
				case 'main_charts_tablink':
					selectedTab = 'charts_tab'
					break
				case 'main_favorites_tablink':
					selectedTab = 'favorites_tab'
					break
				case 'main_analyzer_tablink':
					selectedTab = 'analyzer_tab'
					break
				case 'main_settings_tablink':
					selectedTab = 'settings_tab'
					break
				case 'main_about_tablink':
					selectedTab = 'about_tab'
					break

				default:
					break
			}

			if (!selectedTab) return

			changeTab(sidebarEl, 'main', selectedTab)
		}
	}
}
</script>

