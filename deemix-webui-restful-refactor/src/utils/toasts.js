import Toastify from 'toastify-js'
import 'toastify-js/src/toastify.css'
import '@/styles/css/toasts.css'

import { socket } from '@/utils/socket'

const sharedOptions = {
	gravity: 'bottom',
	position: 'left'
}

let toastsWithId = {}

export const toast = function(msg, icon = null, dismiss = true, id = null) {
	if (toastsWithId[id]) {
		let toastObj = toastsWithId[id]

		let toastElement = document.querySelectorAll(`div.toastify[toast_id=${id}]`)

		if (msg) {
			toastElement.forEach(toast => {
				const messages = toast.querySelectorAll('.toast-message')

				messages.forEach(message => {
					message.innerHTML = msg
				})
			})
		}

		if (icon) {
			if (icon == 'loading') {
				icon = `<div class="circle-loader"></div>`
			} else {
				icon = `<i class="material-icons">${icon}</i>`
			}

			toastElement.forEach(toast => {
				const icons = toast.querySelectorAll('.toast-icon')

				icons.forEach(toastIcon => {
					toastIcon.innerHTML = icon
				})
			})
		}
		if (dismiss !== null && dismiss) {
			toastElement.forEach(toast => {
				toast.classList.add('dismissable')
			})

			setTimeout(() => {
				toastObj.hideToast()

				delete toastsWithId[id]
			}, 3000)
		}
	} else {
		if (icon == null) {
			icon = ''
		} else if (icon == 'loading') {
			icon = `<div class="circle-loader"></div>`
		} else {
			icon = `<i class="material-icons">${icon}</i>`
		}

		let toastObj = Toastify({
			...sharedOptions,
			text: `<span class="toast-icon">${icon}</span><span class="toast-message">${msg}</toast>`,
			duration: dismiss ? 3000 : 0,
			className: dismiss ? 'dismissable' : '',
			onClick: function() {
				let dismissable = true

				if (id) {
					let toastClasses = document.querySelector(`div.toastify[toast_id=${id}]`).classList

					if (toastClasses) {
						dismissable = Array.prototype.slice.call(toastClasses).indexOf('dismissable') != -1
					}
				}
				if (toastObj && dismissable) {
					toastObj.hideToast()

					if (id) {
						delete toastsWithId[id]
					}
				}
			},
			offset: {
				x: 'true' === localStorage.getItem('slimSidebar') ? '3rem': '14rem'
			}
		}).showToast()
		if (id) {
			toastsWithId[id] = toastObj

			toastObj.toastElement.setAttribute('toast_id', id)
		}
	}
}

socket.on('toast', data => {
	const { msg, icon, dismiss, id } = data

	toast(msg, icon || null, dismiss !== undefined ? dismiss : true, id || null)
})
