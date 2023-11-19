<template>
	<div id="errors_tab" class="main_tabcontent">
		<h1>{{ $t('errors.title', [title]) }}</h1>
		<table class="table table--tracklist">
			<tr>
				<th>ID</th>
				<th>{{ $tc('globals.listTabs.artist', 1) }}</th>
				<th>{{ $tc('globals.listTabs.title', 1) }}</th>
				<th>{{ $tc('globals.listTabs.error', 1) }}</th>
			</tr>
			<tr v-for="error in errors" :key="error.data.id">
				<td>{{ error.data.id }}</td>
				<td>{{ error.data.artist }}</td>
				<td>{{ error.data.title }}</td>
				<td>{{ error.errid ? $t(`errors.ids.${error.errid}`) : error.message }}</td>
			</tr>
		</table>
	</div>
</template>

<script>
import { changeTab } from '@js/tabs.js'

import EventBus from '@/utils/EventBus'

export default {
	name: 'the-errors-tab',
	data: () => ({
		title: '',
		errors: []
	}),
	methods: {
		reset() {
			this.title = ''
			this.errors = []
		},
		showErrors(data, eventTarget) {
			this.title = data.artist + ' - ' + data.title
			this.errors = data.errors

			changeTab(eventTarget, 'main', 'errors_tab')
		}
	},
	mounted() {
		EventBus.$on('showTabErrors', this.showErrors)
		this.$root.$on('showTabErrors', this.showErrors)
	}
}
</script>

<style>
</style>
