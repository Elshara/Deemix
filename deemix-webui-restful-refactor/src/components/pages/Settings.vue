<template>
	<div class="fixed-footer">
		<h1 class="mb-8 text-5xl">{{ $t('settings.title') }}</h1>

		<div v-if="isLoggedIn" id="logged_in_info" ref="loggedInInfo">
			<img
				id="settings_picture"
				ref="userpicture"
				:src="pictureHref"
				alt="Profile Picture"
				class="w-32 h-32 rounded-full"
			/>

			<i18n path="settings.login.loggedIn" tag="p">
				<template #username>
					<strong id="settings_username" ref="username">{{ user.name || 'not logged' }}</strong>
				</template>
			</i18n>

			<button class="btn btn-primary" @click="logout">
				{{ $t('settings.login.logout') }}
			</button>

			<select v-if="accounts.length > 1" id="family_account" v-model="accountNum" @change="changeAccount">
				<option v-for="(account, i) in accounts" :key="account" :value="i.toString()">
					{{ account.name }}
				</option>
			</select>
		</div>

		<div class="settings-group">
			<h3 class="settings-group__header"><i class="material-icons">person</i>{{ $t('settings.login.title') }}</h3>

			<div class="my-5 space-y-5">
				<div class="flex items-center">
					<input
						id="login_input_arl"
						ref="loginInput"
						:value="arl"
						autocomplete="off"
						placeholder="ARL"
						type="password"
					/>
					<button class="ml-2 btn btn-primary btn-only-icon" @click="copyARLtoClipboard">
						<i class="material-icons">assignment</i>
					</button>
				</div>

				<RouterLink :to="{ name: 'ARL' }" class="block">
					{{ $t('settings.login.arl.question') }}
				</RouterLink>

				<a v-if="clientMode" class="block" href="#" @click="appLogin">
					{{ $t('settings.login.login') }}
				</a>

				<button class="btn btn-primary" style="width: 100%" @click="login">
					{{ $t('settings.login.arl.update') }}
				</button>
			</div>
		</div>

		<div class="settings-group">
			<h3 class="settings-group__header"><i class="material-icons">language</i>{{ $t('settings.languages') }}</h3>

			<ul class="my-5">
				<li
					v-for="locale in locales"
					:key="locale"
					:class="{ 'locale-flag--current': currentLocale === locale }"
					:title="locale"
					class="inline-flex items-center locale-flag"
					@click="changeLocale(locale)"
					v-html="flags[locale]"
				/>
			</ul>
		</div>

		<BaseAccordion class="settings-group">
			<template #title>
				<h3 class="settings-group__header">
					<i class="material-icons">web</i>
					{{ $t('settings.appearance.title') }}
				</h3>
			</template>

			<label class="with-checkbox">
				<input v-model="modelSlimDownloads" type="checkbox" />
				<span class="checkbox-text">{{ $t('settings.appearance.slimDownloadTab') }}</span>
			</label>
			<label class="mb-4 with-checkbox">
				<input v-model="modelSlimSidebar" type="checkbox" />
				<span class="checkbox-text">{{ $t('settings.appearance.slimSidebar') }}</span>
			</label>
		</BaseAccordion>

		<BaseAccordion class="settings-group">
			<template #title>
				<h3 class="settings-group__header">
					<i class="material-icons">folder</i>{{ $t('settings.downloadPath.title') }}
				</h3>
			</template>

			<div class="flex items-center">
				<input v-model="settings.downloadLocation" autocomplete="off" type="text" />
				<button v-if="clientMode" class="ml-2 btn btn-primary btn-only-icon" @click="selectDownloadFolder">
					<i class="material-icons">folder</i>
				</button>
			</div>
		</BaseAccordion>

		<BaseAccordion class="settings-group">
			<template #title>
				<h3 class="settings-group__header">
					<i class="material-icons">font_download</i>{{ $t('settings.templates.title') }}
				</h3>
			</template>

			<p>{{ $t('settings.templates.tracknameTemplate') }}</p>
			<input v-model="settings.tracknameTemplate" type="text" />

			<TemplateVariablesList :template-variables="trackTemplateVariables" @variable-click="onTemplateVariableClick">
				<template #title>
					{{ $t('settings.templates.tracknameAvailableVariables') }}
				</template>
			</TemplateVariablesList>

			<p>{{ $t('settings.templates.albumTracknameTemplate') }}</p>
			<input v-model="settings.albumTracknameTemplate" type="text" />
			<TemplateVariablesList :template-variables="trackTemplateVariables" @variable-click="onTemplateVariableClick">
				<template #title>
					{{ $t('settings.templates.albumTracknameAvailableVariables') }}
				</template>
			</TemplateVariablesList>

			<p>{{ $t('settings.templates.playlistTracknameTemplate') }}</p>
			<input v-model="settings.playlistTracknameTemplate" type="text" />
			<TemplateVariablesList :template-variables="trackTemplateVariables" @variable-click="onTemplateVariableClick">
				<template #title>
					{{ $t('settings.templates.playlistTracknameAvailableVariables') }}
				</template>
			</TemplateVariablesList>
		</BaseAccordion>

		<BaseAccordion class="settings-group">
			<template #title>
				<h3 class="settings-group__header">
					<i class="material-icons">create_new_folder</i>{{ $t('settings.folders.title') }}
				</h3>
			</template>

			<div class="space-x-5 settings-container">
				<div class="settings-container__third">
					<label class="with-checkbox">
						<input v-model="settings.createPlaylistFolder" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.folders.createPlaylistFolder') }}</span>
					</label>
					<div v-if="settings.createPlaylistFolder" class="input-group">
						<p class="input-group-text">{{ $t('settings.folders.playlistNameTemplate') }}</p>
						<input v-model="settings.playlistNameTemplate" type="text" />
					</div>
				</div>
				<div class="settings-container__third">
					<label class="with-checkbox">
						<input v-model="settings.createArtistFolder" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.folders.createArtistFolder') }}</span>
					</label>

					<div v-if="settings.createArtistFolder" class="input-group">
						<p class="input-group-text">{{ $t('settings.folders.artistNameTemplate') }}</p>
						<input v-model="settings.artistNameTemplate" type="text" />
					</div>
				</div>
				<div class="settings-container__third">
					<label class="with-checkbox">
						<input v-model="settings.createAlbumFolder" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.folders.createAlbumFolder') }}</span>
					</label>

					<div v-if="settings.createAlbumFolder" class="input-group">
						<p class="input-group-text">{{ $t('settings.folders.albumNameTemplate') }}</p>
						<input v-model="settings.albumNameTemplate" type="text" />
					</div>
				</div>
			</div>

			<label class="with-checkbox">
				<input v-model="settings.createCDFolder" type="checkbox" />
				<span class="checkbox-text">{{ $t('settings.folders.createCDFolder') }}</span>
			</label>

			<label class="with-checkbox">
				<input v-model="settings.createStructurePlaylist" type="checkbox" />
				<span class="checkbox-text">{{ $t('settings.folders.createStructurePlaylist') }}</span>
			</label>

			<label class="with-checkbox">
				<input v-model="settings.createSingleFolder" type="checkbox" />
				<span class="checkbox-text">{{ $t('settings.folders.createSingleFolder') }}</span>
			</label>
		</BaseAccordion>

		<BaseAccordion class="settings-group">
			<template #title>
				<h3 class="settings-group__header">
					<i class="material-icons">title</i>{{ $t('settings.trackTitles.title') }}
				</h3>
			</template>

			<div class="space-x-5 settings-container">
				<div class="settings-container__third settings-container__third--only-checkbox">
					<label class="with-checkbox">
						<input v-model="settings.padTracks" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.trackTitles.padTracks') }}</span>
					</label>
				</div>
				<div class="settings-container__third">
					<div class="input-group">
						<p class="input-group-text">{{ $t('settings.trackTitles.paddingSize') }}</p>
						<input v-model="settings.paddingSize" max="10" type="number" />
					</div>
				</div>
				<div class="settings-container__third">
					<div class="input-group">
						<p class="input-group-text">{{ $t('settings.trackTitles.illegalCharacterReplacer') }}</p>
						<input v-model="settings.illegalCharacterReplacer" type="text" />
					</div>
				</div>
			</div>
		</BaseAccordion>

		<BaseAccordion class="settings-group">
			<template #title>
				<h3 class="settings-group__header"><i class="material-icons">album</i>{{ $t('settings.covers.title') }}</h3>
			</template>

			<label class="with-checkbox">
				<input v-model="settings.saveArtwork" type="checkbox" />
				<span class="checkbox-text">{{ $t('settings.covers.saveArtwork') }}</span>
			</label>

			<div v-if="settings.saveArtwork" class="input-group">
				<p class="input-group-text">{{ $t('settings.covers.coverImageTemplate') }}</p>
				<input v-model="settings.coverImageTemplate" type="text" />
			</div>

			<label class="with-checkbox">
				<input v-model="settings.saveArtworkArtist" type="checkbox" />
				<span class="checkbox-text">{{ $t('settings.covers.saveArtworkArtist') }}</span>
			</label>

			<div v-if="settings.saveArtworkArtist" class="input-group">
				<p class="input-group-text">{{ $t('settings.covers.artistImageTemplate') }}</p>
				<input v-model="settings.artistImageTemplate" type="text" />
			</div>

			<div class="input-group">
				<p class="input-group-text">{{ $t('settings.covers.localArtworkSize') }}</p>
				<input v-model.number="settings.localArtworkSize" max="10000" min="100" step="100" type="number" />
				<p v-if="settings.localArtworkSize > 1200" class="input-group-text" style="opacity: 0.75; color: #ffcc22">
					⚠️ {{ $t('settings.covers.imageSizeWarning') }}
				</p>
			</div>

			<div class="input-group">
				<p class="input-group-text">{{ $t('settings.covers.embeddedArtworkSize') }}</p>
				<input v-model.number="settings.embeddedArtworkSize" max="10000" min="100" step="100" type="number" />
				<p v-if="settings.embeddedArtworkSize > 1200" class="input-group-text" style="opacity: 0.75; color: #ffcc22">
					⚠️ {{ $t('settings.covers.imageSizeWarning') }}
				</p>
			</div>

			<div class="input-group">
				<p class="input-group-text">{{ $t('settings.covers.localArtworkFormat.title') }}</p>
				<select v-model="settings.localArtworkFormat">
					<option value="jpg">{{ $t('settings.covers.localArtworkFormat.jpg') }}</option>
					<option value="png">{{ $t('settings.covers.localArtworkFormat.png') }}</option>
					<option value="jpg,png">{{ $t('settings.covers.localArtworkFormat.both') }}</option>
				</select>
			</div>

			<label class="with-checkbox">
				<input v-model="settings.embeddedArtworkPNG" type="checkbox" />
				<span class="checkbox-text">{{ $t('settings.covers.embeddedArtworkPNG') }}</span>
			</label>
			<p v-if="settings.embeddedArtworkPNG" style="opacity: 0.75; color: #ffcc22">
				⚠️ {{ $t('settings.covers.embeddedPNGWarning') }}
			</p>

			<label class="with-checkbox">
				<input v-model="settings.tags.coverDescriptionUTF8" type="checkbox" />
				<span class="checkbox-text">{{ $t('settings.covers.coverDescriptionUTF8') }}</span>
			</label>

			<div class="input-group">
				<p class="input-group-text">{{ $t('settings.covers.jpegImageQuality') }}</p>
				<input v-model.number="settings.jpegImageQuality" max="100" min="1" type="number" />
			</div>
		</BaseAccordion>

		<BaseAccordion class="settings-group">
			<template #title>
				<h3 class="settings-group__header">
					<i class="material-icons" style="width: 1em; height: 1em">bookmarks</i>{{ $t('settings.tags.head') }}
				</h3>
			</template>

			<div class="space-x-5 settings-container">
				<div class="settings-container__half">
					<label class="with-checkbox">
						<input v-model="settings.tags.title" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.title') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.artist" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.artist') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.album" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.album') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.cover" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.cover') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.trackNumber" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.trackNumber') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.trackTotal" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.trackTotal') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.discNumber" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.discNumber') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.discTotal" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.discTotal') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.albumArtist" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.albumArtist') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.genre" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.genre') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.year" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.year') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.date" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.date') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.explicit" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.explicit') }}</span>
					</label>
				</div>

				<div class="settings-container__half">
					<label class="with-checkbox">
						<input v-model="settings.tags.isrc" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.isrc') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.length" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.length') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.barcode" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.barcode') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.bpm" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.bpm') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.replayGain" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.replayGain') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.label" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.label') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.lyrics" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.lyrics') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.syncedLyrics" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.syncedLyrics') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.copyright" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.copyright') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.composer" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.composer') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.involvedPeople" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.involvedPeople') }}</span>
					</label>
					<label class="with-checkbox">
						<input v-model="settings.tags.source" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.tags.source') }}</span>
					</label>
				</div>
			</div>
		</BaseAccordion>

		<BaseAccordion class="settings-group">
			<template #title>
				<h3 class="settings-group__header">
					<i class="material-icons">get_app</i>{{ $t('settings.downloads.title') }}
				</h3>
			</template>

			<div class="input-group">
				<p class="input-group-text">{{ $t('settings.downloads.queueConcurrency') }}</p>
				<input v-model.number="settings.queueConcurrency" min="1" type="number" />
			</div>

			<div class="input-group">
				<p class="input-group-text">{{ $t('settings.downloads.maxBitrate.title') }}</p>
				<select v-model="settings.maxBitrate">
					<option value="9">{{ $t('settings.downloads.maxBitrate.9') }}</option>
					<option value="3">{{ $t('settings.downloads.maxBitrate.3') }}</option>
					<option value="1">{{ $t('settings.downloads.maxBitrate.1') }}</option>
				</select>
			</div>

			<div class="input-group">
				<p class="input-group-text">{{ $t('settings.downloads.overwriteFile.title') }}</p>
				<select v-model="settings.overwriteFile">
					<option value="y">{{ $t('settings.downloads.overwriteFile.y') }}</option>
					<option value="n">{{ $t('settings.downloads.overwriteFile.n') }}</option>
					<option value="e">{{ $t('settings.downloads.overwriteFile.e') }}</option>
					<option value="b">{{ $t('settings.downloads.overwriteFile.b') }}</option>
					<option value="t">{{ $t('settings.downloads.overwriteFile.t') }}</option>
				</select>
			</div>

			<div class="space-x-5 settings-container">
				<div class="settings-container__third settings-container__third--only-checkbox">
					<label class="with-checkbox">
						<input v-model="settings.fallbackBitrate" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.downloads.fallbackBitrate') }}</span>
					</label>

					<label class="with-checkbox">
						<input v-model="settings.fallbackSearch" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.downloads.fallbackSearch') }}</span>
					</label>
				</div>
				<div class="settings-container__third settings-container__third--only-checkbox">
					<label class="with-checkbox">
						<input v-model="settings.logErrors" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.downloads.logErrors') }}</span>
					</label>

					<label class="with-checkbox">
						<input v-model="settings.logSearched" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.downloads.logSearched') }}</span>
					</label>
				</div>
				<div class="settings-container__third settings-container__third--only-checkbox">
					<label class="with-checkbox">
						<input v-model="settings.syncedLyrics" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.downloads.syncedLyrics') }}</span>
					</label>

					<label class="with-checkbox">
						<input v-model="settings.createM3U8File" type="checkbox" />
						<span class="checkbox-text">{{ $t('settings.downloads.createM3U8File') }}</span>
					</label>
				</div>
			</div>

			<div v-if="settings.createM3U8File" class="input-group">
				<p class="input-group-text">{{ $t('settings.downloads.playlistFilenameTemplate') }}</p>
				<input v-model="settings.playlistFilenameTemplate" type="text" />
			</div>

			<label class="with-checkbox">
				<input v-model="settings.saveDownloadQueue" type="checkbox" />
				<span class="checkbox-text">{{ $t('settings.downloads.saveDownloadQueue') }}</span>
			</label>
		</BaseAccordion>

		<BaseAccordion class="settings-group">
			<template #title>
				<h3 class="settings-group__header"><i class="material-icons">list</i>{{ $t('settings.other.title') }}</h3>
			</template>

			<label class="with-checkbox">
				<input v-model="settings.tags.savePlaylistAsCompilation" type="checkbox" />
				<span class="checkbox-text">{{ $t('settings.other.savePlaylistAsCompilation') }}</span>
			</label>

			<label class="with-checkbox">
				<input v-model="settings.tags.useNullSeparator" type="checkbox" />
				<span class="checkbox-text">{{ $t('settings.other.useNullSeparator') }}</span>
			</label>

			<label class="with-checkbox">
				<input v-model="settings.tags.saveID3v1" type="checkbox" />
				<span class="checkbox-text">{{ $t('settings.other.saveID3v1') }}</span>
			</label>

			<div class="input-group">
				<p class="input-group-text">{{ $t('settings.other.multiArtistSeparator.title') }}</p>
				<select v-model="settings.tags.multiArtistSeparator">
					<option value="nothing">{{ $t('settings.other.multiArtistSeparator.nothing') }}</option>
					<option value="default">{{ $t('settings.other.multiArtistSeparator.default') }}</option>
					<option value="andFeat">{{ $t('settings.other.multiArtistSeparator.andFeat') }}</option>
					<option value=" & ">{{ $t('settings.other.multiArtistSeparator.using', { separator: ' & ' }) }}</option>
					<option value=",">{{ $t('settings.other.multiArtistSeparator.using', { separator: ',' }) }}</option>
					<option value=", ">{{ $t('settings.other.multiArtistSeparator.using', { separator: ', ' }) }}</option>
					<option value="/">{{ $t('settings.other.multiArtistSeparator.using', { separator: '/' }) }}</option>
					<option value=" / ">{{ $t('settings.other.multiArtistSeparator.using', { separator: ' / ' }) }}</option>
					<option value=";">{{ $t('settings.other.multiArtistSeparator.using', { separator: ';' }) }}</option>
					<option value="; ">{{ $t('settings.other.multiArtistSeparator.using', { separator: '; ' }) }}</option>
				</select>
			</div>

			<label class="with-checkbox">
				<input v-model="settings.tags.singleAlbumArtist" type="checkbox" />
				<span class="checkbox-text">{{ $t('settings.other.singleAlbumArtist') }}</span>
			</label>

			<label class="with-checkbox">
				<input v-model="settings.albumVariousArtists" type="checkbox" />
				<span class="checkbox-text">{{ $t('settings.other.albumVariousArtists') }}</span>
			</label>

			<label class="with-checkbox">
				<input v-model="settings.removeAlbumVersion" type="checkbox" />
				<span class="checkbox-text">{{ $t('settings.other.removeAlbumVersion') }}</span>
			</label>

			<label class="with-checkbox">
				<input v-model="settings.removeDuplicateArtists" type="checkbox" />
				<span class="checkbox-text">{{ $t('settings.other.removeDuplicateArtists') }}</span>
			</label>

			<div class="input-group">
				<p class="input-group-text">{{ $t('settings.other.dateFormat.title') }}</p>
				<select v-model="settings.dateFormat">
					<option value="Y-M-D">
						{{
							`${$t('settings.other.dateFormat.year')}-${$t('settings.other.dateFormat.month')}-${$t(
								'settings.other.dateFormat.day'
							)}`
						}}
					</option>
					<option value="Y-D-M">
						{{
							`${$t('settings.other.dateFormat.year')}-${$t('settings.other.dateFormat.day')}-${$t(
								'settings.other.dateFormat.month'
							)}`
						}}
					</option>
					<option value="D-M-Y">
						{{
							`${$t('settings.other.dateFormat.day')}-${$t('settings.other.dateFormat.month')}-${$t(
								'settings.other.dateFormat.year'
							)}`
						}}
					</option>
					<option value="M-D-Y">
						{{
							`${$t('settings.other.dateFormat.month')}-${$t('settings.other.dateFormat.day')}-${$t(
								'settings.other.dateFormat.year'
							)}`
						}}
					</option>
					<option value="Y">{{ $t('settings.other.dateFormat.year') }}</option>
				</select>
			</div>

			<div class="input-group">
				<p class="input-group-text">{{ $t('settings.other.featuredToTitle.title') }}</p>
				<select v-model="settings.featuredToTitle">
					<option value="0">{{ $t('settings.other.featuredToTitle.0') }}</option>
					<option value="1">{{ $t('settings.other.featuredToTitle.1') }}</option>
					<option value="3">{{ $t('settings.other.featuredToTitle.3') }}</option>
					<option value="2">{{ $t('settings.other.featuredToTitle.2') }}</option>
				</select>
			</div>

			<div class="input-group">
				<p class="input-group-text">{{ $t('settings.other.titleCasing') }}</p>
				<select v-model="settings.titleCasing">
					<option value="nothing">{{ $t('settings.other.casing.nothing') }}</option>
					<option value="lower">{{ $t('settings.other.casing.lower') }}</option>
					<option value="upper">{{ $t('settings.other.casing.upper') }}</option>
					<option value="start">{{ $t('settings.other.casing.start') }}</option>
					<option value="sentence">{{ $t('settings.other.casing.sentence') }}</option>
				</select>
			</div>

			<div class="input-group">
				<p class="input-group-text">{{ $t('settings.other.artistCasing') }}</p>
				<select v-model="settings.artistCasing">
					<option value="nothing">{{ $t('settings.other.casing.nothing') }}</option>
					<option value="lower">{{ $t('settings.other.casing.lower') }}</option>
					<option value="upper">{{ $t('settings.other.casing.upper') }}</option>
					<option value="start">{{ $t('settings.other.casing.start') }}</option>
					<option value="sentence">{{ $t('settings.other.casing.sentence') }}</option>
				</select>
			</div>

			<div class="input-group">
				<p class="input-group-text">{{ $t('settings.other.previewVolume') }}</p>
				<input v-model.number="modelVolume" class="slider" max="100" min="0" step="1" type="range" />
				<span>{{ previewVolume }}%</span>
			</div>

			<div class="input-group">
				<p class="input-group-text">{{ $t('settings.other.executeCommand.title') }}</p>
				<p class="secondary-text">{{ $t('settings.other.executeCommand.description') }}</p>
				<input v-model="settings.executeCommand" type="text" />
			</div>
		</BaseAccordion>

		<BaseAccordion class="settings-group">
			<template #title>
				<h3 class="settings-group__header">
					<svg
						class="mr-4 w-6 h-6"
						enable-background="new 0 0 24 24"
						style="fill: #1db954"
						viewBox="0 0 24 24"
						xmlns="http://www.w3.org/2000/svg"
					>
						<path
							d="m12 24c6.624 0 12-5.376 12-12s-5.376-12-12-12-12 5.376-12 12 5.376 12 12 12zm4.872-6.344v.001c-.807 0-3.356-2.828-10.52-1.36-.189.049-.436.126-.576.126-.915 0-1.09-1.369-.106-1.578 3.963-.875 8.013-.798 11.467 1.268.824.526.474 1.543-.265 1.543zm1.303-3.173c-.113-.03-.08.069-.597-.203-3.025-1.79-7.533-2.512-11.545-1.423-.232.063-.358.126-.576.126-1.071 0-1.355-1.611-.188-1.94 4.716-1.325 9.775-.552 13.297 1.543.392.232.547.533.547.953-.005.522-.411.944-.938.944zm-13.627-7.485c4.523-1.324 11.368-.906 15.624 1.578 1.091.629.662 2.22-.498 2.22l-.001-.001c-.252 0-.407-.063-.625-.189-3.443-2.056-9.604-2.549-13.59-1.436-.175.048-.393.125-.625.125-.639 0-1.127-.499-1.127-1.142 0-.657.407-1.029.842-1.155z"
						/>
					</svg>
					{{ $t('settings.spotify.title') }}
				</h3>
			</template>

			<RouterLink :to="{ name: 'Spotify Features' }">
				{{ $t('settings.spotify.question') }}
			</RouterLink>

			<div class="input-group">
				<p class="input-group-text">{{ $t('settings.spotify.clientID') }}</p>
				<input v-model="spotifyFeatures.clientId" type="text" />
			</div>

			<div class="input-group">
				<p class="input-group-text">{{ $t('settings.spotify.clientSecret') }}</p>
				<input v-model="spotifyFeatures.clientSecret" type="password" />
			</div>

			<div class="input-group">
				<p class="input-group-text">{{ $t('settings.spotify.username') }}</p>
				<input v-model="spotifyUser" type="text" />
			</div>
		</BaseAccordion>

		<footer class="bg-background-main">
			<button class="mr-2 btn btn-primary" @click="resetToDefault">{{ $t('settings.reset') }}</button>
			<button class="btn btn-primary" @click="saveSettings">{{ $t('settings.save') }}</button>
		</footer>
	</div>
