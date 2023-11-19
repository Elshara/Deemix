import Toastify from 'toastify-js'
import $ from 'jquery'

import { socket } from '@/utils/socket'

let toastsWithId = {}

export const toast = function(msg, icon = null, dismiss = true, id = null) {
	if (toastsWithId[id]) {
		let toastObj = toastsWithId[id]
		let toastDOM = $(`div.toastify[toast_id=${id}]`)
		if (msg) {
			toastDOM.find('.toast-message').html(msg)
		}
		if (icon) {
			if (icon == 'loading') icon = `<div class="circle-loader"></div>`
			else icon = `<i class="material-icons">${icon}</i>`
			toastDOM.find('.toast-icon').html(icon)
		}
		if (dismiss !== null && dismiss) {
			toastDOM.addClass('dismissable')
			setTimeout(function() {
				toastObj.hideToast()
				delete toastsWithId[id]
			}, 3000)
		}
	} else {
		if (icon == null) icon = ''
		else if (icon == 'loading') icon = `<div class="circle-loader"></div>`
		else icon = `<i class="material-icons">${icon}</i>`
		let toastObj = Toastify({
			text: `<span class="toast-icon">${icon}</span><span class="toast-message">${msg}</toast>`,
			duration: dismiss ? 3000 : 0,
			gravity: 'bottom',
			position: 'left',
			className: dismiss ? 'dismissable' : '',
			onClick: function(){
				let dismissable = true
				if (id){
					let toastClasses = document.querySelector(`div.toastify[toast_id=${id}]`).classList
					if (toastClasses){
						dismissable = Array.from(toastClasses).indexOf('dismissable') != -1
					}
				}
				if (toastObj && dismissable) {
					toastObj.hideToast()
					if (id) delete toastsWithId[id]
				}
			}
		}).showToast()
		if (id) {
			toastsWithId[id] = toastObj
			$(toastObj.toastElement).attr('toast_id', id)
		}
	}
}

socket.on('toast', data => {
	const { msg, icon, dismiss, id } = data

	toast(msg, icon || null, dismiss !== undefined ? dismiss : true, id || null)
})
