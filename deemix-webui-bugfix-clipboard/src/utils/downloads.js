import { socket } from '@/utils/socket'

function sendAddToQueue(url, bitrate = null) {
	if (!url) return

	socket.emit('addToQueue', { url, bitrate }, () => {})
}

export default {
	sendAddToQueue
}
