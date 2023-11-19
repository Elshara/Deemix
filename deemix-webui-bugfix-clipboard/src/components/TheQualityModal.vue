<template>
	<div id="modal_quality" class="smallmodal" v-show="open" @click="tryToDownloadTrack($event)" ref="modal">
		<div class="smallmodal-content">
			<button class="quality-button" data-quality-value="9">{{ $t('globals.download', ['FLAC']) }}</button>
			<button class="quality-button" data-quality-value="3">{{ $t('globals.download', ['MP3 320kbps']) }}</button>
			<button class="quality-button" data-quality-value="1">{{ $t('globals.download', ['MP3 128kbps']) }}</button>
			<button class="quality-button" data-quality-value="15">
				{{ $t('globals.download', ['360 Reality Audio [HQ]']) }}
			</button>
			<button class="quality-button" data-quality-value="14">
				{{ $t('globals.download', ['360 Reality Audio [MQ]']) }}
			</button>
			<button class="quality-button" data-quality-value="13">
				{{ $t('globals.download', ['360 Reality Audio [LQ]']) }}
			</button>
		</div>
	</div>
</template>
<style>
.smallmodal {
	position: fixed;
	z-index: 1250;
	left: 0;
	top: 0;
	width: 100%;
	height: 100%;
	overflow: auto;
	background-color: hsla(0, 0%, 0%, 0.4);
	animation-duration: 0.3s;
}

.smallmodal-content {
	background-color: transparent;
	margin: auto;
	width: var(--modal-content-width);
	position: relative;
	top: 50%;
	transform: translateY(-50%);
}

.smallmodal-content button {
	width: 100%;
	margin-bottom: 8px;
}
</style>
<script>
import Downloads from '@/utils/downloads'

export default {
	data: () => ({
		open: false,
		url: ''
	}),
	mounted() {
		this.$root.$on('QualityModal:open', this.openModal)
		this.$refs.modal.addEventListener('webkitAnimationEnd', this.handleAnimationEnd)
	},
	methods: {
		tryToDownloadTrack(event) {
			const { target } = event

			this.$refs.modal.classList.add('animated', 'fadeOut')

			// If true, the click did not happen on a button but outside
			if (!target.matches('.quality-button')) return

			Downloads.sendAddToQueue(this.url, target.dataset.qualityValue)
		},
		openModal(link) {
			this.url = link
			this.open = true
			this.$refs.modal.classList.add('animated', 'fadeIn')
		},
		handleAnimationEnd(event) {
			const { animationName } = event

			this.$refs.modal.classList.remove('animated', animationName)

			if (animationName === 'fadeIn') return

			this.open = false
		}
	}
}
</script>
