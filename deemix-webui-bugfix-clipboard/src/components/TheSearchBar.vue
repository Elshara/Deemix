<template>
	<header id="search">
		<div class="search__icon">
			<i class="material-icons">search</i>
		</div>
		<input
			id="searchbar"
			autocomplete="off"
			type="search"
			name="searchbar"
			value=""
			:placeholder="$t('searchbar')"
			autofocus
			ref="searchbar"
			@keyup="handleSearchBarKeyup($event)"
		/>
	</header>
</template>

<script>
import { isValidURL } from '@/utils/utils'
import Downloads from '@/utils/downloads'

import EventBus from '@/utils/EventBus.js'
import { socket } from '@/utils/socket'

export default {
	methods: {
		handleSearchBarKeyup(keyEvent) {
			// Enter key
			if (keyEvent.keyCode !== 13) return

			let term = this.$refs.searchbar.value

			if (isValidURL(term)) {
				if (keyEvent.ctrlKey) {
					this.$root.$emit('QualityModal:open', term)
				} else {
					if (main_selected === 'analyzer_tab') {
						EventBus.$emit('linkAnalyzerTab:reset')
						socket.emit('analyzeLink', term)
					} else {
						Downloads.sendAddToQueue(term)
					}
				}
			} else {
				if (term === '') return

				this.$root.$emit('mainSearch:showNewResults', term, main_selected)
			}
		}
	}
}
</script>

<style>
</style>