</template>

<style lang="scss" scoped>
#logged_in_info {
	display: flex;
	align-items: center;
	flex-direction: column;
	justify-content: space-evenly;
	height: 250px;
}

.locale-flag {
	justify-content: center;
	cursor: pointer;
	width: 60px;

	&:not(:last-child) {
		margin-right: 10px;
	}

	&.locale-flag--current {
		svg {
			filter: brightness(1);
		}
	}

	&::v-deep svg {
		width: 40px !important;
		height: 40px !important;
		filter: brightness(0.5);
	}
}

.settings-group {
	border-top-width: 1px;
	border-color: hsl(0, 0%, 50%);
}

.settings-group__header {
	display: inline-flex;
	align-items: center;
	padding-top: 2rem;
	padding-bottom: 2rem;
	font-size: 1.5rem;

	i.material-icons {
		margin-right: 1rem;
	}
}

.settings-container {
	display: flex;

	&__half {
		width: 50%;
	}

	&__third {
		width: 33%;

		&--only-checkbox {
			display: flex;
			align-items: start;
			flex-direction: column;
			justify-content: center;
		}
	}

	&__half > *,
	&__third > * {
		margin-bottom: 1rem;
	}
}

.with-checkbox {
	display: flex;
	align-items: center;

	[type='checkbox'] {
		cursor: pointer;
	}

	.checkbox-text {
		margin-left: 10px;
		cursor: pointer;
		user-select: none;
	}
}

