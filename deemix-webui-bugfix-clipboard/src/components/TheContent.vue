<template>
	<section id="content" @scroll="handleContentScroll" ref="content">
		<div id="container">
			<ArtistTab />
			<TheChartsTab />
			<TheFavoritesTab />
			<TheErrorsTab />
			<TheHomeTab />
			<TheLinkAnalyzerTab />
			<TheAboutTab />
			<TheSettingsTab />
			<TheMainSearch :scrolled-search-type="newScrolled" />
			<TracklistTab />
		</div>
	</section>
</template>

<script>
import ArtistTab from '@components/ArtistTab.vue'
import TracklistTab from '@components/TracklistTab.vue'

import TheChartsTab from '@components/TheChartsTab.vue'
import TheFavoritesTab from '@components/TheFavoritesTab.vue'
import TheErrorsTab from '@components/TheErrorsTab.vue'
import TheHomeTab from '@components/TheHomeTab.vue'
import TheLinkAnalyzerTab from '@components/TheLinkAnalyzerTab.vue'
import TheAboutTab from '@components/TheAboutTab.vue'
import TheSettingsTab from '@components/TheSettingsTab.vue'
import TheMainSearch from '@components/TheMainSearch.vue'

import { debounce } from '@/utils/utils'
import EventBus from '@/utils/EventBus.js'

export default {
	components: {
		ArtistTab,
		TheChartsTab,
		TheFavoritesTab,
		TheErrorsTab,
		TheHomeTab,
		TheLinkAnalyzerTab,
		TheAboutTab,
		TheSettingsTab,
		TheMainSearch,
		TracklistTab
	},
	data: () => ({
		newScrolled: null
	}),
	methods: {
		handleContentScroll: debounce(async function() {
			if (this.$refs.content.scrollTop + this.$refs.content.clientHeight < this.$refs.content.scrollHeight) return

			if (
				main_selected !== 'search_tab' ||
				['track_search', 'album_search', 'artist_search', 'playlist_search'].indexOf(window.search_selected) === -1
			) {
				return
			}

			this.newScrolled = window.search_selected.split('_')[0]

			await this.$nextTick()

			this.newScrolled = null
		}, 100)
	}
}
</script>

<style>
</style>