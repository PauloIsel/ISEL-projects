import { notFoundError, alreadyExistsError, invalidOperationError } from './foccacia-error.mjs';
import crypto from 'crypto';

// Simple password hashing using crypto (for production, use bcrypt)
function hashPassword(password) {
    return crypto.createHash('sha256').update(password).digest('hex');
}

function verifyPassword(password, hashedPassword) {
    return hashPassword(password) === hashedPassword;
}

export default function init(fapiData, foccaciaData) {
    if (!fapiData) {
        throw new Error('Football API data module is required');
    }

    if (!foccaciaData) {
        throw new Error('Foccacia data module is required');
    }

    return {
        getCompetitions,
        getTeamsByCompetitionSeason,
        createUser,
        authenticateUser,
        getUserByToken,
        getUserGroups,
        getUserGroupDetail,
        createGroupForUser,
        updateGroupForUser,
        deleteGroupForUser,
        addPlayerToGroup,
        removePlayerFromGroup
    }

    async function getCompetitions(limit) {
        return fapiData.getCompetitions(limit);
    }

    async function getTeamsByCompetitionSeason(competitionCode, season) {
        if (!competitionCode || typeof competitionCode !== 'string' || competitionCode.trim() === '') {
            throw invalidOperationError('Invalid competition code')
        }

        if (!season || !/^[0-9]{4}$/.test(season)) {
            throw invalidOperationError('Invalid season format. Expected format: YYYY')
        }

        const teams = await fapiData.getTeamsByCompetitionSeason(competitionCode, season);
        if (!teams) {
            throw notFoundError('Competition or season not found')
        }
        return teams;
    }

    async function createUser(username, password) {
        if (!username || typeof username !== 'string' || username.trim() === '') {
            throw invalidOperationError('Invalid username')
        }

        if (!password || typeof password !== 'string' || password.length < 6) {
            throw invalidOperationError('Password must be at least 6 characters long')
        }

        const existingUser = await foccaciaData.getUserByUsername(username);
        if (existingUser) {
            throw alreadyExistsError('Username already exists')
        }

        const hashedPassword = hashPassword(password);
        const newUser = await foccaciaData.createUser(username.trim(), hashedPassword);

        return {
            username: newUser.username,
            token: newUser.token
        };
    }

    async function authenticateUser(username, password) {
        if (!username || !password) {
            return null;
        }

        const user = await foccaciaData.getUserByUsername(username);
        if (!user) {
            return null;
        }

        if (!verifyPassword(password, user.password)) {
            return null;
        }

        // Return user without password
        const { password: _, ...userWithoutPassword } = user;
        return userWithoutPassword;
    }

    async function getUserByToken(token) {
        return foccaciaData.getUserByToken(token)
    }

    async function getUserGroups(token) {
        return foccaciaData.getUserGroups(token);
    }

    async function getUserGroupDetail(token, groupId) {
        if (!groupId || typeof groupId !== 'string' || groupId.trim() === '') {
            throw invalidOperationError('Invalid group ID')
        }

        const group = await foccaciaData.getUserGroupById(token, groupId);
        if (!group) {
            throw notFoundError('Group not found')
        }

        return group;
    }

    async function createGroupForUser(token, groupData) {
        if (!groupData || typeof groupData !== 'object') {
            throw invalidOperationError('Invalid group data')
        }

        if (!groupData.name || typeof groupData.name !== 'string' || groupData.name.trim() === '') {
            throw invalidOperationError('Invalid group name')
        }

        if (!groupData.description || typeof groupData.description !== 'string' || groupData.description.trim() === '') {
            throw invalidOperationError('Invalid group description')
        }

        if (!groupData.competition || typeof groupData.competition !== 'string' || groupData.competition.trim() === '') {
            throw invalidOperationError('Invalid competition code')
        }

        if (!groupData.year || !/^[0-9]{4}$/.test(String(groupData.year))) {
            throw invalidOperationError('Invalid year format. Expected format: YYYY')
        }

        const teams = await fapiData.getTeamsByCompetitionSeason(groupData.competition, groupData.year);
        if (!teams) {
            throw notFoundError('Competition or season not found')
        }

        return foccaciaData.createGroupForUser(token, groupData);
    }

    async function updateGroupForUser(token, groupId, groupData) {
        if (!groupId || typeof groupId !== 'string' || groupId.trim() === '') {
            throw invalidOperationError('Invalid group ID')
        }

        if (!groupData || typeof groupData !== 'object') {
            throw invalidOperationError('Invalid group data')
        }

        const hasName = groupData.name !== undefined
        const hasDescription = groupData.description !== undefined

        if (!hasName && !hasDescription) {
            throw invalidOperationError('Nothing to update')
        }

        if (hasName && (typeof groupData.name !== 'string' || groupData.name.trim() === '')) {
            throw invalidOperationError('Invalid group name')
        }

        if (hasDescription && (typeof groupData.description !== 'string' || groupData.description.trim() === '')) {
            throw invalidOperationError('Invalid group description')
        }

        const group = await foccaciaData.getUserGroupById(token, groupId);
        if (!group) {
            throw notFoundError('Group not found')
        }

        const newGroupData = {}
        if (hasName) newGroupData.name = groupData.name.trim()
        if (hasDescription) newGroupData.description = groupData.description.trim()

        return foccaciaData.updateGroupForUser(token, groupId, newGroupData);
    }

    async function deleteGroupForUser(token, groupId) {
        if (!groupId || typeof groupId !== 'string' || groupId.trim() === '') {
            throw invalidOperationError('Invalid group ID')
        }

        const group = await foccaciaData.getUserGroupById(token, groupId);
        if (!group) {
            throw notFoundError('Group not found')
        }

        return foccaciaData.deleteGroupForUser(token, groupId);
    }

    async function addPlayerToGroup(token, groupId, playerData, competitionTeams) {
        if (!groupId || typeof groupId !== 'string' || groupId.trim() === '') {
            throw invalidOperationError('Invalid group ID')
        }

        if (!playerData || typeof playerData !== 'object') {
            throw invalidOperationError('Invalid player data')
        }

        if (!playerData.playerId || typeof playerData.playerId !== 'string' || playerData.playerId.trim() === '') {
            throw invalidOperationError('Invalid player ID')
        }

        if (!playerData.teamId || typeof playerData.teamId !== 'string' || playerData.teamId.trim() === '') {
            throw invalidOperationError('Invalid team ID')
        }

        const group = await foccaciaData.getUserGroupById(token, groupId);
        if (!group) {
            throw notFoundError('Group not found')
        }

        if (group.players.length >= 11) {
            throw invalidOperationError('Group already has 11 players')
        }

        if (group.players.find(p => p.playerId === playerData.playerId)) {
            throw alreadyExistsError('Player already in group')
        }

        // Use passed-in teams data if available, otherwise fetch (for backward compatibility)
        let teams = competitionTeams;
        if (!teams) {
            teams = await fapiData.getTeamsByCompetitionSeason(group.competition, group.year);
        }

        if (!teams) {
            throw notFoundError('Competition or season not found')
        }

        const team = teams.find(t => t.teamId === playerData.teamId);
        if (!team) {
            throw invalidOperationError('Team does not belong to the group competition and season')
        }

        const playerToAdd = team.players.find(p => p.playerId === playerData.playerId);
        if (!playerToAdd) {
            throw invalidOperationError('Player does not exist in the specified team')
        }

        const playerWithTeam = {
            ...playerToAdd,
            teamName: team.teamName,
            teamCode: team.teamId || team.code,
        };

        return foccaciaData.addPlayerToGroup(token, groupId, playerWithTeam);
    }

    async function removePlayerFromGroup(token, groupId, playerId) {
        if (!groupId || typeof groupId !== 'string' || groupId.trim() === '') {
            throw invalidOperationError('Invalid group ID')
        }

        if (!playerId || typeof playerId !== 'string' || playerId.trim() === '') {
            throw invalidOperationError('Invalid player ID')
        }

        const group = await foccaciaData.getUserGroupById(token, groupId);
        if (!group) {
            throw notFoundError('Group not found')
        }

        const player = await foccaciaData.getPlayerFromGroup(token, groupId, playerId);
        if (!player) {
            throw notFoundError('Player not found in group')
        }

        return foccaciaData.removePlayerFromGroup(token, groupId, playerId);
    }
}