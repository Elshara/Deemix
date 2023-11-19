import { socket } from '@/utils/socket'

let settingsData = {}
let defaultSettingsData = {}
let spotifyCredentials = {}

let cached = false

export function getSettingsData() {
	if (cached) {
		return { settingsData, defaultSettingsData, spotifyCredentials }
	} else {
		socket.emit('get_settings_data')

		return new Promise((resolve, reject) => {
			socket.on('init_settings', (settings, credentials, defaults) => {
				settingsData = settings
				defaultSettingsData = defaults
				spotifyCredentials = credentials
				// cached = true

				socket.off('init_settings')
				resolve({ settingsData, defaultSettingsData, spotifyCredentials })
			})
		})
	}
}

/**
 * @returns	{number}
 */
export function getInitialPreviewVolume() {
	let volume = parseInt(localStorage.getItem('previewVolume'))

	if (isNaN(volume)) {
		volume = 80 // Default
		localStorage.setItem('previewVolume', volume.toString())
	}

	return volume
}

/**
 * @returns	{boolean}
 */
export function checkInitialSlimDownloads() {
	return 'true' === localStorage.getItem('slimDownloads')
}

/**
 * @returns	{boolean}
 */
export function checkInitialSlimSidebar() {
	return 'true' === localStorage.getItem('slimSidebar')
}
