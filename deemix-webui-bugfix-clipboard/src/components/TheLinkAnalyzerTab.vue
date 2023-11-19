<template>
	<div id="analyzer_tab" class="main_tabcontent image_header">
		<h2 class="page_heading page_heading--capitalize">{{ $t('sidebar.linkAnalyzer') }}</h2>
		<div v-if="link == ''">
			<p>
				{{ $t('linkAnalyzer.info') }}
			</p>
			<p>
				{{ $t('linkAnalyzer.useful') }}
			</p>
		</div>
		<div v-else-if="link == 'error'">
			<h2>{{ $t('linkAnalyzer.linkNotSupported') }}</h2>
			<p>{{ $t('linkAnalyzer.linkNotSupportedYet') }}</p>
		</div>
		<div v-else>
			<header
				class="inline-flex"
				:style="{
					'background-image':
						'linear-gradient(to bottom, transparent 0%, var(--main-background) 100%), url(\'' + image + '\')'
				}"
			>
				<div>
					<h1>{{ title }}</h1>
					<h2 v-if="type == 'track'">
						<i18n path="globals.by" tag="span">
							<span place="0" class="clickable" @click="artistView" :data-id="data.artist.id">{{ data.artist.name }}</span>
						</i18n>
						 •
						<i18n path="globals.in" tag="span">
							<span place="0" class="clickable" @click="albumView" :data-id="data.album.id">{{ data.album.title }}</span>
						</i18n>
					</h2>
					<h2 v-else-if="type == 'album'">
						<i18n path="globals.by" tag="span">
							<span place="0" class="clickable" @click="artistView" :data-id="data.artist.id">{{ data.artist.name }}</span>
						</i18n>
						{{ ` • ${$tc('globals.listTabs.trackN', data.nb_tracks)}` }}
					</h2>
				</div>
				<div
					role="button"
					aria-label="download"
					@contextmenu.prevent="openQualityModal"
					@click.stop="addToQueue"
					:data-link="link"
					class="fab right"
				>
					<i class="material-icons" :title="$t('globals.download_hint')">get_app</i>
				</div>
			</header>
			<table class="table">
				<tr v-if="data.id">
					<td>{{ $t('linkAnalyzer.table.id') }}</td>
					<td>{{ data.id }}</td>
				</tr>
				<tr v-if="data.isrc">
					<td>{{ $t('linkAnalyzer.table.isrc') }}</td>
					<td>{{ data.isrc }}</td>
				</tr>
				<tr v-if="data.upc">
					<td>{{ $t('linkAnalyzer.table.upc') }}</td>
					<td>{{ data.upc }}</td>
				</tr>
				<tr v-if="data.duration">
					<td>{{ $t('linkAnalyzer.table.duration') }}</td>
					<td>{{ convertDuration(data.duration) }}</td>
				</tr>
				<tr v-if="data.disk_number">
					<td>{{ $t('linkAnalyzer.table.diskNumber') }}</td>
					<td>{{ data.disk_number }}</td>
				</tr>
				<tr v-if="data.track_position">
					<td>{{ $t('linkAnalyzer.table.trackNumber') }}</td>
					<td>{{ data.track_position }}</td>
				</tr>
				<tr v-if="data.release_date">
					<td>{{ $t('linkAnalyzer.table.releaseDate') }}</td>
					<td>{{ data.release_date }}</td>
				</tr>
				<tr v-if="data.bpm">
					<td>{{ $t('linkAnalyzer.table.bpm') }}</td>
					<td>{{ data.bpm }}</td>
				</tr>
				<tr v-if="data.label">
					<td>{{ $t('linkAnalyzer.table.label') }}</td>
					<td>{{ data.label }}</td>
				</tr>
				<tr v-if="data.record_type">
					<td>{{ $t('linkAnalyzer.table.recordType') }}</td>
					<td>{{ $tc(`globals.listTabs.${data.record_type}`, 1) }}</td>
				</tr>
				<tr v-if="data.genres && data.genres.data.length">
					<td>{{ $t('linkAnalyzer.table.genres') }}</td>
					<td>{{ data.genres.data.map(x => x.name).join('; ') }}</td>
				</tr>
			</table>

			<div v-if="type == 'album'">
				<button @click="albumView" :data-id="id">{{ $t('linkAnalyzer.table.tracklist') }}</button>
			</div>
			<div v-if="countries.length">
				<p v-for="country in countries">{{ country[0] }} - {{ country[1] }}</p>
			</div>
		</div>
	</div>
</template>

<script>
import { socket } from '@/utils/socket'
import { showView } from '@js/tabs.js'
import Utils from '@/utils/utils'
import EventBus from '@/utils/EventBus'

export default {
	name: 'the-link-analyzer-tab',
	data() {
		return {
			title: '',
			subtitle: '',
			image: '',
			data: {},
			type: '',
			link: '',
			id: '0',
			countries: []
		}
	},
	methods: {
		artistView: showView.bind(null, 'artist'),
		albumView: showView.bind(null, 'album'),
		convertDuration: Utils.convertDuration,
		reset() {
			this.title = 'Loading...'
			this.subtitle = ''
			this.image = ''
			this.data = {}
			this.type = ''
			this.link = ''
			this.countries = []
		},
		showTrack(data) {
			const {
				title,
				title_version,
				album: { cover_xl },
				link,
				available_countries,
				id
			} = data

			this.title = title + (title_version && title.indexOf(title_version) == -1 ? ' ' + title_version : '')
			this.image = cover_xl
			this.type = 'track'
			this.link = link
			this.id = id

			available_countries.forEach(cc => {
				let temp = []
				let chars = [...cc].map(c => c.charCodeAt() + 127397)
				temp.push(String.fromCodePoint(...chars))
				temp.push(Utils.COUNTRIES[cc])
				this.countries.push(temp)
			})

			this.data = data
		},
		showAlbum(data) {
			const { title, cover_xl, link, id } = data

			this.title = title
			this.image = cover_xl
			this.type = 'album'
			this.link = link
			this.data = data
			this.id = id
		},
		notSupported() {
			this.link = 'error'
		}
	},
	mounted() {
		EventBus.$on('linkAnalyzerTab:reset', this.reset)

		socket.on('analyze_track', this.showTrack)
		socket.on('analyze_album', this.showAlbum)
		socket.on('analyze_notSupported', this.notSupported)
	}
}
</script>

<style>
</style>
