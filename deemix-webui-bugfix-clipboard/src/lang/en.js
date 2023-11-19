const en = {
	globals: {
		welcome: 'Welcome to deemix',
		back: 'back',
		loading: 'loading',
		download: 'Download {0}',
		by: 'by {0}',
		in: 'in {0}',
		download_hint: 'Download',
		play_hint: 'Play',
		toggle_download_tab_hint: 'Expand/Collapse',
		clean_queue_hint: 'Clear Finished',
		cancel_queue_hint: 'Cancel All',
		cut: 'cut',
		copy: 'copy',
		copyLink: 'copy link',
		copyImageLink: 'copy image link',
		copyDeezerLink: 'copy deezer link',
		paste: 'paste',
		listTabs: {
			empty: '',
			all: 'all',
			top_result: 'top result',
			album: 'album | albums',
			artist: 'artist | artists',
			single: 'single | singles',
			title: 'title | titles',
			track: 'track | tracks',
			trackN: '0 tracks | {n} track | {n} tracks',
			releaseN: '0 releases | {n} release | {n} releases',
			playlist: 'playlist | playlists',
			compile: 'compilation | compilations',
			ep: 'ep | eps',
			more: 'More albums',
			featured: 'Featured in',
			spotifyPlaylist: 'spotify playlist | spotify playlists',
			releaseDate: 'release date',
			error: 'error'
		}
	},
	about: {
		titles: {
			usefulLinks: 'Useful Links',
			bugReports: 'Bug Reports',
			contributing: 'Contributing',
			donations: 'Donations',
			license: 'License'
		},
		subtitles: {
			bugReports: "Is there something that isn't working in deemix? Tell us!",
			contributing: 'You want to contribute to this project? You can do it in different ways!',
			donations: 'You want to contribute monetarily? You could make a donation!'
		},
		usesLibrary: 'This app uses the <strong>deemix</strong> library, which you can use to make your own UI for deemix.',
		thanks: `Thanks to <strong>rtonno</strong>, <strong>uhwot</strong> and <strong>lollilol</strong> for helping me with this project and to <strong>BasCurtiz</strong> and <strong>scarvimane</strong> for making the icon.`,
		upToDate: `Stay up to date with the updates by following the <a href="https://t.me/RemixDevNews" target="_blank">news channel</a> on Telegram.`,
		officialWebsite: 'Official Website',
		officialRepo: 'Official Library Repository',
		officialWebuiRepo: 'Official WebUI Repository',
		officialSubreddit: 'Official Subreddit',
		newsChannel: 'News Channel',
		questions: `If you have questions or problems with the app, search for a solution on the <a href="https://www.reddit.com/r/deemix" target="_blank">subreddit</a> first. Then, if you don't find anything you can make a post with your issue on the subreddit.`,
		beforeReporting: `Before reporting a bug make sure you're running the latest version of the app and that what you want to report is actually a bug and not something that's wrong only on your end.`,
		beSure: `Make sure the bug is reproducible on other machines and also <strong>DO NOT</strong> report a bug if it's already been reported.`,
		duplicateReports: 'Duplicate bug reports will be closed, so keep an eye out on that.',
		dontOpenIssues: `<strong>DO NOT</strong> open issues for asking questions, there is a subreddit for that.`,
		newUI: `If you're fluent in python you could try to make a new UI for the app using the base library, or fix bugs in the library with a pull request on the <a href="https://codeberg.org/RemixDev/deemix" target="_blank">repo</a>.`,
		acceptFeatures: `I accept features as well, but no complex things, as they can be implementend directly in the app and not the library.`,
		otherLanguages: `If you're fluent in another programming language you could try to port deemix into other programming languages!`,
		understandingCode: `You need help understanding the code? Just hit RemixDev up on Telegram or Reddit.`,
		contributeWebUI: `If you know Vue.js (JavaScript), HTML or CSS you could contribute to the <a href="https://codeberg.org/RemixDev/deemix-webui" target="_blank">WebUI</a>.`,
		itsFree: `You should remember that <strong>this is a free project</strong> and <strong>you should support the artists you love</strong> before supporting the developers.`,
		notObligated: `Don't feel obligated to donate, I appreciate you anyway!`,
		lincensedUnder: `This work is licensed under the
			<a rel="license" href="https://www.gnu.org/licenses/gpl-3.0.en.html" target="_blank"
				>GNU General Public License 3.0</a
			>.`
	},
	charts: {
		title: 'Charts',
		changeCountry: 'Change Country',
		download: 'Download Chart'
	},
	errors: {
		title: 'Errors for {0}',
		ids: {
			invalidURL: 'URL not recognized',
			unsupportedURL: 'URL not supported yet',
			ISRCnotOnDeezer: 'Track ISRC is not available on Deezer',
			notYourPrivatePlaylist: "You can't download others private playlists.",
			spotifyDisabled: 'Spotify Features is not setted up correctly.',
			trackNotOnDeezer: 'Track not found on Deezer!',
			albumNotOnDeezer: 'Album not found on Deezer!',
			notOnDeezer: 'Track not available on Deezer!',
			notEncoded: 'Track not yet encoded!',
			notEncodedNoAlternative: 'Track not yet encoded and no alternative found!',
			wrongBitrate: 'Track not found at desired bitrate.',
			wrongBitrateNoAlternative: 'Track not found at desired bitrate and no alternative found!',
			no360RA: 'Track is not available in Reality Audio 360.',
			notAvailable: "Track not available on Deezer's servers!",
			notAvailableNoAlternative: "Track not available on Deezer's servers and no alternative found!"
		}
	},
	favorites: {
		title: 'Favorites',
		noPlaylists: 'No Playlists found',
		noAlbums: 'No Favorite Albums found',
		noArtists: 'No Favorite Artists found',
		noTracks: 'No Favorite Tracks found'
	},
	home: {
		needTologin: 'You need to log into your Deezer account before you can start downloading.',
		openSettings: 'Open Settings',
		sections: {
			popularPlaylists: 'Popular playlists',
			popularAlbums: 'Most streamed albums'
		}
	},
	linkAnalyzer: {
		info: 'You can use this section to find more information about the link you are trying to download.',
		useful:
			"This is useful if you're trying to download some tracks that are not available in your country and want to know where they are available, for instance.",
		linkNotSupported: 'This link is not yet supported',
		linkNotSupportedYet: 'Seems like this link is not yet supported, try analyzing another one.',
		table: {
			id: 'ID',
			isrc: 'ISRC',
			upc: 'UPC',
			duration: 'Duration',
			diskNumber: 'Disk Number',
			trackNumber: 'Track Number',
			releaseDate: 'Release Date',
			bpm: 'BPM',
			label: 'Label',
			recordType: 'Record Type',
			genres: 'Genres',
			tracklist: 'Tracklist'
		}
	},
	search: {
		startSearching: 'Start searching!',
		description:
			'You can search a track, a whole album, an artist, a playlist.... everything! You can also paste a Deezer link',
		fans: '{0} fans',
		noResults: 'No results',
		noResultsTrack: 'No Tracks found',
		noResultsAlbum: 'No Albums found',
		noResultsArtist: 'No Artists found',
		noResultsPlaylist: 'No Playlists found'
	},
	searchbar: 'Search anything you want (or just paste a link)',
	downloads: 'downloads',
	toasts: {
		addedToQueue: '{0} added to queue',
		alreadyInQueue: '{0} is already in queue!',
		finishDownload: '{0} finished downloading.',
		allDownloaded: 'All downloads completed!',
		refreshFavs: 'Refresh completed!',
		loggingIn: 'Logging in...',
		loggedIn: 'Logged in',
		alreadyLogged: 'Already logged in',
		loginFailed: "Couldn't log in",
		loggedOut: 'Logged out',
		cancellingCurrentItem: 'Cancelling current item.',
		currentItemCancelled: 'Current item cancelled.',
		startAddingArtist: 'Adding {0} albums to queue',
		finishAddingArtist: 'Added {0} albums to queue',
		startConvertingSpotifyPlaylist: 'Converting spotify tracks to Deezer tracks',
		finishConvertingSpotifyPlaylist: 'Spotify playlist converted',
		loginNeededToDownload: 'You need to log in to download tracks!'
	},
	settings: {
		title: 'Settings',
		languages: 'Languages',
		login: {
			title: 'Login',
			loggedIn: 'You are logged in as {username}',
			arl: {
				question: 'How do I get my own ARL?',
				update: 'Update ARL'
			},
			logout: 'Logout'
		},
		appearance: {
			title: 'Appearance',
			slimDownloadTab: 'Slim download tab'
		},
		downloadPath: {
			title: 'Download Path'
		},
		templates: {
			title: 'Templates',
			tracknameTemplate: 'Trackname template',
			albumTracknameTemplate: 'Album track template',
			playlistTracknameTemplate: 'Playlist track template'
		},
		folders: {
			title: 'Folders',
			createPlaylistFolder: 'Create folder for playlists',
			playlistNameTemplate: 'Playlist folder template',
			createArtistFolder: 'Create folder for artist',
			artistNameTemplate: 'Artist folder template',
			createAlbumFolder: 'Create folder for album',
			albumNameTemplate: 'Album folder template',
			createCDFolder: 'Create folder for CDs',
			createStructurePlaylist: 'Create folder structure for playlists',
			createSingleFolder: 'Create folder structure for singles'
		},
		trackTitles: {
			title: 'Track titles',
			padTracks: 'Pad tracks',
			paddingSize: 'Overwrite padding size',
			illegalCharacterReplacer: 'Illegal Character replacer'
		},
		downloads: {
			title: 'Downloads',
			queueConcurrency: 'Concurrent Downloads',
			maxBitrate: {
				title: 'Preferred Bitrate',
				9: 'FLAC 1411kbps',
				3: 'MP3 320kbps',
				1: 'MP3 128kbps'
			},
			overwriteFile: {
				title: 'Should I overwrite the files?',
				y: 'Yes, overwrite the file',
				n: "No, don't overwrite the file",
				t: 'Overwrite only the tags',
				b: 'No, keep both files and add a number to the duplicate'
			},
			fallbackBitrate: 'Bitrate fallback',
			fallbackSearch: 'Search fallback',
			logErrors: 'Create log files for errors',
			logSearched: 'Create log files for searched tracks',
			createM3U8File: 'Create playlist file',
			syncedLyrics: 'Create .lyr files (Sync Lyrics)',
			playlistFilenameTemplate: 'Playlist filename template',
			saveDownloadQueue: 'Save download queue when closing the app'
		},
		covers: {
			title: 'Album covers',
			saveArtwork: 'Save Covers',
			coverImageTemplate: 'Cover name template',
			saveArtworkArtist: 'Save artist image',
			artistImageTemplate: 'Artist image template',
			localArtworkSize: 'Local artwork size',
			embeddedArtworkSize: 'Embedded artwork size',
			localArtworkFormat: {
				title: 'What format do you want the local artwork to be?',
				jpg: 'A jpeg image',
				png: 'A png image',
				both: 'Both a jpeg and a png'
			},
			jpegImageQuality: 'JPEG image quality'
		},
		tags: {
			head: 'Which tags to save',
			title: 'Title',
			artist: 'Artist',
			album: 'Album',
			cover: 'Cover',
			trackNumber: 'Track Number',
			trackTotal: 'Track Total',
			discNumber: 'Disc Number',
			discTotal: 'Disc Total',
			albumArtist: 'Album Artist',
			genre: 'Genre',
			year: 'Year',
			date: 'Date',
			explicit: 'Explicit Lyrics',
			isrc: 'ISRC',
			length: 'Track Length',
			barcode: 'Album Barcode (UPC)',
			bpm: 'BPM',
			replayGain: 'Replay Gain',
			label: 'Album Label',
			lyrics: 'Unsynchronized Lyrics',
			copyright: 'Copyright',
			composer: 'Composer',
			involvedPeople: 'Involved People'
		},
		other: {
			title: 'Other',
			savePlaylistAsCompilation: 'Save playlists as compilation',
			useNullSeparator: 'Use null separator',
			saveID3v1: 'Save ID3v1 as well',
			multiArtistSeparator: {
				title: 'How would you like to separate your artists?',
				nothing: 'Save only the main artist',
				default: 'Using standard specification',
				andFeat: 'Using & and feat.',
				using: 'Using "{0}"'
			},
			singleAlbumArtist: 'Save only the main album artist',
			albumVariousArtists: 'Keep "Various Artists" in the Album Artists',
			removeAlbumVersion: 'Remove "Album Version" from track title',
			removeDuplicateArtists: 'Remove combinations of artists',
			dateFormat: {
				title: 'Date format for FLAC files',
				year: 'YYYY',
				month: 'MM',
				day: 'DD'
			},
			featuredToTitle: {
				title: 'What should I do with featured artists?',
				0: 'Nothing',
				1: 'Remove it from the title',
				3: 'Remove it from the title and the album title',
				2: 'Move it to the title'
			},
			titleCasing: 'Title casing',
			artistCasing: 'Artist casing',
			casing: {
				nothing: 'Keep unchanged',
				lower: 'lowercase',
				upper: 'UPPERCASE',
				start: 'Start Of Each Word',
				sentence: 'Like a sentence'
			},
			previewVolume: 'Preview Volume',
			executeCommand: {
				title: 'Command to execute after download',
				description: 'Leave blank for no action'
			}
		},
		spotify: {
			title: 'Spotify Features',
			clientID: 'Spotify ClientID',
			clientSecret: 'Spotify Client Secret',
			username: 'Spotify Username'
		},
		reset: 'Reset to Default',
		save: 'Save',
		toasts: {
			init: 'Settings loaded!',
			update: 'Settings updated!',
			ARLcopied: 'ARL copied to clipboard'
		}
	},
	sidebar: {
		home: 'home',
		search: 'search',
		charts: 'charts',
		favorites: 'favorites',
		linkAnalyzer: 'link analyzer',
		settings: 'settings',
		about: 'about'
	},
	tracklist: {
		downloadSelection: 'Download selection'
	}
}

export default en
