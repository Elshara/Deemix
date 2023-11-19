<template>
	<div id="charts_tab" class="main_tabcontent">
		<h2 class="page_heading">{{ $t('charts.title') }}</h2>
		<div v-if="country === ''" id="charts_selection">
			<div class="release_grid charts_grid">
				<template v-for="release in countries">
					<div
						role="button"
						:aria-label="release.title"
						v-if="release.title === 'Worldwide'"
						class="release clickable"
						@click="getTrackList"
						:data-title="release.title"
						:data-id="release.id"
						:key="release.id"
					>
						<img class="rounded coverart" :src="release.picture_medium" />
					</div>
				</template>

				<template v-for="release in countries">
					<div
						role="button"
						:aria-label="release.title"
						v-if="release.title !== 'Worldwide'"
						class="release clickable"
						@click="getTrackList"
						:data-title="release.title"
						:data-id="release.id"
						:key="release.id"
					>
						<img class="rounded coverart" :src="release.picture_medium" />
					</div>
				</template>
			</div>
		</div>
		<div v-else id="charts_table">
			<button @click="changeCountry">{{ $t('charts.changeCountry') }}</button>
			<button @click.stop="addToQueue" :data-link="'https://www.deezer.com/playlist/' + id">
				{{ $t('charts.download') }}
			</button>
			<table class="table table--charts">
				<tbody>
					<tr v-for="track in chart" class="track_row">
						<td class="top-tracks-position" :class="{ first: track.position === 1 }">
							{{ track.position }}
						</td>
						<td class="table__icon table__icon--big">
							<a
								href="#"
								@click="playPausePreview"
								class="rounded"
								:class="{ 'single-cover': track.preview }"
								:data-preview="track.preview"
							>
								<i
									@mouseenter="previewMouseEnter"
									@mouseleave="previewMouseLeave"
									v-if="track.preview"
									class="material-icons preview_controls"
									:title="$t('globals.play_hint')"
								>
									play_arrow
								</i>
								<img class="rounded coverart" :src="track.album.cover_small" />
							</a>
						</td>
						<td class="table__cell--large breakline">
							{{
								track.title +
								(track.title_version && track.title.indexOf(track.title_version) == -1 ? ' ' + track.title_version : '')
							}}
						</td>
						<td
							class="table__cell--medium table__cell--center breakline clickable"
							@click="artistView"
							:data-id="track.artist.id"
						>
							{{ track.artist.name }}
						</td>
						<td
							class="table__cell--medium table__cell--center breakline clickable"
							@click="albumView"
							:data-id="track.album.id"
						>
							{{ track.album.title }}
						</td>
						<td class="table__cell--small table__cell--center">
							{{ convertDuration(track.duration) }}
						</td>
						<td
							class="table__cell--download"
							@click.stop="addToQueue"
							:data-link="track.link"
							role="button"
							aria-label="download"
						>
							<i class="material-icons" :title="$t('globals.download_hint')">get_app</i>
						</td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</template>

<script>
import { socket } from '@/utils/socket'
import { showView } from '@js/tabs.js'
import Downloads from '@/utils/downloads'
import Utils from '@/utils/utils'

import EventBus from '@/utils/EventBus'

export default {
	name: 'the-charts-tab',
	data() {
		return {
			country: '',
			id: 0,
			countries: [],
			chart: []
		}
	},
	methods: {
		artistView: showView.bind(null, 'artist'),
		albumView: showView.bind(null, 'album'),
		playPausePreview(e) {
			EventBus.$emit('trackPreview:playPausePreview', e)
		},
		previewMouseEnter(e) {
			EventBus.$emit('trackPreview:previewMouseEnter', e)
		},
		previewMouseLeave(e) {
			EventBus.$emit('trackPreview:previewMouseLeave', e)
		},
		convertDuration: Utils.convertDuration,
		addToQueue(e) {
			e.stopPropagation()
			Downloads.sendAddToQueue(e.currentTarget.dataset.link)
		},
		getTrackList(event) {
			document.getElementById('content').scrollTo(0, 0)

			const {
				currentTarget: {
					dataset: { title }
				},
				currentTarget: {
					dataset: { id }
				}
			} = event

			this.country = title
			localStorage.setItem('chart', this.country)
			this.id = id
			socket.emit('getChartTracks', this.id)
		},
		setTracklist(data) {
			this.chart = data
		},
		changeCountry() {
			this.country = ''
			this.id = 0
		},
		initCharts(data) {
			this.countries = data
			this.country = localStorage.getItem('chart') || ''

			if (!this.country) return

			let i = 0
			for (; i < this.countries.length; i++) {
				if (this.countries[i].title == this.country) break
			}

			if (i !== this.countries.length) {
				this.id = this.countries[i].id
				socket.emit('getChartTracks', this.id)
			} else {
				this.country = ''
				localStorage.setItem('chart', this.country)
			}
		}
	},
	mounted() {
		socket.on('init_charts', this.initCharts)
		socket.on('setChartTracks', this.setTracklist)
	}
}
</script>

<style>
</style>