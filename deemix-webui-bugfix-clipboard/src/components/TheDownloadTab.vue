<template>
	<div
		id="download_tab_container"
		class="tab_hidden"
		@transitionend="$refs.container.style.transition = ''"
		ref="container"
		:data-label="$t('downloads')"
	>
		<div id="download_tab_drag_handler" @mousedown.prevent="startDrag" ref="dragHandler"></div>
		<i
			id="toggle_download_tab"
			class="material-icons download_bar_icon"
			@click.prevent="toggleDownloadTab"
			ref="toggler"
			:title="$t('globals.toggle_download_tab_hint')"
		></i>
		<div id="queue_buttons">
			<i id="open_downloads_folder" class="material-icons download_bar_icon hide" @click="openDownloadsFolder">
				folder_open
			</i>
			<i id="clean_queue" class="material-icons download_bar_icon" @click="cleanQueue" :title="$t('globals.clean_queue_hint')">clear_all</i>
			<i id="cancel_queue" class="material-icons download_bar_icon" @click="cancelQueue" :title="$t('globals.cancel_queue_hint')">delete_sweep</i>
		</div>
		<div id="download_list" @click="handleListClick" ref="list"></div>
	</div>
</template>

<script>
import $ from 'jquery'
import { socket } from '@/utils/socket'
import { toast } from '@/utils/toasts'

const tabMinWidth = 250
const tabMaxWidth = 500

