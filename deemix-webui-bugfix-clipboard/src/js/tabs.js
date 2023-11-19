import { socket } from '@/utils/socket'
import EventBus from '@/utils/EventBus'

/* ===== Globals ====== */
window.search_selected = ''
window.main_selected = ''
window.windows_stack = []
window.currentStack = {}

// Exporting this function out of the default export
// because it's used in components that are needed
// in this file too
export function showView(viewType, event) {
	// console.error('SHOW VIEW')
	const {
		currentTarget: {
			dataset: { id }
		}
	} = event

	switch (viewType) {
		case 'artist':
			EventBus.$emit('artistTab:reset')
			break
		case 'album':
		case 'playlist':
		case 'spotifyplaylist':
			EventBus.$emit('tracklistTab:reset')
			break

		default:
			break
	}

	socket.emit('getTracklist', { type: viewType, id })
	showTab(viewType, id)
}

/**
 * Changes the tab to the wanted one
 * Need to understand the difference from showTab
 *
 * Needs EventBus
 */
export function changeTab(sidebarEl, section, tabName) {
	// console.error('CHANGE TAB')
	window.windows_stack = []
	window.currentStack = {}

	// * The visualized content of the tab
	// ! Can be more than one per tab, happens in MainSearch and Favorites tab
	// ! because they have more tablinks (see below)
	const tabContent = document.getElementsByClassName(`${section}_tabcontent`)

	for (let i = 0; i < tabContent.length; i++) {
		tabContent[i].style.display = 'none'
	}

	// * Tabs inside the actual tab (like albums, tracks, playlists...)
	const tabLinks = document.getElementsByClassName(`${section}_tablinks`)

	for (let i = 0; i < tabLinks.length; i++) {
		tabLinks[i].classList.remove('active')
	}

	if (tabName === 'settings_tab' && window.main_selected !== 'settings_tab') {
		EventBus.$emit('settingsTab:revertSettings')
		EventBus.$emit('settingsTab:revertCredentials')
	}

	document.getElementById(tabName).style.display = 'block'

	if (section === 'main') {
		window.main_selected = tabName
	} else if ('search' === section) {
		window.search_selected = tabName
	}

	sidebarEl.classList.add('active')

	// Check if you need to load more content in the search tab
	if (
		window.main_selected === 'search_tab' &&
		['track_search', 'album_search', 'artist_search', 'playlist_search'].indexOf(window.search_selected) !== -1
	) {
		EventBus.$emit('mainSearch:checkLoadMoreContent', window.search_selected)
	}
}

/**
 * Shows the passed tab, keeping track of the one that the user is coming from.
 *
 * Needs EventBus
 */
function showTab(type, id, back = false) {
	if (window.windows_stack.length === 0) {
		window.windows_stack.push({ tab: window.main_selected })
	} else if (!back) {
		if (window.currentStack.type === 'artist') {
			EventBus.$emit('artistTab:updateSelected')
		}

		window.windows_stack.push(window.currentStack)
	}

	window.tab = type === 'artist' ? 'artist_tab' : 'tracklist_tab'

	window.currentStack = { type, id }
	let tabcontent = document.getElementsByClassName('main_tabcontent')

	for (let i = 0; i < tabcontent.length; i++) {
		tabcontent[i].style.display = 'none'
	}

	document.getElementById(window.tab).style.display = 'block'

	EventBus.$emit('trackPreview:stopStackedTabsPreview')
}

/**
 * Goes back to the previous tab according to the global window stack.
 *
 * Needs EventBus and socket
 */
function backTab() {
	if (window.windows_stack.length == 1) {
		document.getElementById(`main_${window.main_selected}link`).click()
	} else {
		// Retrieving tab type and tab id
		let data = window.windows_stack.pop()
		let { type, id, selected } = data

		if (type === 'artist') {
			EventBus.$emit('artistTab:reset')

			if (selected) {
				EventBus.$emit('artistTab:changeTab', selected)
			}
		} else {
			EventBus.$emit('tracklistTab:reset')
		}

		socket.emit('getTracklist', { type, id })
		showTab(type, id, true)
	}

	EventBus.$emit('trackPreview:stopStackedTabsPreview')
}

function _linkListeners() {
	const backButtons = Array.prototype.slice.call(document.getElementsByClassName('back-button'))

	backButtons.forEach(button => {
		button.addEventListener('click', backTab)
	})
}

export function init() {
	// Open default tab
	changeTab(document.getElementById('main_home_tablink'), 'main', 'home_tab')

	_linkListeners()
}
