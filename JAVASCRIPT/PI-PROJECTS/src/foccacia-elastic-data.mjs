import { fetchElastic } from './fetch-elastic.mjs';
import { notFound } from './foccacia-error.mjs';
import crypto from 'crypto';

function aUserFromElastic(elasticUser) {
    if (!elasticUser) return null;
    return { id: elasticUser._id, ...elasticUser._source };
}

function aGroupFromElastic(elasticGroup) {
    if (!elasticGroup) return null;
    return { id: elasticGroup._id, ...elasticGroup._source };
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
        const body = {
            "query": {
                "match": {
                    "username": username
                }
            }
        };

        return fetchElastic('POST', '/users/_search', body)
            .then(elasticUsers => {
                const user = elasticUsers.hits.hits[0];
                return aUserFromElastic(user);
            });
    }

    function getUserByToken(token) {
        const body = {
            "query": {
                "match": {
                    "token": token
                }
            }
        };
        return fetchElastic('POST', '/users/_search', body)
            .then(elasticUsers => {
                const user = elasticUsers.hits.hits[0];
                return aUserFromElastic(user);
            });
    }

    function createUser(username, password) {
        const user = {
            username: username,
            password: password,
            token: crypto.randomUUID()
        };
        return fetchElastic('POST', '/users/_doc', user)
            .then(elasticRes => ({id: elasticRes._id, ...user}));
    }

    function getUserGroups(token) {
        return getUserByToken(token)
            .then(user => {
                if (!user) return [];

                const body = {
                    "query": {
                        "match": {
                            "userId": user.id
                        }
                    }
                };
                return fetchElastic('POST', '/groups/_search', body)
                    .then(elasticGroups => elasticGroups.hits.hits.map(aGroupFromElastic));
            });
    }

    function getUserGroupById(userToken, groupId) {
        return getUserByToken(userToken)
            .then(user => {
                if (!user) return null;

                return fetchElastic('GET', `/groups/_doc/${groupId}`)
                    .then(elasticGroup => {
                        if (!elasticGroup.found || elasticGroup._source.userId !== user.id) {
                            return null;
                        }
                        return aGroupFromElastic(elasticGroup);
                    });
            });
    }

    function createGroupForUser(userToken, group) {
        return getUserByToken(userToken)
            .then(user => {
                if (!user) return notFound('User not found');

                const groupToCreate = {...group, userId: user.id, players: []};
                return fetchElastic('POST', '/groups/_doc', groupToCreate)
                    .then(elasticRes => ({id: elasticRes._id, ...groupToCreate}));
            });
    }

    function updateGroupForUser(token, groupId, updates) {
        return getUserGroupById(token, groupId)
            .then(group => {
                if (!group) return notFound('Group not found');

                const updatedGroup = {...group, ...updates};
                delete updatedGroup.id;

                return fetchElastic('PUT', `/groups/_doc/${groupId}`, updatedGroup)
                    .then(() => ({id: groupId, ...updatedGroup}));
            });
    }

    function deleteGroupForUser(token, groupId) {
        return getUserGroupById(token, groupId)
            .then(group => {
                if (!group) return notFound('Group not found');

                return fetchElastic('DELETE', `/groups/_doc/${groupId}`);
            });
    }

    function getPlayerFromGroup(token, groupId, playerId) {
        return getUserGroupById(token, groupId)
            .then(group => group?.players.find(p => p.playerId === playerId));
    }

    function addPlayerToGroup(token, groupId, playerToAdd) {
        return getUserGroupById(token, groupId)
            .then(group => {
                if (!group) return notFound('Group not found');

                group.players.push(playerToAdd);
                return updateGroupForUser(token, groupId, {players: group.players})
                    .then(() => playerToAdd);
            });
    }

    function removePlayerFromGroup(token, groupId, playerId) {
        return getUserGroupById(token, groupId)
            .then(group => {
                if (!group) return notFound('Group not found');

                const index = group.players.findIndex(p => p.playerId === playerId);
                if (index !== -1) {
                    group.players.splice(index, 1);
                    return updateGroupForUser(token, groupId, {players: group.players});
                }
            });
    }
}