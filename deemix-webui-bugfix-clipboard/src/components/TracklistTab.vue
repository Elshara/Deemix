<template>
	<div id="tracklist_tab" class="main_tabcontent fixed_footer image_header">
		<header
			:style="{
				'background-image':
					'linear-gradient(to bottom, transparent 0%, var(--main-background) 100%), url(\'' + image + '\')'
			}"
		>
			<h1 class="inline-flex">
				{{ title }} <i v-if="explicit" class="material-icons explicit_icon explicit_icon--right">explicit</i>
			</h1>
			<h2 class="inline-flex">
				<span v-if="metadata">{{ metadata }}</span>
				<span class="right" v-if="release_date">{{ release_date }}</span>
			</h2>
		</header>

		<table class="table table--tracklist">
			<thead>
				<tr>
					<th>
						<i class="material-icons">music_note</i>
					</th>
					<th>#</th>
					<th>{{ $tc('globals.listTabs.title', 1) }}</th>
					<th>{{ $tc('globals.listTabs.artist', 1) }}</th>
					<th v-if="type === 'playlist'">{{ $tc('globals.listTabs.album', 1) }}</th>
					<th>
						<i class="material-icons">timer</i>
					</th>
					<th class="table__icon table__cell--center clickable">
						<input @click="toggleAll" class="selectAll" type="checkbox" />
					</th>
				</tr>
			</thead>
			<tbody>
				<template v-if="type !== 'spotifyPlaylist'">
					<template v-for="(track, index) in body">
						<tr v-if="track.type == 'track'" @click="selectRow(index, track)">
							<td class="table__cell--x-small table__cell--center">
								<div class="table__cell-content table__cell-content--vertical-center">
									<i
										class="material-icons"
										:class="{ preview_playlist_controls: track.preview, disabled: !track.preview }"
										v-on="{ click: track.preview ? playPausePreview : false }"
										:data-preview="track.preview"
										:title="$t('globals.play_hint')"
									>
										play_arrow
									</i>
								</div>
							</td>
							<td class="table__cell--small table__cell--center track_position">
								{{ type === 'album' ? track.track_position : body.indexOf(track) + 1 }}
							</td>
							<td class="table__cell--large table__cell--with-icon">
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
								class="table__cell--medium table__cell--center clickable"
								@click="artistView"
								:data-id="track.artist.id"
							>
								{{ track.artist.name }}
							</td>
							<td
								v-if="type == 'playlist'"
								class="table__cell--medium table__cell--center clickable"
								@click="albumView"
								:data-id="track.album.id"
							>
								{{ track.album.title }}
							</td>
							<td
								class="table__cell--center"
								:class="{ 'table__cell--small': type === 'album', 'table__cell--x-small': type === 'playlist' }"
							>
								{{ convertDuration(track.duration) }}
							</td>
							<td class="table__icon table__cell--center">
								<input class="clickable" type="checkbox" v-model="track.selected" />
							</td>
						</tr>
						<tr v-else-if="track.type == 'disc_separator'" class="table__row-no-highlight" style="opacity: 0.54;">
							<td>
								<div class="table__cell-content table__cell-content--vertical-center" style="opacity: 0.54;">
									<i class="material-icons">album</i>
								</div>
							</td>
							<td class="table__cell--center">
								{{ track.number }}
							</td>
							<td colspan="4"></td>
						</tr>
					</template>
				</template>
				<template v-else>
					<tr v-for="(track, i) in body">
						<td>
							<i
								v-if="track.preview_url"
								@click="playPausePreview"
								:class="'material-icons' + (track.preview_url ? ' preview_playlist_controls' : '')"
								:data-preview="track.preview_url"
								:title="$t('globals.play_hint')"
							>
								play_arrow
							</i>
							<i v-else class="material-icons disabled">play_arrow</i>
						</td>
						<td>{{ i + 1 }}</td>
						<td class="inline-flex">
							<i v-if="track.explicit" class="material-icons explicit_icon">explicit</i>
							{{ track.name }}
						</td>
						<td>{{ track.artists[0].name }}</td>
						<td>{{ track.album.name }}</td>
						<td>{{ convertDuration(Math.floor(track.duration_ms / 1000)) }}</td>
						<td><input class="clickable" type="checkbox" v-model="track.selected" /></td>
					</tr>
				</template>
			</tbody>
		</table>
		<span v-if="label" style="opacity: 0.4; margin-top: 8px; display: inline-block; font-size: 13px;">{{ label }}</span>
		<footer>
			<button @click.stop="addToQueue" :data-link="link">
				{{ `${$t('globals.download', [$tc(`globals.listTabs.${type}`, 1)])}` }}
			</button>
			<button class="with_icon" @click.stop="addToQueue" :data-link="selectedLinks()">
				{{ $t('tracklist.downloadSelection') }}<i class="material-icons">file_download</i>
			</button>
			<button class="back-button">{{ $t('globals.back') }}</button>
		</footer>
	</div>
