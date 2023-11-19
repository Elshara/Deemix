<template>
	<div id="search_tab" class="main_tabcontent" @click="handleSearchTabClick">
		<div :class="{ hide: results.query != '' }">
			<h2>{{ $t('search.startSearching') }}</h2>
			<p>{{ $t('search.description') }}</p>
		</div>
		<div v-show="results.query !== ''">
			<ul class="section-tabs">
				<li class="section-tabs__tab search_tablinks" id="search_all_tab">{{ $t('globals.listTabs.all') }}</li>
				<li class="section-tabs__tab search_tablinks" id="search_track_tab">{{ $tc('globals.listTabs.track', 2) }}</li>
				<li class="section-tabs__tab search_tablinks" id="search_album_tab">{{ $tc('globals.listTabs.album', 2) }}</li>
				<li class="section-tabs__tab search_tablinks" id="search_artist_tab">
					{{ $tc('globals.listTabs.artist', 2) }}
				</li>
				<li class="section-tabs__tab search_tablinks" id="search_playlist_tab">
					{{ $tc('globals.listTabs.playlist', 2) }}
				</li>
			</ul>
			<div id="search_tab_content">
				<!-- ### Main Search Tab ### -->
				<div id="main_search" class="search_tabcontent">
					<template v-for="section in results.allTab.ORDER">
						<section
							v-if="
								(section != 'TOP_RESULT' && results.allTab[section].data.length > 0) ||
								results.allTab[section].length > 0
							"
							class="search_section"
						>
							<h2
								@click="changeSearchTab(section)"
								class="search_header"
								:class="{ top_result_header: section === 'TOP_RESULT' }"
							>
								{{ $tc(`globals.listTabs.${section.toLowerCase()}`, 2) }}
							</h2>
							<!-- Top result -->
							<div
								v-if="section == 'TOP_RESULT'"
								class="top_result clickable"
								@click="handleClickTopResult"
								:data-id="results.allTab.TOP_RESULT[0].id"
							>
								<div class="cover_container">
									<img
										aria-hidden="true"
										:src="results.allTab.TOP_RESULT[0].picture"
										:class="(results.allTab.TOP_RESULT[0].type == 'artist' ? 'circle' : 'rounded') + ' coverart'"
									/>
									<div
										role="button"
										aria-label="download"
										@click.stop="addToQueue"
										:data-link="results.allTab.TOP_RESULT[0].link"
										class="download_overlay"
									>
										<i class="material-icons" :title="$t('globals.download_hint')">get_app</i>
									</div>
								</div>
								<div class="info_box">
									<p class="primary-text">{{ results.allTab.TOP_RESULT[0].title }}</p>
									<p class="secondary-text">
										{{
											results.allTab.TOP_RESULT[0].type == 'artist'
												? $t('search.fans', [$n(results.allTab.TOP_RESULT[0].nb_fan)])
												: $t('globals.by', [results.allTab.TOP_RESULT[0].artist]) +
												  ' - ' +
												  $tc('globals.listTabs.trackN', results.allTab.TOP_RESULT[0].nb_song)
										}}
									</p>
									<span class="tag">{{ $tc(`globals.listTabs.${results.allTab.TOP_RESULT[0].type}`, 1) }}</span>
								</div>
							</div>
							<div v-else-if="section == 'TRACK'">
								<table class="table table--tracks">
									<tbody>
										<tr v-for="track in results.allTab.TRACK.data.slice(0, 6)">
											<td class="table__icon" aria-hidden="true">
												<img
													class="rounded coverart"
													:src="
														'https://e-cdns-images.dzcdn.net/images/cover/' +
														track.ALB_PICTURE +
														'/32x32-000000-80-0-0.jpg'
													"
												/>
											</td>
											<td class="table__cell table__cell--large breakline">
												<div class="table__cell-content table__cell-content--vertical-center">
													<i v-if="track.EXPLICIT_LYRICS == 1" class="material-icons explicit_icon">
														explicit
													</i>
													{{ track.SNG_TITLE + (track.VERSION ? ' ' + track.VERSION : '') }}
												</div>
											</td>
											<td class="table__cell table__cell--medium table__cell--center breakline">
												<span
													class="clickable"
													@click="artistView"
													:data-id="artist.ART_ID"
													v-for="artist in track.ARTISTS"
													>{{ artist.ART_NAME }}
												</span>
											</td>
											<td
												class="table__cell--medium table__cell--center breakline clickable"
												@click="albumView"
												:data-id="track.ALB_ID"
											>
												{{ track.ALB_TITLE }}
											</td>
											<td class="table__cell table__cell--center">
												{{ convertDuration(track.DURATION) }}
											</td>
											<td
												class="table__cell--download table__cell--center clickable"
												@click.stop="addToQueue"
												:data-link="'https://www.deezer.com/track/' + track.SNG_ID"
												role="button"
												aria-label="download"
											>
												<i class="material-icons" :title="$t('globals.download_hint')">
													get_app
												</i>
											</td>
										</tr>
									</tbody>
								</table>
							</div>
							<div v-else-if="section == 'ARTIST'" class="release_grid firstrow_only">
								<div
									v-for="release in results.allTab.ARTIST.data.slice(0, 10)"
									class="release clickable"
									@click="artistView"
									:data-id="release.ART_ID"
								>
									<div class="cover_container">
										<img
											aria-hidden="true"
											class="circle coverart"
											:src="
												'https://e-cdns-images.dzcdn.net/images/artist/' +
												release.ART_PICTURE +
												'/156x156-000000-80-0-0.jpg'
											"
										/>
										<div
											role="button"
											aria-label="download"
											@click.stop="addToQueue"
											:data-link="'https://deezer.com/artist/' + release.ART_ID"
											class="download_overlay"
										>
											<i class="material-icons" :title="$t('globals.download_hint')">get_app</i>
										</div>
									</div>
									<p class="primary-text">{{ release.ART_NAME }}</p>
									<p class="secondary-text">{{ $t('search.fans', [$n(release.NB_FAN)]) }}</p>
								</div>
							</div>
							<div v-else-if="section == 'ALBUM'" class="release_grid firstrow_only">
								<div
									v-for="release in results.allTab.ALBUM.data.slice(0, 10)"
									class="release clickable"
									@click="albumView"
									:data-id="release.ALB_ID"
								>
									<div class="cover_container">
										<img
											aria-hidden="true"
											class="rounded coverart"
											:src="
												'https://e-cdns-images.dzcdn.net/images/cover/' +
												release.ALB_PICTURE +
												'/156x156-000000-80-0-0.jpg'
											"
										/>
										<div
											role="button"
											aria-label="download"
											@click.stop="addToQueue"
											:data-link="'https://deezer.com/album/' + release.ALB_ID"
											class="download_overlay"
										>
											<i class="material-icons" :title="$t('globals.download_hint')">get_app</i>
										</div>
									</div>
									<p class="primary-text inline-flex">
										<i
											v-if="[1, 4].indexOf(release.EXPLICIT_ALBUM_CONTENT.EXPLICIT_LYRICS_STATUS) != -1"
											class="material-icons explicit_icon"
											>explicit</i
										>
										{{ release.ALB_TITLE }}
									</p>
									<p class="secondary-text">
										{{ release.ART_NAME + ' - ' + $tc('globals.listTabs.trackN', release.NUMBER_TRACK) }}
									</p>
								</div>
							</div>
							<div v-else-if="section == 'PLAYLIST'" class="release_grid firstrow_only">
								<div
									v-for="release in results.allTab.PLAYLIST.data.slice(0, 10)"
									class="release clickable"
									@click="playlistView"
									:data-id="release.PLAYLIST_ID"
								>
									<div class="cover_container">
										<img
											aria-hidden="true"
											class="rounded coverart"
											:src="
												'https://e-cdns-images.dzcdn.net/images/' +
												release.PICTURE_TYPE +
												'/' +
												release.PLAYLIST_PICTURE +
												'/156x156-000000-80-0-0.jpg'
											"
										/>
										<div
											role="button"
											aria-label="download"
											@click.stop="addToQueue"
											:data-link="'https://deezer.com/playlist/' + release.PLAYLIST_ID"
											class="download_overlay"
										>
											<i class="material-icons" :title="$t('globals.download_hint')">get_app</i>
										</div>
									</div>
									<p class="primary-text">{{ release.TITLE }}</p>
									<p class="secondary-text">{{ $tc('globals.listTabs.trackN', release.NB_SONG) }}</p>
								</div>
							</div>
						</section>
					</template>
					<div
						v-if="
							results.allTab.ORDER.every(section =>
								section == 'TOP_RESULT' ? results.allTab[section].length == 0 : results.allTab[section].data.length == 0
							)
						"
					>
						<h1>{{ $t('search.noResults') }}</h1>
					</div>
				</div>
				<!-- ### Track Search Tab ### -->
				<div id="track_search" class="search_tabcontent">
					<base-loading-placeholder v-if="!results.trackTab.loaded"></base-loading-placeholder>
					<div v-else-if="results.trackTab.data.length == 0">
						<h1>{{ $t('search.noResultsTrack') }}</h1>
					</div>
					<table class="table table--tracks" v-if="results.trackTab.data.length > 0">
						<thead>
							<tr>
								<th colspan="2">{{ $tc('globals.listTabs.title', 1) }}</th>
								<th>{{ $tc('globals.listTabs.artist', 1) }}</th>
								<th>{{ $tc('globals.listTabs.album', 1) }}</th>
								<th>
									<i class="material-icons">
										timer
									</i>
								</th>
								<th style="width: 56px;"></th>
							</tr>
						</thead>
						<tbody>
							<tr v-for="track in results.trackTab.data">
								<td class="table__icon table__icon--big">
									<a
										href="#"
										@click="playPausePreview"
										:class="'rounded' + (track.preview ? ' single-cover' : '')"
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
								<td class="table__cell table__cell--large breakline">
									<div class="table__cell-content table__cell-content--vertical-center">
										<i v-if="track.explicit_lyrics" class="material-icons explicit_icon">
											explicit
										</i>
										{{
											track.title +
											(track.title_version && track.title.indexOf(track.title_version) == -1
												? ' ' + track.title_version
												: '')
										}}
									</div>
								</td>
								<td
									class="table__cell table__cell--medium table__cell--center breakline clickable"
									@click="artistView"
									:data-id="track.artist.id"
								>
									{{ track.artist.name }}
								</td>
								<td
									class="table__cell table__cell--medium table__cell--center breakline clickable"
									@click="albumView"
									:data-id="track.album.id"
								>
									{{ track.album.title }}
								</td>
								<td class="table__cell table__cell--small table__cell--center">
									{{ convertDuration(track.duration) }}
								</td>
								<td
									class="table__cell--download table__cell--center clickable"
									@click.stop="addToQueue"
									:data-link="track.link"
									role="button"
									aria-label="download"
								>
									<i class="material-icons" :title="$t('globals.download_hint')">
										get_app
									</i>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
				<!-- ### Album Search Tab ### -->
				<div id="album_search" class="search_tabcontent">
					<base-loading-placeholder v-if="!results.albumTab.loaded"></base-loading-placeholder>
					<div v-else-if="results.albumTab.data.length == 0">
						<h1>{{ $t('search.noResultsAlbum') }}</h1>
					</div>
					<div class="release_grid" v-if="results.albumTab.data.length > 0">
						<div
							v-for="release in results.albumTab.data"
							class="release clickable"
							@click="albumView"
							:data-id="release.id"
						>
							<div class="cover_container">
								<img aria-hidden="true" class="rounded coverart" :src="release.cover_medium" />
								<div
									role="button"
									aria-label="download"
									@click.stop="addToQueue"
									:data-link="release.link"
									class="download_overlay"
								>
									<i class="material-icons" :title="$t('globals.download_hint')">get_app</i>
								</div>
							</div>
							<p class="primary-text inline-flex">
								<i v-if="release.explicit_lyrics" class="material-icons explicit_icon">explicit</i>
								{{ release.title }}
							</p>
							<p class="secondary-text">
								{{
									$t('globals.by', [release.artist.name]) + ' - ' + $tc('globals.listTabs.trackN', release.nb_tracks)
								}}
							</p>
						</div>
					</div>
				</div>
				<!-- ### Artist Search Tab ### -->
				<div id="artist_search" class="search_tabcontent">
					<base-loading-placeholder v-if="!results.artistTab.loaded"></base-loading-placeholder>
					<div v-else-if="results.artistTab.data.length == 0">
						<h1>{{ $t('search.noResultsArtist') }}</h1>
					</div>
					<div class="release_grid" v-if="results.artistTab.data.length > 0">
						<div
							v-for="release in results.artistTab.data"
							class="release clickable"
							@click="artistView"
							:data-id="release.id"
						>
							<div class="cover_container">
								<img aria-hidden="true" class="circle coverart" :src="release.picture_medium" />
								<div
									role="button"
									aria-label="download"
									@click.stop="addToQueue"
									:data-link="release.link"
									class="download_overlay"
								>
									<i class="material-icons" :title="$t('globals.download_hint')">get_app</i>
								</div>
							</div>
							<p class="primary-text">{{ release.name }}</p>
							<p class="secondary-text">{{ $tc('globals.listTabs.releaseN', release.nb_album) }}</p>
						</div>
					</div>
				</div>
				<!-- ### Playlist Search Tab ### -->
				<div id="playlist_search" class="search_tabcontent">
					<base-loading-placeholder v-if="!results.playlistTab.loaded"></base-loading-placeholder>
					<div v-else-if="results.playlistTab.data.length == 0">
						<h1>{{ $t('search.noResultsPlaylist') }}</h1>
					</div>
					<div class="release_grid" v-if="results.playlistTab.data.length > 0">
						<div
							v-for="release in results.playlistTab.data"
							class="release clickable"
							@click="playlistView"
							:data-id="release.id"
						>
							<div class="cover_container">
								<img aria-hidden="true" class="rounded coverart" :src="release.picture_medium" />
								<div
									role="button"
									aria-label="download"
									@click.stop="addToQueue"
									:data-link="release.link"
									class="download_overlay"
								>
									<i class="material-icons" :title="$t('globals.download_hint')">get_app</i>
								</div>
							</div>
							<p class="primary-text">{{ release.title }}</p>
							<p class="secondary-text">
								{{ `${$t('globals.by', [release.user.name])} - ${$tc('globals.listTabs.trackN', release.nb_tracks)}` }}
							</p>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script>
