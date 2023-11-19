<template>
	<div id="artist_tab" class="main_tabcontent fixed_footer image_header">
		<header
			class="inline-flex"
			:style="{
				'background-image':
					'linear-gradient(to bottom, transparent 0%, var(--main-background) 100%), url(\'' + image + '\')'
			}"
		>
			<h1>{{ title }}</h1>
			<div role="button" aria-label="download" @click.stop="addToQueue" :data-link="link" class="fab right">
				<i class="material-icons" :title="$t('globals.download_hint')">get_app</i>
			</div>
		</header>

		<div class="tab">
			<button
				v-for="(item, name) in body"
				:key="name"
				class="selective"
				:class="{ active: name === currentTab }"
				:href="'#artist_' + name"
				@click="changeTab(name)"
			>
				{{ $tc(`globals.listTabs.${name}`, 2) }}
			</button>
		</div>

		<table class="table">
			<thead>
				<tr>
					<th
						v-for="data in head"
						@click="data.sortKey ? sortBy(data.sortKey) : null"
						:style="{ width: data.width ? data.width : 'auto' }"
						:class="{
							'sort-asc': data.sortKey == sortKey && sortOrder == 'asc',
							'sort-desc': data.sortKey == sortKey && sortOrder == 'desc',
							sortable: data.sortKey,
							clickable: data.sortKey
						}"
					>
						<!-- Need to change this behaviour for translations -->
						{{ data.title }}
					</th>
				</tr>
			</thead>
			<tbody>
				<tr v-for="release in showTable" :key="release.id">
					<td class="inline-flex clickable" @click="albumView" :data-id="release.id">
						<img
							class="rounded coverart"
							:src="release.cover_small"
							style="margin-right: 16px; width: 56px; height: 56px;"
						/>
						<i v-if="release.explicit_lyrics" class="material-icons explicit_icon">
							explicit
						</i>
						{{ release.title }}
						<i v-if="checkNewRelease(release.release_date)" class="material-icons" style="color: #ff7300;">
							fiber_new
						</i>
					</td>
					<td>{{ release.release_date }}</td>
					<td>{{ release.nb_song }}</td>
					<td @click.stop="addToQueue" :data-link="release.link" class="clickable">
						<i class="material-icons" :title="$t('globals.download_hint')">
							file_download
						</i>
					</td>
				</tr>
			</tbody>
		</table>

		<footer>
			<button class="back-button">{{ $t('globals.back') }}</button>
		</footer>
	</div>
</template>

<script>
import { isEmpty, orderBy } from 'lodash-es'
import { socket } from '@/utils/socket'
import Downloads from '@/utils/downloads'
import { showView } from '@js/tabs'
import EventBus from '@/utils/EventBus'

export default {
	name: 'artist-tab',
	data() {
		return {
			currentTab: '',
			sortKey: 'release_date',
			sortOrder: 'desc',
			title: '',
			image: '',
			type: '',
			link: '',
			head: null,
			body: null
		}
	},
	methods: {
		albumView: showView.bind(null, 'album'),
		reset() {
			this.title = 'Loading...'
			this.image = ''
			this.type = ''
			this.currentTab = ''
			this.sortKey = 'release_date'
			this.sortOrder = 'desc'
			this.link = ''
			this.head = []
			this.body = null
		},
		addToQueue(e) {
			e.stopPropagation()
			Downloads.sendAddToQueue(e.currentTarget.dataset.link)
		},
		sortBy(key) {
			if (key == this.sortKey) {
				this.sortOrder = this.sortOrder == 'asc' ? 'desc' : 'asc'
			} else {
				this.sortKey = key
				this.sortOrder = 'asc'
			}
		},
		changeTab(tab) {
			this.currentTab = tab
		},
		getCurrentTab() {
			return this.currentTab
		},
		updateSelected() {
			window.currentStack.selected = this.currentTab
		},
		checkNewRelease(date) {
			let g1 = new Date()
			let g2 = new Date(date)
			g2.setDate(g2.getDate() + 3)
			g1.setHours(0, 0, 0, 0)

			return g1.getTime() <= g2.getTime()
		},
		showArtist(data) {
			const { name, picture_xl, id, releases } = data

			this.title = name
			this.image = picture_xl
			this.type = 'Artist'
			this.link = `https://www.deezer.com/artist/${id}`
			if (this.currentTab === '') this.currentTab = Object.keys(releases)[0]
			this.sortKey = 'release_date'
			this.sortOrder = 'desc'
			this.head = [
				{ title: this.$tc('globals.listTabs.title', 1), sortKey: 'title' },
				{ title: this.$t('globals.listTabs.releaseDate'), sortKey: 'release_date' },
				{ title: this.$tc('globals.listTabs.track', 2), sortKey: 'nb_song' },
				{ title: '', width: '32px' }
			]
			if (isEmpty(releases)) {
				this.body = null
			} else {
				this.body = releases
			}
		}
	},
	computed: {
		showTable() {
			if (this.body) {
				if (this.sortKey == 'nb_song')
					return orderBy(
						this.body[this.currentTab],
						function (o) {
							return new Number(o.nb_song)
						},
						this.sortOrder
					)
				else return orderBy(this.body[this.currentTab], this.sortKey, this.sortOrder)
			} else return []
		}
	},
	mounted() {
		socket.on('show_artist', this.showArtist)

		EventBus.$on('artistTab:reset', this.reset)
		EventBus.$on('artistTab:updateSelected', this.updateSelected)
		EventBus.$on('artistTab:changeTab', this.changeTab)
	}
}
</script>

<style>
</style>
