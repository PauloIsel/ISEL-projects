import crypto from 'crypto'

const users = [{
    "username": "alice",
    "password": "e0c9035898dd52fc65c41454cec9c4d2611bfb37920d939364e9ea561f4bae0f", // hashed "password123"
    "token": "6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2"
}]
const userGroups = {
    "6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2": [
        {
            "name": "Alice's Best XI",
            "description": "My favorite players from PPL 2024",
            "competition": "PPL",
            "year": "2024",
            "id": "f5c44c50-cfdc-434c-a113-12d59f94480b",
            "players": []
        }
    ]
}

export default function init() {
    return {
        getUserByUsername,
        getUserByToken,
        createUser,
        getUserGroups,
        getUserGroupById,
        createGroupForUser,
        updateGroupForUser,
        deleteGroupForUser,
        getPlayerFromGroup,
        addPlayerToGroup,
        removePlayerFromGroup
    }

    function getUserByUsername(username) {
        const user = users.find(user => user.username === username)
        return Promise.resolve(user)
    }

    function getUserByToken(token) {
        const user = users.find(user => user.token === token)
        return Promise.resolve(user)
    }

    function createUser(username, password) {
        const newUser = {
            username: username,
            password: password,
            token: crypto.randomUUID()
        }

        users.push(newUser);
        return Promise.resolve(newUser)
    }

    function getUserGroups(token) {
        if (!userGroups[token]) return Promise.resolve([])

        return Promise.resolve(userGroups[token].map(g => ({
            id: g.id,
            name: g.name,
            description: g.description,
            competition: g.competition,
            year: g.year
        })))
    }

    function getUserGroupById(userToken, groupId) {
        return Promise.resolve(userGroups[userToken].find(g => g.id === groupId))
    }

    function createGroupForUser(userToken, group) {
        if (!userGroups[userToken]) userGroups[userToken] = []

        group.id = crypto.randomUUID()
        group.players = []
        userGroups[userToken].push(group)

        return Promise.resolve(group)
    }

    function updateGroupForUser(token, groupId, updates) {
        const groups = userGroups[token]
        const group = groups?.find(g => g.id === groupId)

        Object.assign(group, updates)
        return Promise.resolve(group)
    }

    function deleteGroupForUser(token, groupId) {
        const groups = userGroups[token]
        const index = groups?.findIndex(g => g.id === groupId)

        if (index !== -1) {
            groups.splice(index, 1)
        }

        return Promise.resolve()
    }

    function getPlayerFromGroup(token, groupId, playerId) {
        const groups = userGroups[token]
        const group = groups?.find(g => g.id === groupId)

        const player = group.players.find(p => p.playerId === playerId)
        return Promise.resolve(player)
    }

    function addPlayerToGroup(token, groupId, playerToAdd) {
        const groups = userGroups[token]
        const group = groups?.find(g => g.id === groupId)

        group.players.push(playerToAdd)
        return Promise.resolve(playerToAdd)
    }

    function removePlayerFromGroup(token, groupId, playerId) {
        const groups = userGroups[token]
        const group = groups?.find(g => g.id === groupId)

        const index = group.players.findIndex(p => p.playerId === playerId)
        if (index !== -1) {
            group.players.splice(index, 1)
        }

        return Promise.resolve()
    }
}