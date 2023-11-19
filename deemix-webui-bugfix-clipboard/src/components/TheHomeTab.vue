<template>
	<div id="home_tab" class="main_tabcontent">
		<h2 class="page_heading">{{ $t('globals.welcome') }}</h2>
		<section id="home_not_logged_in" class="home_section" ref="notLogged">
			<p id="home_not_logged_text">{{ $t('home.needTologin') }}</p>
			<button type="button" name="button" @click="openSettings">{{ $t('home.openSettings') }}</button>
		</section>
		<section v-if="playlists.length" class="home_section">
			<h3 class="section_heading">{{ $t('home.sections.popularPlaylists') }}</h3>
			<div class="release_grid">
				<div
					v-for="release in playlists"
					:key="release.id"
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
		</section>
		<section v-if="albums.length" class="home_section">
			<h3 class="section_heading">{{ $t('home.sections.popularAlbums') }}</h3>
			<div class="release_grid">
				<div
					v-for="release in albums"
					:key="release.id"
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
					<p class="primary-text">{{ release.title }}</p>
					<p class="secondary-text">{{ `${$t('globals.by', [release.artist.name])}` }}</p>
				</div>
			</div>
		</section>
	</div>
</template>

<script>
import { socket } from '@/utils/socket'
import { showView } from '@js/tabs.js'
import Downloads from '@/utils/downloads'

export default {
	name: 'the-home-tab',
	data() {
		return {
			playlists: [],
			albums: []
		}
	},
	methods: {
		artistView: showView.bind(null, 'artist'),
		albumView: showView.bind(null, 'album'),
		playlistView: showView.bind(null, 'playlist'),
		openSettings() {
			document.getElementById('main_settings_tablink').click()
		},
		addToQueue(e) {
			Downloads.sendAddToQueue(e.currentTarget.dataset.link)
		},
		initHome(data) {
			const {
				playlists: { data: playlistData },
				albums: { data: albumData }
			} = data

			this.playlists = playlistData
			this.albums = albumData
		}
	},
	mounted() {
		if (localStorage.getItem('arl')) {
			this.$refs.notLogged.classList.add('hide')
		}

		socket.on('init_home', this.initHome)
	}
}
</script>

<style>
</style>
