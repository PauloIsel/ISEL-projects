import dotenv from 'dotenv';

dotenv.config();

const API_KEY = (typeof process !== 'undefined' && (process.env.FOOTBALL_API_KEY)) || null;

function apiFetch(path) {
    if (!API_KEY) {
        return Promise.reject(new Error('API key not configured'));
    }

    const url = `${process.env.FOOTBALL_API_URL}${path}`;
    return fetch(url, {headers: {'X-Auth-Token': API_KEY}}).then(res => {
        if (!res.ok) {
            return res.text()
                .catch(() => '')
                .then(text => {
                    throw new Error(`API error ${res.status} ${res.statusText}: ${text}`);
                });
        }
        return res.json();
    });
}

export default function init() {
    return {
        getCompetitions,
        getTeamsByCompetitionSeason
    }

    function getCompetitions(limit) {
        if (!API_KEY) return Promise.reject(new Error('API key not configured'));

        return apiFetch('/competitions').then(json => {
            const comps = (json.competitions || []).map(c => ({
                code: c.code || String(c.id),
                name: c.name || c.area?.name || ''
            }));

            if (limit === undefined || limit === null) return comps;
            const n = Number.parseInt(limit, 10);
            if (Number.isNaN(n)) return comps;
            return comps.slice(0, n);
        });
    }

    function getTeamsByCompetitionSeason(competitionCode, season) {
        if (!API_KEY) return Promise.reject(new Error('API key not configured'));

        return apiFetch(`/competitions/${encodeURIComponent(competitionCode)}/teams?season=${encodeURIComponent(season)}`).then(json => {
            const teamsRaw = json.teams || [];

            return teamsRaw.map(t => ({
                teamId: t.tla || String(t.id),
                teamName: t.name || t.shortName || '',
                country: t.area?.name || '',
                players: t.squad ? t.squad.map((p, i) => ({
                    playerId: p.id ? String(p.id) : `${t.tla || String(t.id)}-${i}`,
                    playerName: p.name || 'Unknown',
                    position: p.position || ''
                })) : []
            }));
        }).catch(() => null);
    }
}