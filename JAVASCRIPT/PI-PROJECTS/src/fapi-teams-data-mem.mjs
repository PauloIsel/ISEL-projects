const data = {
    competitions: [
        {code: "PPL", name: "Primeira Liga"},
        {code: "PL", name: "Premier League"}
    ],

    teams: {
        "PPL": {
            "2024": [
                {
                    "teamId": "RIO",
                    "teamName": "Rio Ave FC",
                    "country": "Portugal",
                    "players": [
                        {
                            "playerId": "72783",
                            "playerName": "Matheus Teixeira",
                            "position": "Goalkeeper"
                        },
                        {
                            "playerId": "105208",
                            "playerName": "Kiko Bondoso",
                            "position": "Offence"
                        },
                        {
                            "playerId": "263610",
                            "playerName": "Antzelo Sina",
                            "position": "Goalkeeper"
                        },
                        {
                            "playerId": "4266",
                            "playerName": "Omar Richards",
                            "position": "Defence"
                        },
                        {
                            "playerId": "98870",
                            "playerName": "Jonathan Panzo",
                            "position": "Defence"
                        },
                        {
                            "playerId": "193588",
                            "playerName": "Joao Tome",
                            "position": "Defence"
                        },
                        {
                            "playerId": "193589",
                            "playerName": "Player Seven",
                            "position": "Midfielder"
                        },
                        {
                            "playerId": "193590",
                            "playerName": "Player Eight",
                            "position": "Midfielder"
                        },
                        {
                            "playerId": "193591",
                            "playerName": "Player Nine",
                            "position": "Midfielder"
                        },
                        {
                            "playerId": "193592",
                            "playerName": "Player Ten",
                            "position": "Forward"
                        },
                        {
                            "playerId": "193593",
                            "playerName": "Player Eleven",
                            "position": "Forward"
                        },
                        {
                            "playerId": "193594",
                            "playerName": "Player Twelve",
                            "position": "Forward"
                        }
                    ]
                },
                {
                    "teamId": "SPO",
                    "teamName": "Sporting Clube de Portugal",
                    "country": "Portugal",
                    "players": [
                        {
                            "playerId": "32014",
                            "playerName": "Rui Silva",
                            "position": "Goalkeeper"
                        },
                        {
                            "playerId": "97893",
                            "playerName": "Franco Israel",
                            "position": "Goalkeeper"
                        },
                        {
                            "playerId": "178770",
                            "playerName": "Diego Calai",
                            "position": "Goalkeeper"
                        },
                        {
                            "playerId": "194860",
                            "playerName": "Francisco Silva",
                            "position": "Goalkeeper"
                        },
                        {
                            "playerId": "202181",
                            "playerName": "Diogo Pinto",
                            "position": "Goalkeeper"
                        },
                        {
                            "playerId": "160926",
                            "playerName": "Zeno Debast",
                            "position": "Defence"
                        }
                    ]
                },
            ]
        },
        "PL": {
            "2021": [
                {
                    teamId: "MUN",
                    teamName: "Manchester United",
                    country: "England",
                    players: [
                        {playerId: "p20001", playerName: "Player One", position: "Midfielder"}
                    ]
                },
                {
                    teamId: "LIV",
                    teamName: "Liverpool",
                    country: "England",
                    players: [
                        {playerId: "p20002", playerName: "Player Two", position: "Defender"}
                    ]
                }
            ],
            "2022": [
                {teamId: "CHE", teamName: "Chelsea", country: "England", players: []},
                {teamId: "ARS", teamName: "Arsenal", country: "England", players: []}
            ]
        }
    }
}

export default function init() {
    return {
        getCompetitions,
        getTeamsByCompetitionSeason
    }

    function getCompetitions(limit) {
        if (limit === undefined || limit === null) return Promise.resolve(data.competitions)

        const n = Number.parseInt(limit, 10)
        if (Number.isNaN(n)) return Promise.resolve(data.competitions)
        return Promise.resolve(data.competitions.slice(0, n))
    }

    function getTeamsByCompetitionSeason(competitionCode, season) {
        const comp = data.teams[competitionCode]
        if (!comp) return Promise.resolve(null)

        return Promise.resolve(comp[season] || null)
    }
}