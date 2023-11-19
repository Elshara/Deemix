export function standardizeData(rawObj, formatFunc) {
	if (!rawObj.hasLoaded) {
		return null
	} else {
		const { data: rawData } = rawObj
		const formattedData = []

		for (const dataElement of rawData) {
			let formatted = formatFunc(dataElement)

			formattedData.push(formatted)
		}

		return {
			data: formattedData,
			hasLoaded: rawObj.hasLoaded
		}
	}
}
