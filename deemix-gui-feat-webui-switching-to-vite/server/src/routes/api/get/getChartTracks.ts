import { RequestHandler } from 'express'
// @ts-expect-error
import { Deezer } from 'deezer-js'
import { ApiHandler } from '../../../types'
import { sessionDZ } from '../../../main'

import { isObjectEmpy } from '../../../helpers/primitive-checks'
import { BadRequestError, isBadRequestError, consoleError } from '../../../helpers/errors'

export interface RawChartTracksQuery {
	id: string
	index?: number
	limit?: number
}

const path: ApiHandler['path'] = '/getChartTracks'

const handler: RequestHandler<{}, {}, {}, RawChartTracksQuery> = async (req, res, next) => {
	try {
		if (!sessionDZ[req.session.id]) sessionDZ[req.session.id] = new Deezer()
		const dz = sessionDZ[req.session.id]

		if (isObjectEmpy(req.query) || !req.query.id) {
			throw new BadRequestError()
		}

		const playlistId = req.query.id
		const index = req.query.index
		const limit = req.query.limit

		const response = await dz.api.get_playlist_tracks(playlistId, { index, limit })
		res.status(200).send(response)
		next()
	} catch (error) {
		if (isBadRequestError(error)) {
			consoleError(error.message)
			res.status(400).send()
			return next()
		}
	}
}

const apiHandler: ApiHandler = { path, handler }

export default apiHandler
