/**
 * Climbs the DOM until the root is reached, storing every node passed.
 * @param 	{HTMLElement} el
 * @return	{Array}				Contains all the nodes between el and the root
 */
export function generatePath(el) {
	if (!el) {
		throw new Error('No element passed to the generatePath function!')
	}

	let path = [el]

	while ((el = el.parentNode) && el !== document) {
		path.push(el)
	}

	return path
}

export function isValidURL(text) {
	let lowerCaseText = text.toLowerCase()

	if (lowerCaseText.startsWith('http')) {
		if (
			lowerCaseText.indexOf('deezer.com') >= 0 ||
			lowerCaseText.indexOf('deezer.page.link') >= 0 ||
			lowerCaseText.indexOf('open.spotify.com') >= 0 ||
			lowerCaseText.indexOf('link.tospotify.com') >= 0
		) {
			return true
		}
	} else if (lowerCaseText.startsWith('spotify:')) {
		return true
	}
	return false
}

export function convertDuration(duration) {
	// Convert from seconds only to mm:ss format
	let mm, ss
	mm = Math.floor(duration / 60)
	ss = duration - mm * 60
	// Add leading zero if ss < 0
	if (ss < 10) {
		ss = '0' + ss
	}
	return mm + ':' + ss
}

export function convertDurationSeparated(duration) {
	let hh, mm, ss
	mm = Math.floor(duration / 60)
	hh = Math.floor(mm / 60)
	ss = duration - mm * 60
	mm -= hh * 60
	return [hh, mm, ss]
}

export function numberWithDots(x) {
	return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.')
}

// On scroll event, returns currentTarget = null
// Probably on other events too
export function debounce(func, wait, immediate) {
	var timeout
	return function() {
		var context = this
		var args = arguments
		var later = function() {
			timeout = null
			if (!immediate) func.apply(context, args)
		}
		var callNow = immediate && !timeout
		clearTimeout(timeout)
		timeout = setTimeout(later, wait)
		if (callNow) func.apply(context, args)
	}
}

/**
 * Workaround to copy to the clipboard cross-OS by generating a
 * ghost input and copying the passed String
 *
 * @param {string}	text Text to copy
 */
export function copyToClipboard(text) {
	const ghostInput = document.createElement('input')

	document.body.appendChild(ghostInput)
	ghostInput.setAttribute('type', 'text')
	ghostInput.setAttribute('value', text)
	ghostInput.select()
	ghostInput.setSelectionRange(0, 99999)
	document.execCommand('copy')
	ghostInput.remove()
}

/**
 * @param		{object|array}	obj
 * @param		{...any}				props
 * @returns	{any|null}			property requested
 */
export function getPropertyWithFallback(obj, ...props) {
	for (const prop of props) {
		// Example: this.is.an.example
		let hasDotNotation = /\./.test(prop)

		// Searching the properties in the object
		let valueToTest = hasDotNotation
			? prop.split('.').reduce((o, i) => {
					if (o) {
						return o[i]
					}
			  }, obj)
			: obj[prop]

		if ('undefined' !== typeof valueToTest) {
			return valueToTest
		}
	}

	return null
}

export default {
	isValidURL,
	convertDuration,
	convertDurationSeparated,
	numberWithDots,
	debounce
}
