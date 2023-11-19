import Vue from 'vue'

// Object is needed for vue change detection
window.vol = {
	preview_max_volume: 100
}

import App from '@components/App.vue'
import i18n from '@/plugins/i18n'
// import router from '@/plugins/router'

import $ from 'jquery'
import { socket } from '@/utils/socket'
import { toast } from '@/utils/toasts'
import { init as initTabs } from '@js/tabs.js'

/* ===== App initialization ===== */

function startApp() {
	mountApp()
	initTabs()
}

function mountApp() {
	new Vue({
		// router,
		i18n,
		render: h => h(App)
	}).$mount('#app')
}

function initClient() {
	window.clientMode = true
	document.querySelector(`#open_downloads_folder`).classList.remove('hide')
	document.querySelector(`#select_downloads_folder`).classList.remove('hide')
}

document.addEventListener('DOMContentLoaded', startApp)
window.addEventListener('pywebviewready', initClient)

/* ===== Socketio listeners ===== */

// Debug messages for socketio
socket.on('message', function(msg) {
	console.log(msg)
})

socket.on('logging_in', function() {
	toast(i18n.t('toasts.loggingIn'), 'loading', false, 'login-toast')
})

socket.on('init_autologin', function() {
	let arl = localStorage.getItem('arl')
	let accountNum = localStorage.getItem('accountNum')
	if (arl) {
		arl = arl.trim()
		if (accountNum != 0) {
			socket.emit('login', arl, true, accountNum)
		} else {
			socket.emit('login', arl)
		}
	}
})

socket.on('logged_in', function(data) {
	switch (data.status) {
		case 1:
		case 3:
			toast(i18n.t('toasts.loggedIn'), 'done', true, 'login-toast')
			if (data.arl) {
				localStorage.setItem('arl', data.arl)
				$('#login_input_arl').val(data.arl)
			}
			$('#open_login_prompt').hide()
			if (data.user) {
				$('#settings_username').text(data.user.name)
				$('#settings_picture').attr(
					'src',
					`https://e-cdns-images.dzcdn.net/images/user/${data.user.picture}/125x125-000000-80-0-0.jpg`
				)
				// $('#logged_in_info').show()
				document.getElementById('logged_in_info').classList.remove('hide')
			}
			document.getElementById('home_not_logged_in').classList.add('hide')
			break
		case 2:
			toast(i18n.t('toasts.alreadyLogged'), 'done', true, 'login-toast')
			if (data.user) {
				$('#settings_username').text(data.user.name)
				$('#settings_picture').attr(
					'src',
					`https://e-cdns-images.dzcdn.net/images/user/${data.user.picture}/125x125-000000-80-0-0.jpg`
				)
				// $('#logged_in_info').show()
				document.getElementById('logged_in_info').classList.remove('hide')
			}
			document.getElementById('home_not_logged_in').classList.add('hide')
			break
		case 0:
			toast(i18n.t('toasts.loginFailed'), 'close', true, 'login-toast')
			localStorage.removeItem('arl')
			$('#login_input_arl').val('')
			$('#open_login_prompt').show()
			document.getElementById('logged_in_info').classList.add('hide')
			// $('#logged_in_info').hide()
			$('#settings_username').text('Not Logged')
			$('#settings_picture').attr('src', `https://e-cdns-images.dzcdn.net/images/user/125x125-000000-80-0-0.jpg`)
			document.getElementById('home_not_logged_in').classList.remove('hide')
			break
	}
})

socket.on('logged_out', function() {
	toast(i18n.t('toasts.loggedOut'), 'done', true, 'login-toast')
	localStorage.removeItem('arl')
	$('#login_input_arl').val('')
	$('#open_login_prompt').show()
	document.getElementById('logged_in_info').classList.add('hide')
	$('#settings_username').text('Not Logged')
	$('#settings_picture').attr('src', `https://e-cdns-images.dzcdn.net/images/user/125x125-000000-80-0-0.jpg`)
	document.getElementById('home_not_logged_in').classList.remove('hide')
})

socket.on('cancellingCurrentItem', function(uuid) {
	toast(i18n.t('toasts.cancellingCurrentItem'), 'loading', false, 'cancelling_' + uuid)
})

socket.on('currentItemCancelled', function(uuid) {
	toast(i18n.t('toasts.currentItemCancelled'), 'done', true, 'cancelling_' + uuid)
})

socket.on('startAddingArtist', function(data) {
	toast(i18n.t('toasts.startAddingArtist', [data.name]), 'loading', false, 'artist_' + data.id)
})

socket.on('finishAddingArtist', function(data) {
	toast(i18n.t('toasts.finishAddingArtist', [data.name]), 'done', true, 'artist_' + data.id)
})

socket.on('startConvertingSpotifyPlaylist', function(id) {
	toast(i18n.t('toasts.startConvertingSpotifyPlaylist'), 'loading', false, 'spotifyplaylist_' + id)
})

socket.on('finishConvertingSpotifyPlaylist', function(id) {
	toast(i18n.t('toasts.finishConvertingSpotifyPlaylist'), 'done', true, 'spotifyplaylist_' + id)
})

socket.on('errorMessage', function(error) {
	toast(error, 'error')
})

socket.on('queueError', function(queueItem) {
	if (queueItem.errid) toast(i18n.t(`errors.ids.${queueItem.errid}`), 'error')
	else toast(queueItem.error, 'error')
})

socket.on('alreadyInQueue', function(data) {
	toast(i18n.t('toasts.alreadyInQueue', [data.title]), 'playlist_add_check')
})

socket.on('loginNeededToDownload', function(data) {
	toast(i18n.t('toasts.loginNeededToDownload'), 'report')
})