/* Input group */
.input-group {
	.input-group-text {
		margin-bottom: 0.5rem;
	}
}
</style>

<script>
import { mapActions, mapGetters } from 'vuex'
import { debounce } from 'lodash-es'

import { getSettingsData } from '@/data/settings'
import { trackTemplateVariables } from '@/data/file-templates'

import { toast } from '@/utils/toasts'
import { socket } from '@/utils/socket'
import { flags } from '@/utils/flags'
import { copyToClipboard } from '@/utils/utils'

import BaseAccordion from '@/components/globals/BaseAccordion.vue'
import TemplateVariablesList from '@components/settings/TemplateVariablesList.vue'
import { fetchData, sendToServer } from '@/utils/api'

export default {
	components: {
		BaseAccordion,
		TemplateVariablesList
	},
	data() {
		return {
			flags,
			currentLocale: this.$i18n.locale,
			locales: this.$i18n.availableLocales,
			settings: {
				tags: {}
			},
			lastSettings: {},
			spotifyFeatures: {},
			lastCredentials: {},
			defaultSettings: {},
			lastUser: '',
			spotifyUser: '',
			accountNum: 0,
			accounts: [],
			trackTemplateVariables
		}
	},
	computed: {
		...mapGetters({
			arl: 'getARL',
			user: 'getUser',
			isLoggedIn: 'isLoggedIn',
			clientMode: 'getClientMode',
			previewVolume: 'getPreviewVolume',
			hasSlimDownloads: 'getSlimDownloads',
			hasSlimSidebar: 'getSlimSidebar'
		}),
		needToWait() {
			return Object.keys(this.getSettings).length === 0
		},
		modelVolume: {
			get() {
				return this.previewVolume
			},
			set: debounce(function (value) {
				this.setPreviewVolume(value)
			}, 20)
		},
		modelSlimDownloads: {
			get() {
				return this.hasSlimDownloads
			},
			set(wantSlimDownloads) {
				this.setSlimDownloads(wantSlimDownloads)
			}
		},
		modelSlimSidebar: {
			get() {
				return this.hasSlimSidebar
			},
			set(wantSlimSidebar) {
				this.setSlimSidebar(wantSlimSidebar)
			}
		},
		pictureHref() {
			// Default image: https://e-cdns-images.dzcdn.net/images/user/125x125-000000-80-0-0.jpg
			return `https://e-cdns-images.dzcdn.net/images/user/${this.user.picture}/125x125-000000-80-0-0.jpg`
		}
	},
	async mounted() {
		const { settingsData, defaultSettingsData, spotifyCredentials } = await getSettingsData()

		this.defaultSettings = defaultSettingsData
		this.initSettings(settingsData, spotifyCredentials)

		// TODO Move in store
		let storedAccountNum = localStorage.getItem('accountNum')

		if (storedAccountNum) {
			this.accountNum = storedAccountNum
		}

		// TODO Move in store
		let spotifyUser = localStorage.getItem('spotifyUser')

		if (spotifyUser) {
			this.lastUser = spotifyUser
			this.spotifyUser = spotifyUser
			socket.emit('update_userSpotifyPlaylists', spotifyUser)
		}

		socket.on('updateSettings', this.updateSettings)
		socket.on('accountChanged', this.accountChanged)
		socket.on('familyAccounts', this.initAccounts)
		socket.on('downloadFolderSelected', this.downloadFolderSelected)
		socket.on('applogin_arl', this.loggedInViaDeezer)

		this.$on('hook:destroyed', () => {
			socket.off('updateSettings')
			socket.off('accountChanged')
			socket.off('familyAccounts')
			socket.off('downloadFolderSelected')
			socket.off('applogin_arl')
		})
	},
	methods: {
		...mapActions({
			dispatchARL: 'setARL',
			setPreviewVolume: 'setPreviewVolume',
			setSlimDownloads: 'setSlimDownloads',
			setSlimSidebar: 'setSlimSidebar'
		}),
		onTemplateVariableClick(templateName) {
			copyToClipboard(templateName)
			toast(`Copied ${templateName} to clipboard!`)
		},
		revertSettings() {
			this.settings = JSON.parse(JSON.stringify(this.lastSettings))
		},
		revertCredentials() {
			this.spotifyCredentials = JSON.parse(JSON.stringify(this.lastCredentials))
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
		saveSettings() {
			this.lastSettings = JSON.parse(JSON.stringify(this.settings))
			this.lastCredentials = JSON.parse(JSON.stringify(this.spotifyFeatures))

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
			socket.emit('selectDownloadFolder')
		},
		downloadFolderSelected(folder) {
			this.$set(this.settings, 'downloadLocation', folder)
		},
		loadSettings(data) {
			this.lastSettings = JSON.parse(JSON.stringify(data))
			this.settings = JSON.parse(JSON.stringify(data))
		},
		loadCredentials(credentials) {
			this.lastCredentials = JSON.parse(JSON.stringify(credentials))
			this.spotifyFeatures = JSON.parse(JSON.stringify(credentials))
		},
		async loggedInViaDeezer(arl) {
			this.dispatchARL({ arl })
			// this.login()
			// const res = await fetchData('login', { arl, force: true, child: this.accountNum })
		},
		async login() {
			let newArl = this.$refs.loginInput.value.trim()

			if (newArl && newArl !== this.arl) {
				const res = await fetchData('login', { arl: newArl, force: true, child: this.accountNum })
				this.loggedInViaDeezer(res.arl)
			}
		},
		appLogin(e) {
			socket.emit('applogin')
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
		initSettings(settings, credentials) {
			// this.loadDefaultSettings()
			this.loadSettings(settings)
			this.loadCredentials(credentials)

			toast(this.$t('settings.toasts.init'), 'settings')
		},
		updateSettings(newSettings, newCredentials) {
			this.loadSettings(newSettings)
			this.loadCredentials(newCredentials)

			toast(this.$t('settings.toasts.update'), 'settings')
		},
		resetToDefault() {
			const wantsToReset = confirm(this.$t('settings.resetMessage'))

			if (!wantsToReset) return

			this.settings = JSON.parse(JSON.stringify(this.defaultSettings))
			toast(this.$t('settings.toasts.reset'), 'settings')
		}
	}
}
</script>
