import {getErrorResponse} from './foccacia-error-map.mjs'

function renderError(resp, error, view = 'error', viewData = {}) {
    const errorResponse = (error && error.code) ? getErrorResponse(error) : {message: error?.message || 'Unexpected error'}
    resp.render(view, {...viewData, error: errorResponse.message})
}

function asyncHandling(handler, errorView = 'error', buildErrorModel) {
    return async (req, resp) => {
        try {
            return await handler(req, resp)
        } catch (error) {
            let viewData = {}
            if (buildErrorModel) {
                try {
                    viewData = await buildErrorModel(req)
                } catch (contextErr) {
                    console.error('Failed to build error view data', contextErr)
                }
            }
            renderError(resp, error, errorView, viewData)
        }
    }
}

export default function init(foccaciaServices) {
    if (!foccaciaServices) {
        throw new Error('Foccacia services module is required');
    }

    // Middleware to ensure user is authenticated
    function ensureAuthenticated(req, resp, next) {
        if (req.isAuthenticated()) {
            return next();
        }
        resp.redirect('/users/accounts');
    }

    async function buildUserContext(req) {
        if (req.user) {
            return { username: req.user.username }
        }
        return {}
    }

    return {
        Home,
        ensureAuthenticated,
        getCompetitions: asyncHandling(getCompetitions, 'competitions', buildUserContext),
        getTeamsByCompetitionSeason: asyncHandling(getTeamsByCompetitionSeason, 'teams', buildUserContext),
        getLoginForm,
        getCreateUserForm,
        registerUser: asyncHandling(registerUser, 'create-user'),
        logoutUser,
        listGroups: asyncHandling(listGroups, 'groups-list', buildUserContext),
        getGroup: asyncHandling(getGroup, 'group-details', buildUserContext),
        getCreateGroupForm: asyncHandling(getCreateGroupForm, 'create-group', buildUserContext),
        createGroup: asyncHandling(createGroup, 'create-group', buildUserContext),
        getEditGroupForm: asyncHandling(getEditGroupForm, 'edit-group', buildUserContext),
        editGroup: asyncHandling(editGroup, 'edit-group', buildUserContext),
        deleteGroup: asyncHandling(deleteGroup, 'groups-list', buildUserContext),
        getAddPlayerForm: asyncHandling(getAddPlayerForm, 'add-player', buildUserContext),
        addPlayerToGroup: asyncHandling(addPlayerToGroup, 'add-player', buildUserContext),
        removePlayerFromGroup: asyncHandling(removePlayerFromGroup, 'groups-list', buildUserContext)
    }

    async function Home(req, resp) {
        const userContext = await buildUserContext(req)
        resp.render('home', userContext)
    }

    async function getCompetitions(req, resp) {
        const limit = req.query.limit
        const competitions = await foccaciaServices.getCompetitions(limit)
        const userContext = await buildUserContext(req)
        resp.render('competitions', {competitions, ...userContext})
    }

    async function getTeamsByCompetitionSeason(req, resp) {
        const {competitionCode} = req.params
        const {season} = req.query

        if (!season) {
            return renderError(resp, {message: 'Season parameter is required'})
        }

        const teams = await foccaciaServices.getTeamsByCompetitionSeason(competitionCode, season)

        if (!teams) {
            return renderError(resp, {message: 'No teams found for this competition and season'})
        }

        const userContext = await buildUserContext(req)
        resp.render('teams', {
            teams, competitionCode, season, ...userContext
        })
    }

    async function getLoginForm(req, resp) {
        const error = req.query.error || null
        resp.render('login', { error })
    }

    async function getCreateUserForm(req, resp) {
        resp.render('create-user')
    }

    async function registerUser(req, resp) {
        const { username, password, confirmPassword } = req.body;

        if (!username || !password || !confirmPassword) {
            return renderError(resp, { message: 'All fields are required' }, 'create-user', { formData: req.body });
        }

        if (password !== confirmPassword) {
            return renderError(resp, { message: 'Passwords do not match' }, 'create-user', { formData: req.body });
        }

        const newUser = await foccaciaServices.createUser(username.trim(), password);
        resp.render('user-created', { user: newUser });
    }

    function logoutUser(req, resp) {
        req.logout((err) => {
            if (err) {
                return resp.redirect('/');
            }
            resp.redirect('/');
        });
    }

    async function listGroups(req, resp) {
        const token = req.user.token;
        const groups = await foccaciaServices.getUserGroups(token);

        resp.render('groups-list', {
            groups,
            username: req.user.username
        })
    }

    async function getGroup(req, resp) {
        const {groupId} = req.params;
        const token = req.user.token;
        const group = await foccaciaServices.getUserGroupDetail(token, groupId);

        if (!group) {
            return renderError(resp, {message: 'Group not found'}, 'group-details', {...await buildUserContext(req)});
        }

        resp.render('group-details', {
            group,
            username: req.user.username
        });
    }

    async function getCreateGroupForm(req, resp) {
        const competitions = await foccaciaServices.getCompetitions();

        resp.render('create-group', {
            username: req.user.username,
            competitions
        });
    }

    async function createGroup(req, resp) {
        const token = req.user.token;
        const {name, description, competition, year} = req.body;

        if (!name || !description || !competition || !year) {
            const competitions = await foccaciaServices.getCompetitions();

            return renderError(resp, {message: 'All fields are required'}, 'create-group', {
                ...await buildUserContext(req), competitions, formData: req.body
            })
        }

        try {
            const groupData = {name, description, competition, year};
            const newGroup = await foccaciaServices.createGroupForUser(token, groupData);
            resp.redirect(`/groups/${newGroup.id}`);
        } catch (error) {
            const competitions = await foccaciaServices.getCompetitions();

            return renderError(resp, error, 'create-group', {
                ...await buildUserContext(req), competitions, formData: req.body
            })
        }
    }

    async function getEditGroupForm(req, resp) {
        const {groupId} = req.params;
        const token = req.user.token;
        const group = await foccaciaServices.getUserGroupDetail(token, groupId);

        if (!group) {
            return resp.render('error', {
                error: 'Group not found', backLink: `/groups`
            });
        }

        resp.render('edit-group', {
            group, username: req.user.username
        });
    }

    async function editGroup(req, resp) {
        const {groupId} = req.params;
        const token = req.user.token;
        const {name, description} = req.body;
        const updates = {};

        if (name) updates.name = name;
        if (description) updates.description = description;

        if (Object.keys(updates).length === 0) {
            const group = await foccaciaServices.getUserGroupDetail(token, groupId);

            return renderError(resp, {message: 'At least one field must be updated'}, 'edit-group', {
                ...await buildUserContext(req), group
            })
        }

        await foccaciaServices.updateGroupForUser(token, groupId, updates);
        resp.redirect(`/groups/${groupId}`);
    }

    async function deleteGroup(req, resp) {
        const {groupId} = req.params;
        const token = req.user.token;
        await foccaciaServices.deleteGroupForUser(token, groupId);
        resp.redirect(`/groups`);
    }

    async function getAddPlayerForm(req, resp) {
        const {groupId} = req.params;
        const token = req.user.token;
        const group = await foccaciaServices.getUserGroupDetail(token, groupId);

        if (!group) {
            return resp.render('error', {
                error: 'Group not found', backLink: `/groups`
            });
        }

        // Get available teams for this group's competition and year
        const teams = await foccaciaServices.getTeamsByCompetitionSeason(group.competition, group.year);

        // Flatten players from all teams and exclude already selected players
        const availablePlayers = extractAvailablePlayers(teams, group.players);
        const selectedPlayerCount = group.players.length;

        resp.render('add-player', {
            group, availablePlayers, selectedPlayerCount, username: req.user.username
        });
    }

    async function addPlayerToGroup(req, resp) {
        try {
            const {groupId} = req.params;
            const token = req.user.token;
            let group = await foccaciaServices.getUserGroupDetail(token, groupId);

            if (!group || group.code) {
                return renderError(resp, group, 'add-player', {...await buildUserContext(req)});
            }

            let teams;
            try {
                teams = await foccaciaServices.getTeamsByCompetitionSeason(group.competition, group.year);
            } catch (err) {
                teams = null;
            }

            // Handle the case where no players are selected
            if (!req.body.players) {
                return renderError(resp, {message: 'Please select at least one player to add'}, 'add-player', {
                    ...await buildUserContext(req),
                    group,
                    availablePlayers: teams ? extractAvailablePlayers(teams, group.players) : [],
                    selectedPlayerCount: group.players.length,
                })
            }

            // Convert single player to array for uniform handling
            const playerSelections = Array.isArray(req.body.players) ? req.body.players : [req.body.players];

            // Validate that we won't exceed 11 players
            const totalWillBe = group.players.length + playerSelections.length;
            if (totalWillBe > 11) {
                return renderError(resp, {message: `Cannot add ${playerSelections.length} players. You have ${group.players.length}/11 and can only add ${11 - group.players.length} more.`}, 'add-player', {
                    ...await buildUserContext(req),
                    group,
                    availablePlayers: teams ? extractAvailablePlayers(teams, group.players) : [],
                    selectedPlayerCount: group.players.length,
                })
            }

            // Validate all selections before processing any
            const validSelections = [];
            for (const selection of playerSelections) {
                const [playerId, teamId] = selection.split('|');
                if (!playerId || !teamId) {
                    return renderError(resp, {message: 'Invalid player selection'}, 'add-player', {
                        ...await buildUserContext(req),
                        group,
                        availablePlayers: teams ? extractAvailablePlayers(teams, group.players) : [],
                        selectedPlayerCount: group.players.length,
                    })
                }
                validSelections.push({playerId, teamId});
            }

            // Process each selected player
            let errorMsg = null;
            let currentGroup = group;
            const addedPlayerIds = new Set();

            for (const playerData of validSelections) {
                // Check if player was already added in this batch
                if (addedPlayerIds.has(playerData.playerId)) {
                    continue;
                }

                // Check if player already in current group (may have been updated by previous iteration)
                if (currentGroup.players.find(p => p.playerId === playerData.playerId)) {
                    continue; // Skip this player, don't error out
                }

                // Service call will return error object on error
                try {
                    const result = await foccaciaServices.addPlayerToGroup(token, groupId, playerData, teams);
                    if (result && result.code) {
                        errorMsg = (result && result.error) || 'Failed to add player';
                        break;
                    }
                    addedPlayerIds.add(playerData.playerId);
                } catch (serviceErr) {
                    errorMsg = (serviceErr && serviceErr.error) || (serviceErr && serviceErr.message) || 'Failed to add player';
                    break;
                }

                // Refresh group to get updated player list
                try {
                    let refreshed = await foccaciaServices.getUserGroupDetail(token, groupId);
                    if (refreshed && refreshed.code) {
                        errorMsg = (refreshed && refreshed.error) || 'Failed to refresh group';
                        break;
                    }
                    currentGroup = refreshed;
                } catch (refreshErr) {
                    errorMsg = (refreshErr && refreshErr.error) || (refreshErr && refreshErr.message) || 'Failed to refresh group';
                    break;
                }
            }

            if (errorMsg) {
                // Use the teams we already fetched - no additional API call
                return renderError(resp, {message: errorMsg}, 'add-player', {
                    ...await buildUserContext(req),
                    group: currentGroup,
                    availablePlayers: teams ? extractAvailablePlayers(teams, currentGroup.players) : [],
                    selectedPlayerCount: currentGroup.players.length,
                })
            }

            resp.redirect(`/groups/${groupId}`);
        } catch (err) {
            // Catch any unexpected errors to prevent server crash
            console.error('Unexpected error in addPlayerToGroup:', err);
            resp.status(500).render('error', {
                error: 'An unexpected error occurred while adding players', title: 'Error'
            });
        }
    }

    function extractAvailablePlayers(teams, selectedPlayers) {
        const selectedPlayerIds = new Set(selectedPlayers.map(p => p.playerId));
        const availablePlayers = [];

        if (teams) {
            teams.forEach(team => {
                if (team.players) {
                    team.players.forEach(player => {
                        if (!selectedPlayerIds.has(player.playerId)) {
                            availablePlayers.push({
                                playerId: player.playerId,
                                playerName: player.playerName,
                                position: player.position,
                                teamCode: team.teamId,
                                teamName: team.teamName,
                                nationality: player.nationality || 'Unknown',
                                age: player.age || 'N/A'
                            });
                        }
                    });
                }
            });
        }

        return availablePlayers;
    }

    async function removePlayerFromGroup(req, resp) {
        const {groupId} = req.params;
        const {playerId} = req.body;
        const token = req.user.token;

        if (!playerId) {
            return resp.redirect(`/groups/${groupId}`);
        }

        await foccaciaServices.removePlayerFromGroup(token, groupId, playerId);
        resp.redirect(`/groups/${groupId}`);
    }
}