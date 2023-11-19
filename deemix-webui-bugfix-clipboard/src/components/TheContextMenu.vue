<template>
	<div class="context-menu" v-show="menuOpen" ref="contextMenu" :style="{ top: yPos, left: xPos }">
		<button
			class="menu-option"
			v-for="option of sortedOptions"
			:key="option.label"
			v-show="option.show"
			@click.prevent="option.action"
		>
			<span class="menu-option__text">{{ option.label }}</span>
		</button>
	</div>
</template>

<script>
import Downloads from '@/utils/downloads'
import downloadQualities from '@js/qualities'
import { generatePath, isChromium } from '@/utils/utils'

import * as clipboard from 'clipboard-polyfill/text'

export default {
	data() {
		return {
			menuOpen: false,
			xPos: 0,
			yPos: 0,
			deezerHref: '',
			generalHref: '',
			imgSrc: ''
		}
	},
	computed: {
		options() {
			// In the action property:
			// Use arrow functions to keep the Vue instance in 'this'
			// Use normal functions to keep the object instance in 'this'
			const options = {
				cut: {
					label: this.$t('globals.cut'),
					show: true,
					position: 1,
					action: () => {
						document.execCommand('Cut')
					}
				},
				copy: {
					label: this.$t('globals.copy'),
					show: true,
					position: 2,
					action: () => {
						document.execCommand('Copy')
					}
				},
				copyLink: {
					label: this.$t('globals.copyLink'),
					show: false,
					position: 3,
					action: () => {
						if (isChromium) {
							navigator.clipboard.writeText(this.generalHref).catch(err => {
								console.error('Link copying failed', err)
							})
						} else {
							clipboard.writeText(this.generalHref).catch(err => {
								console.error('Link copying failed', err)
							})
						}
					}
				},
				copyImageLink: {
					label: this.$t('globals.copyImageLink'),
					show: false,
					position: 4,
					action: () => {
						if (isChromium) {
							navigator.clipboard.writeText(this.imgSrc).catch(err => {
								console.error('Image copying failed', err)
							})
						} else {
							clipboard.writeText(this.imgSrc).catch(err => {
								console.error('Image copying failed', err)
							})
						}
					}
				},
				copyDeezerLink: {
					label: this.$t('globals.copyDeezerLink'),
					show: false,
					position: 5,
					action: () => {
						if (isChromium) {
							navigator.clipboard.writeText(this.generalHref).catch(err => {
								console.error('Deezer link copying failed', err)
							})
						} else {
							clipboard.writeText(this.generalHref).catch(err => {
								console.error('Deezer link copying failed', err)
							})
						}
					}
				},
				paste: {
					label: this.$t('globals.paste'),
					show: true,
					position: 6,
					action: () => {
						if (isChromium) {
							navigator.clipboard.readText().then(text => {
								document.execCommand('insertText', undefined, text)
							})
						} else {
							clipboard.readText().then(text => {
								document.execCommand('insertText', undefined, text)
							})
						}
					}
				}
			}

			let nextValuePosition = Object.values(options).length + 1

			downloadQualities.forEach((quality, index) => {
				options[quality.objName] = {
					label: `${this.$t('globals.download', [quality.label])}`,
					show: false,
					position: nextValuePosition + index,
					action: this.tryToDownloadTrack.bind(null, quality.value)
				}
			})

			return options
		},
		// This computed property is used for rendering the options in the wanted order
		// while keeping the options computed property an Object to make the properties
		// accessible via property name (es this.options.copyLink)
		sortedOptions() {
			return Object.values(this.options).sort((first, second) => {
				return first.position < second.position ? -1 : 1
			})
		}
	},
	mounted() {
		document.body.addEventListener('contextmenu', this.showMenu)
		document.body.addEventListener('click', this.hideMenu)
	},
	methods: {
		showMenu(contextMenuEvent) {
			contextMenuEvent.preventDefault()

			const { pageX, pageY, target: elementClicked } = contextMenuEvent
			const path = generatePath(elementClicked)

			this.positionMenu(pageX, pageY)

			// Show 'Copy Link' option
			if (elementClicked.matches('a')) {
				this.generalHref = elementClicked.href
				this.options.copyLink.show = true
			}

			// Show 'Copy Image Link' option
			if (elementClicked.matches('img')) {
				this.imgSrc = elementClicked.src
				this.options.copyImageLink.show = true
			}

			let deezerLink = null

			for (let i = 0; i < path.length; i++) {
				if (path[i] == document) break

				if (path[i].matches('[data-link]')) {
					deezerLink = path[i].dataset.link
					break
				}
			}

			// Show 'Copy Deezer Link' option
			if (deezerLink) {
				this.deezerHref = deezerLink
				this.showDeezerOptions()
			}

			this.menuOpen = true
		},
		hideMenu() {
			if (!this.menuOpen) return

			// Finish all operations before closing (may be not necessary)
			this.$nextTick()
				.then(() => {
					this.menuOpen = false

					this.options.copyLink.show = false
					this.options.copyDeezerLink.show = false
					this.options.copyImageLink.show = false

					downloadQualities.forEach(quality => {
						this.options[quality.objName].show = false
					})
				})
				.catch(err => {
					console.error(err)
				})
		},
		positionMenu(newX, newY) {
			this.xPos = `${newX}px`
			this.yPos = `${newY}px`
		},
		showDeezerOptions() {
			this.options.copyDeezerLink.show = true

			downloadQualities.forEach(quality => {
				this.options[quality.objName].show = true
			})
		},
		tryToDownloadTrack(qualityValue) {
			Downloads.sendAddToQueue(this.deezerHref, qualityValue)
		}
	}
}
</script>

<style lang="scss" scoped>
.context-menu {
	position: absolute;
	top: 0;
	left: 0;
	min-width: 100px;
	background: var(--foreground-inverted);
	border-radius: 7px;
	box-shadow: 4px 10px 18px 0px hsla(0, 0%, 0%, 0.15);
	z-index: 10000;
}

.menu-option {
	display: flex;
	align-items: center;
	width: 100%;
	height: 40px;
	padding-left: 10px;
	padding-right: 10px;
	color: var(--foreground);
	cursor: pointer;

	&:first-child {
		border-radius: 7px 7px 0 0;
	}

	&:last-child {
		border-radius: 0 0 7px 7px;
	}

	&:hover {
		background: var(--table-highlight);
		filter: brightness(150%);
	}

	&__text {
		text-transform: capitalize;
	}
}

// Resetting buttons only for this component (because the style is scoped)
button {
	color: var(--accent-text);
	color: unset;
	background-color: var(--accent-color);
	background-color: unset;
	min-width: unset;
	position: unset;
	border: unset;
	border-radius: unset;
	font-family: unset;
	font-weight: unset;
	font-size: unset;
	padding: unset;
	margin-right: unset;
	height: unset;
	text-transform: unset;
	cursor: unset;
	transition: unset;

	&:focus {
		outline: none;
	}

	&[disabled] {
		background-color: unset;
		color: unset;
		opacity: unset;
	}

	&.selective {
		background-color: unset;
		color: unset;

		&.active {
			background-color: unset;
			color: unset;
		}
	}

	&.with_icon {
		display: unset;
		align-items: unset;

		i {
			margin-left: unset;
		}
	}

	&:active {
		background-color: unset;
		transform: unset;
	}

	&:hover {
		background: unset;
		border: unset;
	}
}
</style>