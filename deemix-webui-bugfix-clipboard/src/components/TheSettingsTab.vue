<template>
	<div id="settings_tab" class="main_tabcontent fixed_footer">
		<h2 class="page_heading">{{ $t('settings.title') }}</h2>

		<div id="logged_in_info" ref="loggedInInfo">
			<img id="settings_picture" src="" alt="Profile Picture" ref="userpicture" class="circle" />
			<i18n path="settings.login.loggedIn" tag="p">
				<strong place="username" id="settings_username" ref="username"></strong>
			</i18n>
			<button id="settings_btn_logout" @click="logout">{{ $t('settings.login.logout') }}</button>
			<select v-if="accounts.length" id="family_account" v-model="accountNum" @change="changeAccount">
				<option v-for="(account, i) in accounts" :value="i.toString()">{{ account.BLOG_NAME }}</option>
			</select>
		</div>

		<div class="settings-group">
			<h3 class="settings-group__header settings-group__header--with-icon">
				<i class="material-icons">person</i>{{ $t('settings.login.title') }}
			</h3>
			<div class="inline-flex">
				<input autocomplete="off" type="password" id="login_input_arl" ref="loginInput" placeholder="ARL" />
				<button id="settings_btn_copyArl" class="only_icon" @click="copyARLtoClipboard">
					<i class="material-icons">assignment</i>
				</button>
			</div>
			<a href="https://codeberg.org/RemixDev/deemix/wiki/Getting-your-own-ARL" target="_blank">
				{{ $t('settings.login.arl.question') }}
			</a>
			<button id="settings_btn_updateArl" @click="login" style="width: 100%;">
				{{ $t('settings.login.arl.update') }}
			</button>
		</div>

		<div class="settings-group">
			<h3 class="settings-group__header settings-group__header--with-icon">
				<i class="material-icons">language</i>{{ $t('settings.languages') }}
			</h3>
			<div>
				<span
					v-for="locale in locales"
					:key="locale"
					class="locale-flag"
					:class="{ 'locale-flag--current': currentLocale === locale }"
					@click="changeLocale(locale)"
					v-html="flags[locale]"
				>
				</span>
			</div>
		</div>

		<div class="settings-group">
			<h3 class="settings-group__header settings-group__header--with-icon">
				<i class="material-icons">web</i>{{ $t('settings.appearance.title') }}
			</h3>
			<label class="with_checkbox">
				<input type="checkbox" v-model="changeSlimDownloads" />
				<span class="checkbox_text">{{ $t('settings.appearance.slimDownloadTab') }}</span>
			</label>
		</div>

		<div class="settings-group">
			<h3 class="settings-group__header settings-group__header--with-icon">
				<i class="material-icons">folder</i>{{ $t('settings.downloadPath.title') }}
			</h3>
			<div class="inline-flex">
				<input autocomplete="off" type="text" v-model="settings.downloadLocation" />
				<button id="select_downloads_folder" class="only_icon hide" @click="selectDownloadFolder">
					<i class="material-icons">folder</i>
				</button>
			</div>
		</div>

		<div class="settings-group">
			<h3 class="settings-group__header settings-group__header--with-icon">
				<i class="material-icons">font_download</i>{{ $t('settings.templates.title') }}
			</h3>

			<p>{{ $t('settings.templates.tracknameTemplate') }}</p>
			<input type="text" v-model="settings.tracknameTemplate" />

			<p>{{ $t('settings.templates.albumTracknameTemplate') }}</p>
			<input type="text" v-model="settings.albumTracknameTemplate" />

			<p>{{ $t('settings.templates.playlistTracknameTemplate') }}</p>
			<input type="text" v-model="settings.playlistTracknameTemplate" />
		</div>

		<div class="settings-group">
			<h3 class="settings-group__header settings-group__header--with-icon">
				<i class="material-icons">create_new_folder</i>{{ $t('settings.folders.title') }}
			</h3>
			<div class="settings-container">
				<div class="settings-container__third">
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.createPlaylistFolder" />
						<span class="checkbox_text">{{ $t('settings.folders.createPlaylistFolder') }}</span>
					</label>
					<div class="input_group" v-if="settings.createPlaylistFolder">
						<p class="input_group_text">{{ $t('settings.folders.playlistNameTemplate') }}</p>
						<input type="text" v-model="settings.playlistNameTemplate" />
					</div>
				</div>
				<div class="settings-container__third">
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.createArtistFolder" />
						<span class="checkbox_text">{{ $t('settings.folders.createArtistFolder') }}</span>
					</label>

					<div class="input_group" v-if="settings.createArtistFolder">
						<p class="input_group_text">{{ $t('settings.folders.artistNameTemplate') }}</p>
						<input type="text" v-model="settings.artistNameTemplate" />
					</div>
				</div>
				<div class="settings-container__third">
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.createAlbumFolder" />
						<span class="checkbox_text">{{ $t('settings.folders.createAlbumFolder') }}</span>
					</label>

					<div class="input_group" v-if="settings.createAlbumFolder">
						<p class="input_group_text">{{ $t('settings.folders.albumNameTemplate') }}</p>
						<input type="text" v-model="settings.albumNameTemplate" />
					</div>
				</div>
			</div>

			<label class="with_checkbox">
				<input type="checkbox" v-model="settings.createCDFolder" />
				<span class="checkbox_text">{{ $t('settings.folders.createCDFolder') }}</span>
			</label>

			<label class="with_checkbox">
				<input type="checkbox" v-model="settings.createStructurePlaylist" />
				<span class="checkbox_text">{{ $t('settings.folders.createStructurePlaylist') }}</span>
			</label>

			<label class="with_checkbox">
				<input type="checkbox" v-model="settings.createSingleFolder" />
				<span class="checkbox_text">{{ $t('settings.folders.createSingleFolder') }}</span>
			</label>
		</div>

		<div class="settings-group">
			<h3 class="settings-group__header settings-group__header--with-icon">
				<i class="material-icons">title</i>{{ $t('settings.trackTitles.title') }}
			</h3>

			<div class="settings-container">
				<div class="settings-container__third settings-container__third--only-checkbox">
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.padTracks" />
						<span class="checkbox_text">{{ $t('settings.trackTitles.padTracks') }}</span>
					</label>
				</div>
				<div class="settings-container__third">
					<div class="input_group">
						<p class="input_group_text">{{ $t('settings.trackTitles.paddingSize') }}</p>
						<input max="10" type="number" v-model="settings.paddingSize" />
					</div>
				</div>
				<div class="settings-container__third">
					<div class="input_group">
						<p class="input_group_text">{{ $t('settings.trackTitles.illegalCharacterReplacer') }}</p>
						<input type="text" v-model="settings.illegalCharacterReplacer" />
					</div>
				</div>
			</div>
		</div>

		<div class="settings-group">
			<h3 class="settings-group__header settings-group__header--with-icon">
				<i class="material-icons">get_app</i>{{ $t('settings.downloads.title') }}
			</h3>

			<div class="input_group">
				<p class="input_group_text">{{ $t('settings.downloads.queueConcurrency') }}</p>
				<input type="number" v-model.number="settings.queueConcurrency" />
			</div>

			<div class="input_group">
				<p class="input_group_text">{{ $t('settings.downloads.maxBitrate.title') }}</p>
				<select v-model="settings.maxBitrate">
					<option value="9">{{ $t('settings.downloads.maxBitrate.9') }}</option>
					<option value="3">{{ $t('settings.downloads.maxBitrate.3') }}</option>
					<option value="1">{{ $t('settings.downloads.maxBitrate.1') }}</option>
				</select>
			</div>

			<div class="input_group">
				<p class="input_group_text">{{ $t('settings.downloads.overwriteFile.title') }}</p>
				<select v-model="settings.overwriteFile">
					<option value="y">{{ $t('settings.downloads.overwriteFile.y') }}</option>
					<option value="n">{{ $t('settings.downloads.overwriteFile.n') }}</option>
					<option value="b">{{ $t('settings.downloads.overwriteFile.b') }}</option>
					<option value="t">{{ $t('settings.downloads.overwriteFile.t') }}</option>
				</select>
			</div>

			<div class="settings-container">
				<div class="settings-container__third settings-container__third--only-checkbox">
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.fallbackBitrate" />
						<span class="checkbox_text">{{ $t('settings.downloads.fallbackBitrate') }}</span>
					</label>

					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.fallbackSearch" />
						<span class="checkbox_text">{{ $t('settings.downloads.fallbackSearch') }}</span>
					</label>
				</div>
				<div class="settings-container__third settings-container__third--only-checkbox">
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.logErrors" />
						<span class="checkbox_text">{{ $t('settings.downloads.logErrors') }}</span>
					</label>

					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.logSearched" />
						<span class="checkbox_text">{{ $t('settings.downloads.logSearched') }}</span>
					</label>
				</div>
				<div class="settings-container__third settings-container__third--only-checkbox">
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.syncedLyrics" />
						<span class="checkbox_text">{{ $t('settings.downloads.syncedLyrics') }}</span>
					</label>

					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.createM3U8File" />
						<span class="checkbox_text">{{ $t('settings.downloads.createM3U8File') }}</span>
					</label>
				</div>
			</div>

			<div class="input_group" v-if="settings.createM3U8File">
				<p class="input_group_text">{{ $t('settings.downloads.playlistFilenameTemplate') }}</p>
				<input type="text" v-model="settings.playlistFilenameTemplate" />
			</div>

			<label class="with_checkbox">
				<input type="checkbox" v-model="settings.saveDownloadQueue" />
				<span class="checkbox_text">{{ $t('settings.downloads.saveDownloadQueue') }}</span>
			</label>
		</div>

		<div class="settings-group">
			<h3 class="settings-group__header settings-group__header--with-icon">
				<i class="material-icons">album</i>{{ $t('settings.covers.title') }}
			</h3>

			<label class="with_checkbox">
				<input type="checkbox" v-model="settings.saveArtwork" />
				<span class="checkbox_text">{{ $t('settings.covers.saveArtwork') }}</span>
			</label>

			<div class="input_group" v-if="settings.saveArtwork">
				<p class="input_group_text">{{ $t('settings.covers.coverImageTemplate') }}</p>
				<input type="text" v-model="settings.coverImageTemplate" />
			</div>

			<label class="with_checkbox">
				<input type="checkbox" v-model="settings.saveArtworkArtist" />
				<span class="checkbox_text">{{ $t('settings.covers.saveArtworkArtist') }}</span>
			</label>

			<div class="input_group" v-if="settings.saveArtworkArtist">
				<p class="input_group_text">{{ $t('settings.covers.artistImageTemplate') }}</p>
				<input type="text" v-model="settings.artistImageTemplate" />
			</div>

			<div class="input_group">
				<p class="input_group_text">{{ $t('settings.covers.localArtworkSize') }}</p>
				<input type="number" min="100" max="1800" step="100" v-model.number="settings.localArtworkSize" />
			</div>

			<div class="input_group">
				<p class="input_group_text">{{ $t('settings.covers.embeddedArtworkSize') }}</p>
				<input type="number" min="100" max="1800" step="100" v-model.number="settings.embeddedArtworkSize" />
			</div>

			<div class="input_group">
				<p class="input_group_text">{{ $t('settings.covers.localArtworkFormat.title') }}</p>
				<select v-model="settings.localArtworkFormat">
					<option value="jpg">{{ $t('settings.covers.localArtworkFormat.jpg') }}</option>
					<option value="png">{{ $t('settings.covers.localArtworkFormat.png') }}</option>
					<option value="jpg,png">{{ $t('settings.covers.localArtworkFormat.both') }}</option>
				</select>
			</div>

			<div class="input_group">
				<p class="input_group_text">{{ $t('settings.covers.jpegImageQuality') }}</p>
				<input type="number" min="1" max="100" v-model.number="settings.jpegImageQuality" />
			</div>
		</div>

		<div class="settings-group">
			<h3 class="settings-group__header settings-group__header--with-icon">
				<i class="material-icons" style="width: 1em; height: 1em;">bookmarks</i>{{ $t('settings.tags.head') }}
			</h3>

			<div class="settings-container">
				<div class="settings-container__half">
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.title" />
						<span class="checkbox_text">{{ $t('settings.tags.title') }}</span>
					</label>
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.artist" />
						<span class="checkbox_text">{{ $t('settings.tags.artist') }}</span>
					</label>
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.album" />
						<span class="checkbox_text">{{ $t('settings.tags.album') }}</span>
					</label>
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.cover" />
						<span class="checkbox_text">{{ $t('settings.tags.cover') }}</span>
					</label>
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.trackNumber" />
						<span class="checkbox_text">{{ $t('settings.tags.trackNumber') }}</span>
					</label>
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.trackTotal" />
						<span class="checkbox_text">{{ $t('settings.tags.trackTotal') }}</span>
					</label>
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.discNumber" />
						<span class="checkbox_text">{{ $t('settings.tags.discNumber') }}</span>
					</label>
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.discTotal" />
						<span class="checkbox_text">{{ $t('settings.tags.discTotal') }}</span>
					</label>
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.albumArtist" />
						<span class="checkbox_text">{{ $t('settings.tags.albumArtist') }}</span>
					</label>
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.genre" />
						<span class="checkbox_text">{{ $t('settings.tags.genre') }}</span>
					</label>
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.year" />
						<span class="checkbox_text">{{ $t('settings.tags.year') }}</span>
					</label>
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.date" />
						<span class="checkbox_text">{{ $t('settings.tags.date') }}</span>
					</label>
				</div>

				<div class="settings-container__half">
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.explicit" />
						<span class="checkbox_text">{{ $t('settings.tags.explicit') }}</span>
					</label>
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.isrc" />
						<span class="checkbox_text">{{ $t('settings.tags.isrc') }}</span>
					</label>
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.length" />
						<span class="checkbox_text">{{ $t('settings.tags.length') }}</span>
					</label>
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.barcode" />
						<span class="checkbox_text">{{ $t('settings.tags.barcode') }}</span>
					</label>
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.bpm" />
						<span class="checkbox_text">{{ $t('settings.tags.bpm') }}</span>
					</label>
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.replayGain" />
						<span class="checkbox_text">{{ $t('settings.tags.replayGain') }}</span>
					</label>
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.label" />
						<span class="checkbox_text">{{ $t('settings.tags.label') }}</span>
					</label>
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.lyrics" />
						<span class="checkbox_text">{{ $t('settings.tags.lyrics') }}</span>
					</label>
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.copyright" />
						<span class="checkbox_text">{{ $t('settings.tags.copyright') }}</span>
					</label>
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.composer" />
						<span class="checkbox_text">{{ $t('settings.tags.composer') }}</span>
					</label>
					<label class="with_checkbox">
						<input type="checkbox" v-model="settings.tags.involvedPeople" />
						<span class="checkbox_text">{{ $t('settings.tags.involvedPeople') }}</span>
					</label>
				</div>
			</div>
		</div>

		<div class="settings-group">
			<h3 class="settings-group__header settings-group__header--with-icon">
				<i class="material-icons">list</i>{{ $t('settings.other.title') }}
			</h3>

			<label class="with_checkbox">
				<input type="checkbox" v-model="settings.tags.savePlaylistAsCompilation" />
				<span class="checkbox_text">{{ $t('settings.other.savePlaylistAsCompilation') }}</span>
			</label>

			<label class="with_checkbox">
				<input type="checkbox" v-model="settings.tags.useNullSeparator" />
				<span class="checkbox_text">{{ $t('settings.other.useNullSeparator') }}</span>
			</label>

			<label class="with_checkbox">
				<input type="checkbox" v-model="settings.tags.saveID3v1" />
				<span class="checkbox_text">{{ $t('settings.other.saveID3v1') }}</span>
			</label>

			<div class="input_group">
				<p class="input_group_text">{{ $t('settings.other.multiArtistSeparator.title') }}</p>
				<select v-model="settings.tags.multiArtistSeparator">
					<option value="nothing">{{ $t('settings.other.multiArtistSeparator.nothing') }}</option>
					<option value="default">{{ $t('settings.other.multiArtistSeparator.default') }}</option>
					<option value="andFeat">{{ $t('settings.other.multiArtistSeparator.andFeat') }}</option>
					<option value=" & ">{{ $t('settings.other.multiArtistSeparator.using', [' & ']) }}</option>
					<option value=",">{{ $t('settings.other.multiArtistSeparator.using', [',']) }}</option>
					<option value=", ">{{ $t('settings.other.multiArtistSeparator.using', [', ']) }}</option>
					<option value="/">{{ $t('settings.other.multiArtistSeparator.using', ['/']) }}</option>
					<option value=" / ">{{ $t('settings.other.multiArtistSeparator.using', [' / ']) }}</option>
					<option value=";">{{ $t('settings.other.multiArtistSeparator.using', [';']) }}</option>
					<option value="; ">{{ $t('settings.other.multiArtistSeparator.using', ['; ']) }}</option>
				</select>
			</div>

			<label class="with_checkbox">
				<input type="checkbox" v-model="settings.tags.singleAlbumArtist" />
				<span class="checkbox_text">{{ $t('settings.other.singleAlbumArtist') }}</span>
			</label>

			<label class="with_checkbox">
				<input type="checkbox" v-model="settings.albumVariousArtists" />
				<span class="checkbox_text">{{ $t('settings.other.albumVariousArtists') }}</span>
			</label>

			<label class="with_checkbox">
				<input type="checkbox" v-model="settings.removeAlbumVersion" />
				<span class="checkbox_text">{{ $t('settings.other.removeAlbumVersion') }}</span>
			</label>

			<label class="with_checkbox">
				<input type="checkbox" v-model="settings.removeDuplicateArtists" />
				<span class="checkbox_text">{{ $t('settings.other.removeDuplicateArtists') }}</span>
			</label>

			<div class="input_group">
				<p class="input_group_text">{{ $t('settings.other.dateFormat.title') }}</p>
				<select v-model="settings.dateFormat">
					<option value="Y-M-D">{{
						`${$t('settings.other.dateFormat.year')}-${$t('settings.other.dateFormat.month')}-${$t(
							'settings.other.dateFormat.day'
						)}`
					}}</option>
					<option value="Y-D-M">{{
						`${$t('settings.other.dateFormat.year')}-${$t('settings.other.dateFormat.day')}-${$t(
							'settings.other.dateFormat.month'
						)}`
					}}</option>
					<option value="D-M-Y">{{
						`${$t('settings.other.dateFormat.day')}-${$t('settings.other.dateFormat.month')}-${$t(
							'settings.other.dateFormat.year'
						)}`
					}}</option>
					<option value="M-D-Y">{{
						`${$t('settings.other.dateFormat.month')}-${$t('settings.other.dateFormat.day')}-${$t(
							'settings.other.dateFormat.year'
						)}`
					}}</option>
					<option value="Y">{{ $t('settings.other.dateFormat.year') }}</option>
				</select>
			</div>

			<div class="input_group">
				<p class="input_group_text">{{ $t('settings.other.featuredToTitle.title') }}</p>
				<select v-model="settings.featuredToTitle">
					<option value="0">{{ $t('settings.other.featuredToTitle.0') }}</option>
					<option value="1">{{ $t('settings.other.featuredToTitle.1') }}</option>
					<option value="3">{{ $t('settings.other.featuredToTitle.3') }}</option>
					<option value="2">{{ $t('settings.other.featuredToTitle.2') }}</option>
				</select>
			</div>

			<div class="input_group">
				<p class="input_group_text">{{ $t('settings.other.titleCasing') }}</p>
				<select v-model="settings.titleCasing">
					<option value="nothing">{{ $t('settings.other.casing.nothing') }}</option>
					<option value="lower">{{ $t('settings.other.casing.lower') }}</option>
					<option value="upper">{{ $t('settings.other.casing.upper') }}</option>
					<option value="start">{{ $t('settings.other.casing.start') }}</option>
					<option value="sentence">{{ $t('settings.other.casing.sentence') }}</option>
				</select>
			</div>

			<div class="input_group">
				<p class="input_group_text">{{ $t('settings.other.artistCasing') }}</p>
				<select v-model="settings.artistCasing">
					<option value="nothing">{{ $t('settings.other.casing.nothing') }}</option>
					<option value="lower">{{ $t('settings.other.casing.lower') }}</option>
					<option value="upper">{{ $t('settings.other.casing.upper') }}</option>
					<option value="start">{{ $t('settings.other.casing.start') }}</option>
					<option value="sentence">{{ $t('settings.other.casing.sentence') }}</option>
				</select>
			</div>

			<div class="input_group">
				<p class="input_group_text">{{ $t('settings.other.previewVolume') }}</p>
				<input
					type="range"
					@change="updateMaxVolume"
					min="0"
					max="100"
					step="1"
					class="slider"
					v-model.number="previewVolume.preview_max_volume"
				/>
				<span>{{ previewVolume.preview_max_volume }}%</span>
			</div>

			<div class="input_group">
				<p class="input_group_text">{{ $t('settings.other.executeCommand.title') }}</p>
				<p class="secondary-text">{{ $t('settings.other.executeCommand.description') }}</p>
				<input type="text" v-model="settings.executeCommand" />
			</div>
		</div>

		<div class="settings-group">
			<h3 class="settings-group__header settings-group__header--with-icon">
				<svg id="spotify_icon" enable-background="new 0 0 24 24" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
					<path
						d="m12 24c6.624 0 12-5.376 12-12s-5.376-12-12-12-12 5.376-12 12 5.376 12 12 12zm4.872-6.344v.001c-.807 0-3.356-2.828-10.52-1.36-.189.049-.436.126-.576.126-.915 0-1.09-1.369-.106-1.578 3.963-.875 8.013-.798 11.467 1.268.824.526.474 1.543-.265 1.543zm1.303-3.173c-.113-.03-.08.069-.597-.203-3.025-1.79-7.533-2.512-11.545-1.423-.232.063-.358.126-.576.126-1.071 0-1.355-1.611-.188-1.94 4.716-1.325 9.775-.552 13.297 1.543.392.232.547.533.547.953-.005.522-.411.944-.938.944zm-13.627-7.485c4.523-1.324 11.368-.906 15.624 1.578 1.091.629.662 2.22-.498 2.22l-.001-.001c-.252 0-.407-.063-.625-.189-3.443-2.056-9.604-2.549-13.59-1.436-.175.048-.393.125-.625.125-.639 0-1.127-.499-1.127-1.142 0-.657.407-1.029.842-1.155z"
					/>
				</svg>
				{{ $t('settings.spotify.title') }}
			</h3>

			<div class="input_group">
				<p class="input_group_text">{{ $t('settings.spotify.clientID') }}</p>
				<input type="text" v-model="spotifyFeatures.clientId" />
			</div>

			<div class="input_group">
				<p class="input_group_text">{{ $t('settings.spotify.clientSecret') }}</p>
				<input type="password" v-model="spotifyFeatures.clientSecret" />
			</div>

			<div class="input_group">
				<p class="input_group_text">{{ $t('settings.spotify.username') }}</p>
				<input type="text" v-model="spotifyUser" />
			</div>
		</div>

		<footer>
			<button @click="resetSettings">{{ $t('settings.reset') }}</button>
			<button @click="saveSettings">{{ $t('settings.save') }}</button>
		</footer>
	</div>