import { socket } from '@/utils/socket'
import { showView } from '@js/tabs.js'
import Downloads from '@/utils/downloads'
import Utils from '@/utils/utils'
import BaseLoadingPlaceholder from '@components/BaseLoadingPlaceholder.vue'

import { changeTab } from '@js/tabs.js'
import EventBus from '@/utils/EventBus.js'

export default {
	name: 'the-main-search-tab',
	components: {
		BaseLoadingPlaceholder
	},
	data() {
		return {
			results: {
				query: '',
				allTab: {
					ORDER: [],
					TOP_RESULT: [],
					ALBUM: {},
					ARTIST: {},
					TRACK: {},
					PLAYLIST: {}
				},
				trackTab: {
					data: [],
					next: 0,
					total: 0,
					loaded: false
				},
				albumTab: {
					data: [],
					next: 0,
					total: 0,
					loaded: false
				},
				artistTab: {
					data: [],
					next: 0,
					total: 0,
					loaded: false
				},
				playlistTab: {
					data: [],
					next: 0,
					total: 0,
					loaded: false
				}
			}
		}
	},
	props: {
		scrolledSearchType: {
			type: String,
			required: false
		}
	},
	mounted() {
		EventBus.$on('mainSearch:checkLoadMoreContent', this.checkLoadMoreContent)

		this.$root.$on('mainSearch:showNewResults', this.showNewResults)
		socket.on('mainSearch', this.handleMainSearch)
		socket.on('search', this.handleSearch)
	},
	methods: {
		artistView: showView.bind(null, 'artist'),
		albumView: showView.bind(null, 'album'),
		playlistView: showView.bind(null, 'playlist'),
		playPausePreview(e) {
			EventBus.$emit('trackPreview:playPausePreview', e)
		},
		previewMouseEnter(e) {
			EventBus.$emit('trackPreview:previewMouseEnter', e)
		},
		previewMouseLeave(e) {
			EventBus.$emit('trackPreview:previewMouseLeave', e)
		},
		handleSearchTabClick(event) {
			const {
				target,
				target: { id }
			} = event
			let selectedTab = null

			switch (id) {
				case 'search_all_tab':
					selectedTab = 'main_search'
					break
				case 'search_track_tab':
					selectedTab = 'track_search'
					break
				case 'search_album_tab':
					selectedTab = 'album_search'
					break
				case 'search_artist_tab':
					selectedTab = 'artist_search'
					break
				case 'search_playlist_tab':
					selectedTab = 'playlist_search'
					break

				default:
					break
			}

			if (!selectedTab) return

			changeTab(target, 'search', selectedTab)
		},
		handleClickTopResult(event) {
			let topResultType = this.results.allTab.TOP_RESULT[0].type

			switch (topResultType) {
				case 'artist':
					this.artistView(event)
					break
				case 'album':
					this.albumView(event)
					break
				case 'playlist':
					this.playlistView(event)
					break

				default:
					break
			}
		},
		showNewResults(term, mainSelected) {
			if (term !== this.results.query || mainSelected == 'search_tab') {
				document.getElementById('search_tab_content').style.display = 'none'
				socket.emit('mainSearch', { term })

				// Showing loading placeholder
				document.getElementById('content').style.display = 'none'
				document.getElementById('search_placeholder').classList.toggle('loading_placeholder--hidden')
			} else {
				document.getElementById('search_tab_content').style.display = 'block'
				document.getElementById('main_search_tablink').click()
			}
		},
		checkLoadMoreContent(searchSelected) {
			if (this.results[searchSelected.split('_')[0] + 'Tab'].data.length !== 0) return

			this.search(searchSelected.split('_')[0])
		},
		changeSearchTab(section) {
			if (section === 'TOP_RESULT') return

			let tabID

			// Using the switch beacuse it's tricky to find refernces of the belo IDs
			switch (section) {
				case 'TRACK':
					tabID = 'search_track_tab'
					break
				case 'ALBUM':
					tabID = 'search_album_tab'
					break
				case 'ARTIST':
					tabID = 'search_artist_tab'
					break
				case 'PLAYLIST':
					tabID = 'search_playlist_tab'
					break

				default:
					break
			}

			document.getElementById(tabID).click()
		},
		addToQueue(e) {
			Downloads.sendAddToQueue(e.currentTarget.dataset.link)
		},
		numberWithDots: Utils.numberWithDots,
		convertDuration: Utils.convertDuration,
		search(type) {
			socket.emit('search', {
				term: this.results.query,
				type: type,
				start: this.results[type + 'Tab'].next,
				nb: 30
			})
		},
		scrolledSearch(type) {
			let currentTab = type + 'Tab'

			if (this.results[currentTab].next < this.results[currentTab].total) {
				socket.emit('search', {
					term: this.results.query,
					type: type,
					start: this.results[currentTab].next,
					nb: 30
				})
			}
		},
		handleMainSearch(result) {
			// Hiding loading placeholder
			document.getElementById('content').style.display = ''
			document.getElementById('search_placeholder').classList.toggle('loading_placeholder--hidden')

			let resetObj = { data: [], next: 0, total: 0, loaded: false }

			this.results.allTab = result
			this.results.trackTab = { ...resetObj }
			this.results.albumTab = { ...resetObj }
			this.results.artistTab = { ...resetObj }
			this.results.playlistTab = { ...resetObj }

			if (this.results.query == '') document.getElementById('search_all_tab').click()

			this.results.query = result.QUERY
			document.getElementById('search_tab_content').style.display = 'block'
			document.getElementById('main_search_tablink').click()
		},
		handleSearch(result) {
			const { next: nextResult, total, type, data } = result

			let currentTab = type + 'Tab'
			let next = 0

			if (nextResult) {
				next = parseInt(nextResult.match(/index=(\d*)/)[1])
			} else {
				next = total
			}

			if (this.results[currentTab].total != total) {
				this.results[currentTab].total = total
			}

			if (this.results[currentTab].next != next) {
				this.results[currentTab].next = next
				this.results[currentTab].data = this.results[currentTab].data.concat(data)
			}

			this.results[currentTab].loaded = true
		}
	},
	watch: {
		scrolledSearchType(newType) {
			if (!newType) return

			this.scrolledSearch(newType)
		}
	}
}
</script>

<style>
</style>