</template>

<script>
import { isEmpty } from 'lodash-es'
import { socket } from '@/utils/socket'
import { showView } from '@js/tabs.js'
import Downloads from '@/utils/downloads'
import Utils from '@/utils/utils'
import EventBus from '@/utils/EventBus'

export default {
	name: 'tracklist-tab',
	data: () => ({
		title: '',
		metadata: '',
		release_date: '',
		label: '',
		explicit: false,
		image: '',
		type: 'empty',
		link: '',
		body: []
	}),
	methods: {
		artistView: showView.bind(null, 'artist'),
		albumView: showView.bind(null, 'album'),
		playPausePreview(e) {
			EventBus.$emit('trackPreview:playPausePreview', e)
		},
		reset() {
			this.title = 'Loading...'
			this.image = ''
			this.metadata = ''
			this.label = ''
			this.release_date = ''
			this.explicit = false
			this.type = 'empty'
			this.body = []
		},
		addToQueue(e) {
			Downloads.sendAddToQueue(e.currentTarget.dataset.link)
		},
		toggleAll(e) {
			this.body.forEach(item => {
				if (item.type == 'track') {
					item.selected = e.currentTarget.checked
				}
			})
		},
		selectedLinks() {
			var selected = []
			if (this.body) {
				this.body.forEach(item => {
					if (item.type == 'track' && item.selected)
						selected.push(this.type == 'spotifyPlaylist' ? item.uri : item.link)
				})
			}
			return selected.join(';')
		},
		convertDuration: Utils.convertDuration,
		showAlbum(data) {
			const {
				id: albumID,
				title: albumTitle,
				explicit_lyrics,
				label: albumLabel,
				artist: { name: artistName },
				tracks: albumTracks,
				tracks: { length: numberOfTracks },
				release_date,
				cover_xl
			} = data

			this.type = 'album'
			this.link = `https://www.deezer.com/album/${albumID}`
			this.title = albumTitle
			this.explicit = explicit_lyrics
			this.label = albumLabel
			this.metadata = `${artistName} • ${this.$tc('globals.listTabs.trackN', numberOfTracks)}`
			this.release_date = release_date.substring(0, 10)
			this.image = cover_xl

			if (isEmpty(albumTracks)) {
				this.body = null
			} else {
				this.body = albumTracks
			}
		},
		showPlaylist(data) {
			const {
				id: playlistID,
				title: playlistTitle,
				picture_xl: playlistCover,
				creation_date,
				creator: { name: creatorName },
				tracks: playlistTracks,
				tracks: { length: numberOfTracks }
			} = data

			this.type = 'playlist'
			this.link = `https://www.deezer.com/playlist/${playlistID}`
			this.title = playlistTitle
			this.image = playlistCover
			this.release_date = creation_date.substring(0, 10)
			this.metadata = `${this.$t('globals.by', [creatorName])} • ${this.$tc('globals.listTabs.trackN', numberOfTracks)}`

			if (isEmpty(playlistTracks)) {
				this.body = null
			} else {
				this.body = playlistTracks
			}
		},
		showSpotifyPlaylist(data) {
			const {
				uri: playlistURI,
				name: playlistName,
				images,
				images: { length: numberOfImages },
				owner: { display_name: ownerName },
				tracks: playlistTracks,
				tracks: { length: numberOfTracks }
			} = data

			this.type = 'spotifyPlaylist'
			this.link = playlistURI
			this.title = playlistName
			this.image = numberOfImages
				? images[0].url
				: 'https://e-cdns-images.dzcdn.net/images/cover/d41d8cd98f00b204e9800998ecf8427e/1000x1000-000000-80-0-0.jpg'
			this.release_date = ''
			this.metadata = `${this.$t('globals.by', [ownerName])} • ${this.$tc('globals.listTabs.trackN', numberOfTracks)}`

			if (isEmpty(playlistTracks)) {
				this.body = null
			} else {
				this.body = playlistTracks
			}
		},
		selectRow(index, track) {
			track.selected = !track.selected
		}
	},
	mounted() {
		EventBus.$on('tracklistTab:reset', this.reset)
		EventBus.$on('tracklistTab:selectRow', this.selectRow)

		socket.on('show_album', this.showAlbum)
		socket.on('show_playlist', this.showPlaylist)
		socket.on('show_spotifyplaylist', this.showSpotifyPlaylist)
	}
}
</script>

<style>
</style>