</template>
<style lang="scss">
.locale-flag {
	width: 60px;
	display: inline-flex;
	justify-content: center;
	align-items: center;
	cursor: pointer;

	&:not(:last-child) {
		margin-right: 10px;
	}

	&.locale-flag--current {
		svg {
			filter: brightness(1);
		}
	}

	svg {
		width: 40px;
		height: 40px;
		filter: brightness(0.5);
	}
}
</style>

<script>
import { toast } from '@/utils/toasts'
import { socket } from '@/utils/socket'
import EventBus from '@/utils/EventBus'
import flags from '@/utils/flags'

export default {
	name: 'the-settings-tab',
	data: () => ({
		flags,
		currentLocale: 'en',
		locales: [],
		settings: { tags: {} },
		lastSettings: {},
		spotifyFeatures: {},
		lastCredentials: {},
		defaultSettings: {},
		lastUser: '',
		spotifyUser: '',
		slimDownloads: false,
		previewVolume: window.vol,
		accountNum: 0,
		accounts: []
	}),
	computed: {
		changeSlimDownloads: {
			get() {
				return this.slimDownloads
			},
			set(wantSlimDownloads) {
				this.slimDownloads = wantSlimDownloads
				document.getElementById('download_list').classList.toggle('slim', wantSlimDownloads)
				localStorage.setItem('slimDownloads', wantSlimDownloads)
			}
		}
	},
	mounted() {
		this.locales = this.$i18n.availableLocales

		EventBus.$on('settingsTab:revertSettings', this.revertSettings)
		EventBus.$on('settingsTab:revertCredentials', this.revertCredentials)

		this.$refs.loggedInInfo.classList.add('hide')

		let storedLocale = localStorage.getItem('locale')

		if (storedLocale) {
			this.$i18n.locale = storedLocale
			this.currentLocale = storedLocale
		}

		let storedArl = localStorage.getItem('arl')

		if (storedArl) {
			this.$refs.loginInput.value = storedArl.trim()
		}

		let storedAccountNum = localStorage.getItem('accountNum')

		if (storedAccountNum) {
			this.accountNum = storedAccountNum
		}

		let spotifyUser = localStorage.getItem('spotifyUser')

		if (spotifyUser) {
			this.lastUser = spotifyUser
			this.spotifyUser = spotifyUser
			socket.emit('update_userSpotifyPlaylists', spotifyUser)
		}

		this.changeSlimDownloads = 'true' === localStorage.getItem('slimDownloads')

		let volume = parseInt(localStorage.getItem('previewVolume'))
		if (isNaN(volume)) {
			volume = 80
			localStorage.setItem('previewVolume', volume)
		}
		window.vol.preview_max_volume = volume

		socket.on('init_settings', this.initSettings)
		socket.on('updateSettings', this.updateSettings)
		socket.on('accountChanged', this.accountChanged)
		socket.on('familyAccounts', this.initAccounts)
		socket.on('downloadFolderSelected', this.downloadFolderSelected)
	},
	methods: {
		revertSettings() {
			this.settings = { ...this.lastSettings }
		},
		revertCredentials() {
			this.spotifyCredentials = { ...this.lastCredentials }
			this.spotifyUser = (' ' + this.lastUser).slice(1)
		},
		copyARLtoClipboard() {
			let copyText = this.$refs.loginInput

			copyText.setAttribute('type', 'text')
			copyText.select()
			copyText.setSelectionRange(0, 99999)
			document.execCommand('copy')
			copyText.setAttribute('type', 'password')

			toast(this.$t('settings.toasts.ARLcopied'), 'assignment')
		},
		changeLocale(newLocale) {
			this.$i18n.locale = newLocale
			this.currentLocale = newLocale
			localStorage.setItem('locale', newLocale)
		},
		updateMaxVolume() {
			localStorage.setItem('previewVolume', this.previewVolume.preview_max_volume)
		},
		saveSettings() {
			this.lastSettings = { ...this.settings }
			this.lastCredentials = { ...this.spotifyFeatures }
			let changed = false
			if (this.lastUser != this.spotifyUser) {
				// force cloning without linking
				this.lastUser = (' ' + this.spotifyUser).slice(1)
				localStorage.setItem('spotifyUser', this.lastUser)
				changed = true
			}

			socket.emit('saveSettings', this.lastSettings, this.lastCredentials, changed ? this.lastUser : false)
		},
		selectDownloadFolder() {
			if (window.clientMode) socket.emit('selectDownloadFolder')
		},
		downloadFolderSelected(folder){
			console.log(folder)
			this.settings.downloadLocation = folder
		},
		loadSettings(settings, spotifyCredentials, defaults = null) {
			if (defaults) {
				this.defaultSettings = { ...defaults }
			}

			this.lastSettings = { ...settings }
			this.lastCredentials = { ...spotifyCredentials }
			this.settings = settings
			this.spotifyFeatures = spotifyCredentials
		},
		login() {
			let arl = this.$refs.loginInput.value.trim()
			if (arl != '' && arl != localStorage.getItem('arl')) {
				socket.emit('login', arl, true, this.accountNum)
			}
		},
		changeAccount() {
			socket.emit('changeAccount', this.accountNum)
		},
		accountChanged(user, accountNum) {
			this.$refs.username.innerText = user.name
			this.$refs.userpicture.src = `https://e-cdns-images.dzcdn.net/images/user/${user.picture}/125x125-000000-80-0-0.jpg`
			this.accountNum = accountNum
			localStorage.setItem('accountNum', this.accountNum)
		},
		initAccounts(accounts) {
			this.accounts = accounts
		},
		logout() {
			socket.emit('logout')
		},
		initSettings(settings, credentials, defaults) {
			this.loadSettings(settings, credentials, defaults)
			toast(this.$t('settings.toasts.init'), 'settings')
		},
		updateSettings(settings, credentials) {
			this.loadSettings(settings, credentials)
			toast(this.$t('settings.toasts.update'), 'settings')
		},
		resetSettings() {
			this.settings = { ...this.defaultSettings }
		}
	}
}
</script>