export default {
	data: () => ({
		cachedTabWidth: parseInt(localStorage.getItem('downloadTabWidth')) || 300,
		queue: [],
		queueList: {},
		queueComplete: []
	}),
	mounted() {
		socket.on('startDownload', this.startDownload)
		socket.on('init_downloadQueue', this.initQueue)
		socket.on('addedToQueue', this.addToQueue)
		socket.on('updateQueue', this.updateQueue)
		socket.on('removedFromQueue', this.removeFromQueue)
		socket.on('finishDownload', this.finishDownload)
		socket.on('removedAllDownloads', this.removeAllDownloads)
		socket.on('removedFinishedDownloads', this.removedFinishedDownloads)

		// Check if download tab has slim entries
		if ('true' === localStorage.getItem('slimDownloads')) {
			this.$refs.list.classList.add('slim')
		}

		if ('true' === localStorage.getItem('downloadTabOpen')) {
			this.$refs.container.classList.remove('tab_hidden')

			this.setTabWidth(this.cachedTabWidth)
		}

		document.addEventListener('mouseup', () => {
			document.removeEventListener('mousemove', this.handleDrag)
		})

		window.addEventListener('beforeunload', () => {
			localStorage.setItem('downloadTabWidth', this.cachedTabWidth)
		})
	},
	methods: {
		setTabWidth(newWidth) {
			if (undefined === newWidth) {
				this.$refs.container.style.width = ''
				this.$refs.list.style.width = ''
			} else {
				this.$refs.container.style.width = newWidth + 'px'
				this.$refs.list.style.width = newWidth + 'px'
			}
		},
		handleListClick(event) {
			const { target } = event

			if (!target.matches('.queue_icon[data-uuid]')) {
				return
			}

			let icon = target.innerText
			let uuid = $(target).data('uuid')

			switch (icon) {
				case 'remove':
					socket.emit('removeFromQueue', uuid)
					break
				default:
			}
		},
		initQueue(data) {
			const { queue: initQueue, queueComplete: initQueueComplete, currentItem, queueList: initQueueList } = data

			if (initQueueComplete.length) {
				initQueueComplete.forEach(item => {
					initQueueList[item].init = true
					this.addToQueue(initQueueList[item])
				})
			}

			if (currentItem) {
				initQueueList[currentItem].init = true
				this.addToQueue(initQueueList[currentItem], true)
			}

			initQueue.forEach(item => {
				initQueueList[item].init = true
				this.addToQueue(initQueueList[item])
			})
		},
		addToQueue(queueItem, current = false) {
			this.queueList[queueItem.uuid] = queueItem

			if (queueItem.downloaded + queueItem.failed == queueItem.size) {
				if (this.queueComplete.indexOf(queueItem.uuid) == -1) {
					this.queueComplete.push(queueItem.uuid)
				}
			} else {
				if (this.queue.indexOf(queueItem.uuid) == -1) {
					this.queue.push(queueItem.uuid)
				}
			}

			let queueDOM = document.getElementById('download_' + queueItem.uuid)

			if (typeof queueDOM == 'undefined' || queueDOM == null) {
				$(this.$refs.list).append(
					`<div class="download_object" id="download_${queueItem.uuid}" data-deezerid="${queueItem.id}">
						<div class="download_info">
							<img width="75px" class="rounded coverart" src="${queueItem.cover}" alt="Cover ${queueItem.title}"/>
							<div class="download_info_data">
								<span class="download_line">${queueItem.title}</span> <span class="download_slim_separator"> - </span>
								<span class="secondary-text">${queueItem.artist}</span>
							</div>
							<div class="download_info_status">
								<span class="download_line"><span class="queue_downloaded">${queueItem.downloaded + queueItem.failed}</span>/${
						queueItem.size
					}</span>
							</div>
						</div>
						<div class="download_bar">
							<div class="progress"><div id="bar_${queueItem.uuid}" class="indeterminate"></div></div>
							<i class="material-icons queue_icon" data-uuid="${queueItem.uuid}">remove</i>
						</div>
					</div>`
				)
			}

			if (queueItem.progress > 0 || current) {
				this.startDownload(queueItem.uuid)
			}

			$('#bar_' + queueItem.uuid).css('width', queueItem.progress + '%')

			if (queueItem.failed >= 1 && $('#download_' + queueItem.uuid + ' .queue_failed').length == 0) {
				$('#download_' + queueItem.uuid + ' .download_info_status').append(
					`<span class="secondary-text inline-flex"><span class="download_slim_separator">(</span><span class="queue_failed_button inline-flex"><span class="queue_failed">${queueItem.failed}</span><i class="material-icons">error_outline</i></span><span class="download_slim_separator">)</span></span>`
				)
			}

			if (queueItem.downloaded + queueItem.failed == queueItem.size) {
				let resultIcon = $('#download_' + queueItem.uuid).find('.queue_icon')

				if (queueItem.failed == 0) {
					resultIcon.text('done')
				} else {
					let failedButton = $('#download_' + queueItem.uuid).find('.queue_failed_button')

					resultIcon.addClass('clickable')
					failedButton.addClass('clickable')

					resultIcon.bind('click', { item: queueItem }, this.showErrorsTab)
					failedButton.bind('click', { item: queueItem }, this.showErrorsTab)

					if (queueItem.failed >= queueItem.size) {
						resultIcon.text('error')
					} else {
						resultIcon.text('warning')
					}
				}
			}

			if (!queueItem.init) {
				toast(this.$t('toasts.addedToQueue', [queueItem.title]), 'playlist_add_check')
			}
		},
		updateQueue(update) {
			// downloaded and failed default to false?
			const { uuid, downloaded, failed, progress, error, data, errid } = update

			if (uuid && this.queue.indexOf(uuid) > -1) {
				if (downloaded) {
					this.queueList[uuid].downloaded++
					$('#download_' + uuid + ' .queue_downloaded').text(
						this.queueList[uuid].downloaded + this.queueList[uuid].failed
					)
				}

				if (failed) {
					this.queueList[uuid].failed++
					$('#download_' + uuid + ' .queue_downloaded').text(
						this.queueList[uuid].downloaded + this.queueList[uuid].failed
					)
					if (this.queueList[uuid].failed == 1 && $('#download_' + uuid + ' .queue_failed').length == 0) {
						$('#download_' + uuid + ' .download_info_status').append(
							`<span class="secondary-text inline-flex"><span class="download_slim_separator">(</span><span class="queue_failed_button inline-flex"><span class="queue_failed">1</span> <i class="material-icons">error_outline</i></span><span class="download_slim_separator">)</span></span>`
						)
					} else {
						$('#download_' + uuid + ' .queue_failed').text(this.queueList[uuid].failed)
					}

					this.queueList[uuid].errors.push({ message: error, data: data, errid: errid })
				}

				if (progress) {
					this.queueList[uuid].progress = progress
					$('#bar_' + uuid).css('width', progress + '%')
				}
			}
		},
		removeFromQueue(uuid) {
			let index = this.queue.indexOf(uuid)

			if (index > -1) {
				this.queue.splice(index, 1)
				$(`#download_${this.queueList[uuid].uuid}`).remove()
				delete this.queueList[uuid]
			}
		},
		removeAllDownloads(currentItem) {
			this.queueComplete = []

			if (currentItem == '') {
				this.queue = []
				this.queueList = {}
				$(listEl).html('')
			} else {
				this.queue = [currentItem]
				let tempQueueItem = this.queueList[currentItem]
				this.queueList = {}
				this.queueList[currentItem] = tempQueueItem

				$('.download_object').each(function(index) {
					if ($(this).attr('id') != 'download_' + currentItem) $(this).remove()
				})
			}
		},
		removedFinishedDownloads() {
			this.queueComplete.forEach(item => {
				$('#download_' + item).remove()
			})

			this.queueComplete = []
		},
		toggleDownloadTab(clickEvent) {
			this.setTabWidth()

			this.$refs.container.style.transition = 'all 250ms ease-in-out'

			// Toggle returns a Boolean based on the action it performed
			let isHidden = this.$refs.container.classList.toggle('tab_hidden')

			if (!isHidden) {
				this.setTabWidth(this.cachedTabWidth)
			}

			localStorage.setItem('downloadTabOpen', !isHidden)
		},
		cleanQueue() {
			socket.emit('removeFinishedDownloads')
		},
		cancelQueue() {
			socket.emit('cancelAllDownloads')
		},
		finishDownload(uuid) {
			if (this.queue.indexOf(uuid) > -1) {
				toast(this.$t('toasts.finishDownload', [this.queueList[uuid].title]), 'done')

				$('#bar_' + uuid).css('width', '100%')

				let resultIcon = $('#download_' + uuid).find('.queue_icon')

				if (this.queueList[uuid].failed == 0) {
					resultIcon.text('done')
				} else {
					let failedButton = $('#download_' + uuid).find('.queue_failed_button')

					resultIcon.addClass('clickable')
					failedButton.addClass('clickable')

					resultIcon.bind('click', { item: this.queueList[uuid] }, this.showErrorsTab)
					failedButton.bind('click', { item: this.queueList[uuid] }, this.showErrorsTab)

					if (this.queueList[uuid].failed >= this.queueList[uuid].size) {
						resultIcon.text('error')
					} else {
						resultIcon.text('warning')
					}
				}

				let index = this.queue.indexOf(uuid)
				if (index > -1) {
					this.queue.splice(index, 1)
					this.queueComplete.push(uuid)
				}

				if (this.queue.length <= 0) {
					toast(this.$t('toasts.allDownloaded'), 'done_all')
				}
			}
		},
		openDownloadsFolder() {
			if (window.clientMode) {
				socket.emit('openDownloadsFolder')
			}
		},
		handleDrag(event) {
			let newWidth = window.innerWidth - event.pageX + 2

			if (newWidth < tabMinWidth) {
				newWidth = tabMinWidth
			} else if (newWidth > tabMaxWidth) {
				newWidth = tabMaxWidth
			}

			this.cachedTabWidth = newWidth
			this.setTabWidth(newWidth)
		},
		startDrag() {
			document.addEventListener('mousemove', this.handleDrag)
		},
		startDownload(uuid) {
			$('#bar_' + uuid)
				.removeClass('indeterminate')
				.addClass('determinate')
		},
		showErrorsTab(clickEvent) {
			this.$root.$emit('showTabErrors', clickEvent.data.item, clickEvent.target)
		}
	}
}
</script>

<style>
</style>
