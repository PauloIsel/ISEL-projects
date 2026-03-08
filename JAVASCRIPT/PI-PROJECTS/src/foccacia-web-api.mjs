import { getErrorResponse } from './foccacia-error-map.mjs'

function handleError(resp, error) {
    if (error && error.code) {
        const errorResponse = getErrorResponse(error)
        return resp.status(errorResponse.status).json(errorResponse.message)
    }
}

function asyncHandler(fn) {
    return async (req, res, next) => {
        try {
            await fn(req, res, next)
        } catch (error) {
            return handleError(res, error)
        }
    }
}

export default function init(foccaciaServices) {
    if (!foccaciaServices) {
        throw new Error('Foccacia services module is required');
    }

    return {
        getCompetitions: asyncHandler(getCompetitions),
        getTeamsByCompetitionSeason: asyncHandler(getTeamsByCompetitionSeason),
        createUser: asyncHandler(createUser),
        authMiddleware: asyncHandler(authMiddleware),
        listGroups: asyncHandler(listGroups),
        createGroup: asyncHandler(createGroup),
        getGroup: asyncHandler(getGroup),
        editGroup: asyncHandler(editGroup),
        deleteGroup: asyncHandler(deleteGroup),
        addPlayerToGroup: asyncHandler(addPlayerToGroup),
        removePlayerFromGroup: asyncHandler(removePlayerFromGroup)
    }

    async function getCompetitions(req, resp) {
        const competitions = await foccaciaServices.getCompetitions(req.query.limit)
        return resp.status(200).json(competitions)
    }

    async function getTeamsByCompetitionSeason(req, res) {
        const competitionCode = req.params.competitionCode
        const season = req.query.season
        const teams = await foccaciaServices.getTeamsByCompetitionSeason(competitionCode, season)
        return res.status(200).json(teams)
    }

    async function createUser(req, res) {
        const { username, password } = req.body
        const newUser = await foccaciaServices.createUser(username, password)
        return res.status(201).json(newUser)
    }

    async function authMiddleware(req, resp, next) {
        let token = null

        // Check if user is authenticated via session (Passport) and has token
        if (req.isAuthenticated() && req.user?.token) {
            token = req.user.token
        } else {
            // Check for token in Authorization header
            const authHeader = req.headers['authorization']
            if (authHeader && authHeader.startsWith('Bearer ')) {
                token = authHeader.substring('Bearer '.length).trim()
            }
        }

        if (!token) {
            return resp.status(401).json('Unauthorized - missing or invalid token')
        }

        const user = await foccaciaServices.getUserByToken(token)
        if (!user) {
            return resp.status(401).json('Unauthorized - missing or invalid token')
        }

        req.user = user
        next()
    }

    async function listGroups(req, res) {
        const userGroups = await foccaciaServices.getUserGroups(req.user.token)
        return res.status(200).json(userGroups)
    }

    async function getGroup(req, res) {
        const groupId = req.params.groupId
        const group = await foccaciaServices.getUserGroupDetail(req.user.token, groupId)
        return res.status(200).json(group)
    }

    async function createGroup(req, res) {
        const groupData = req.body
        const newGroup = await foccaciaServices.createGroupForUser(req.user.token, groupData)
        return res.status(201).json(newGroup)
    }

    async function editGroup(req, res) {
        const groupId = req.params.groupId
        const groupData = req.body
        const updatedGroup = await foccaciaServices.updateGroupForUser(req.user.token, groupId, groupData)
        return res.status(200).json(updatedGroup)
    }

    async function deleteGroup(req, res) {
        const groupId = req.params.groupId
        await foccaciaServices.deleteGroupForUser(req.user.token, groupId)
        return res.status(204).send()
    }

    async function addPlayerToGroup(req, res) {
        const groupId = req.params.groupId
        const playerData = req.body
        const newPlayer = await foccaciaServices.addPlayerToGroup(req.user.token, groupId, playerData)
        return res.status(201).json(newPlayer)
    }

    async function removePlayerFromGroup(req, res) {
        const groupId = req.params.groupId
        const playerId = req.params.playerId
        await foccaciaServices.removePlayerFromGroup(req.user.token, groupId, playerId)
        return res.status(204).send()
    }
}