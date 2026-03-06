import express from 'express'
import request from 'supertest'
import { expect } from 'chai'
import fapiDataInit from '../src/fapi-teams-data-mem.mjs'
import foccaciaDataInit from '../src/foccacia-data-mem.mjs'
import foccaciaServicesInit from '../src/foccacia-services.mjs'
import foccaciaApiInit from '../src/foccacia-web-api.mjs'

// Initialize modules
const fapiData = fapiDataInit()
const foccaciaData = foccaciaDataInit()
const services = foccaciaServicesInit(fapiData, foccaciaData)
const webapi = foccaciaApiInit(services)

// Valid token for alice
const validToken = '6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2'

// Custom auth middleware that works correctly with supertest
const authMiddleware = async (req, res, next) => {
    try {
        let token = null
        const authHeader = req.headers['authorization']
        if (authHeader && authHeader.startsWith('Bearer ')) {
            token = authHeader.substring('Bearer '.length).trim()
        }

        if (!token) {
            return res.status(401).json('Unauthorized - missing or invalid token')
        }

        const user = await services.getUserByToken(token)
        if (!user) {
            return res.status(401).json('Unauthorized - missing or invalid token')
        }

        req.user = user
        next()
    } catch (error) {
        return res.status(500).json('Internal server error')
    }
}

// Setup Express app
const app = express()
app.use(express.json())

// API routes
app.get('/api/competitions', webapi.getCompetitions)
app.get('/api/competitions/:competitionCode/teams', webapi.getTeamsByCompetitionSeason)
app.post('/api/users', webapi.createUser)
app.get('/api/groups', authMiddleware, webapi.listGroups)
app.get('/api/groups/:groupId', authMiddleware, webapi.getGroup)
app.post('/api/groups', authMiddleware, webapi.createGroup)
app.put('/api/groups/:groupId', authMiddleware, webapi.editGroup)
app.delete('/api/groups/:groupId', authMiddleware, webapi.deleteGroup)
app.post('/api/groups/:groupId/players', authMiddleware, webapi.addPlayerToGroup)
app.delete('/api/groups/:groupId/players/:playerId', authMiddleware, webapi.removePlayerFromGroup)

describe('Foccacia Web API Tests', function() {

    describe('GET /api/competitions', function() {
        it('should return all competitions', function() {
            return request(app)
                .get('/api/competitions')
                .set('Accept', 'application/json')
                .expect('Content-Type', /json/)
                .expect(200)
                .then(response => {
                    expect(response.body).to.be.an('array')
                    expect(response.body).to.have.lengthOf.at.least(2)
                    expect(response.body[0]).to.have.property('code')
                    expect(response.body[0]).to.have.property('name')
                })
        })

        it('should return limited competitions when limit is provided', function() {
            return request(app)
                .get('/api/competitions?limit=1')
                .set('Accept', 'application/json')
                .expect(200)
                .then(response => {
                    expect(response.body).to.have.lengthOf(1)
                })
        })
    })

    describe('GET /api/competitions/:competitionCode/teams', function() {
        it('should return teams for valid competition and season', function() {
            return request(app)
                .get('/api/competitions/PPL/teams?season=2024')
                .set('Accept', 'application/json')
                .expect(200)
                .then(response => {
                    expect(response.body).to.be.an('array')
                    expect(response.body).to.have.lengthOf.at.least(2)
                    expect(response.body[0]).to.have.property('teamId')
                    expect(response.body[0]).to.have.property('teamName')
                    expect(response.body[0]).to.have.property('players')
                })
        })

        it('should return 404 for invalid competition', function() {
            return request(app)
                .get('/api/competitions/INVALID/teams?season=2024')
                .set('Accept', 'application/json')
                .expect(404)
        })
    })

    describe('POST /api/users', function() {
        it('should create a new user', function() {
            const username = 'testuser_' + Date.now()
            return request(app)
                .post('/api/users')
                .set('Accept', 'application/json')
                .set('Content-Type', 'application/json')
                .send({ username, password: 'password123' })
                .expect(201)
                .then(response => {
                    expect(response.body).to.have.property('username', username)
                    expect(response.body).to.have.property('token')
                })
        })

        it('should return 400 when username already exists', function() {
            return request(app)
                .post('/api/users')
                .set('Accept', 'application/json')
                .set('Content-Type', 'application/json')
                .send({ username: 'alice', password: 'password123' })
                .expect(409)
        })

        it('should return 400 when password is too short', function() {
            return request(app)
                .post('/api/users')
                .set('Accept', 'application/json')
                .set('Content-Type', 'application/json')
                .send({ username: 'newuser', password: '123' })
                .expect(400)
        })
    })

    describe('Authentication Middleware', function() {
        it('should return 401 when no token is provided', function() {
            return request(app)
                .get('/api/groups')
                .set('Accept', 'application/json')
                .expect(401)
        })

        it('should return 401 when invalid token is provided', function() {
            return request(app)
                .get('/api/groups')
                .set('Accept', 'application/json')
                .set('Authorization', 'Bearer invalid-token')
                .expect(401)
        })

        it('should allow access with valid token', function() {
            return request(app)
                .get('/api/groups')
                .set('Accept', 'application/json')
                .set('Authorization', `Bearer ${validToken}`)
                .expect(200)
        })
    })

    describe('GET /api/groups', function() {
        it('should return all groups for authenticated user', function() {
            return request(app)
                .get('/api/groups')
                .set('Accept', 'application/json')
                .set('Authorization', `Bearer ${validToken}`)
                .expect(200)
                .then(response => {
                    expect(response.body).to.be.an('array')
                    expect(response.body[0]).to.have.property('name')
                    expect(response.body[0]).to.have.property('description')
                    expect(response.body[0]).to.have.property('competition')
                })
        })
    })

    describe('POST /api/groups', function() {
        it('should create a new group', function() {
            const newGroup = {
                name: 'Test Group ' + Date.now(),
                description: 'Test Description',
                competition: 'PPL',
                year: '2024'
            }

            return request(app)
                .post('/api/groups')
                .set('Accept', 'application/json')
                .set('Content-Type', 'application/json')
                .set('Authorization', `Bearer ${validToken}`)
                .send(newGroup)
                .expect(201)
                .then(response => {
                    expect(response.body).to.have.property('name', newGroup.name)
                    expect(response.body).to.have.property('id')
                })
        })

        it('should return 400 when invalid competition is provided', function() {
            return request(app)
                .post('/api/groups')
                .set('Accept', 'application/json')
                .set('Content-Type', 'application/json')
                .set('Authorization', `Bearer ${validToken}`)
                .send({
                    name: 'Test Group',
                    description: 'Test',
                    competition: 'INVALID',
                    year: '2024'
                })
                .expect(404)
        })
    })

    describe('GET /api/groups/:groupId', function() {
        it('should return group details', function() {
            const groupId = 'f5c44c50-cfdc-434c-a113-12d59f94480b' // Alice's existing group

            return request(app)
                .get(`/api/groups/${groupId}`)
                .set('Accept', 'application/json')
                .set('Authorization', `Bearer ${validToken}`)
                .expect(200)
                .then(response => {
                    expect(response.body).to.have.property('id', groupId)
                    expect(response.body).to.have.property('name')
                    expect(response.body).to.have.property('players')
                })
        })

        it('should return 404 for non-existent group', function() {
            return request(app)
                .get('/api/groups/non-existent-id')
                .set('Accept', 'application/json')
                .set('Authorization', `Bearer ${validToken}`)
                .expect(404)
        })
    })

    describe('PUT /api/groups/:groupId', function() {
        it('should update group name', async function() {
            // Create a group first
            const newGroup = {
                name: 'Original Name',
                description: 'Test',
                competition: 'PPL',
                year: '2024'
            }

            const createResponse = await request(app)
                .post('/api/groups')
                .set('Authorization', `Bearer ${validToken}`)
                .send(newGroup)

            const groupId = createResponse.body.id

            // Update the group
            return request(app)
                .put(`/api/groups/${groupId}`)
                .set('Accept', 'application/json')
                .set('Content-Type', 'application/json')
                .set('Authorization', `Bearer ${validToken}`)
                .send({ name: 'Updated Name' })
                .expect(200)
                .then(response => {
                    expect(response.body).to.have.property('name', 'Updated Name')
                })
        })
    })

    describe('POST /api/groups/:groupId/players', function() {
        it('should add player to group', async function() {
            // Create a group first
            const newGroup = {
                name: 'Team for Players',
                description: 'Test',
                competition: 'PPL',
                year: '2024'
            }

            const createResponse = await request(app)
                .post('/api/groups')
                .set('Authorization', `Bearer ${validToken}`)
                .send(newGroup)

            const groupId = createResponse.body.id

            // Add a player
            const player = {
                playerId: '72783',
                teamId: 'RIO'
            }

            return request(app)
                .post(`/api/groups/${groupId}/players`)
                .set('Accept', 'application/json')
                .set('Content-Type', 'application/json')
                .set('Authorization', `Bearer ${validToken}`)
                .send(player)
                .expect(201)
                .then(response => {
                    expect(response.body).to.have.property('playerId', '72783')
                })
        })

        it('should return 400 when adding duplicate player', async function() {
            // Create a group
            const newGroup = {
                name: 'Duplicate Test',
                description: 'Test',
                competition: 'PPL',
                year: '2024'
            }

            const createResponse = await request(app)
                .post('/api/groups')
                .set('Authorization', `Bearer ${validToken}`)
                .send(newGroup)

            const groupId = createResponse.body.id
            const player = { playerId: '72783', teamId: 'RIO' }

            // Add player first time
            await request(app)
                .post(`/api/groups/${groupId}/players`)
                .set('Authorization', `Bearer ${validToken}`)
                .send(player)

            // Try to add same player again
            return request(app)
                .post(`/api/groups/${groupId}/players`)
                .set('Authorization', `Bearer ${validToken}`)
                .send(player)
                .expect(409)
        })
    })

    describe('DELETE /api/groups/:groupId/players/:playerId', function() {
        it('should remove player from group', async function() {
            // Create a group and add a player
            const newGroup = {
                name: 'Remove Player Test',
                description: 'Test',
                competition: 'PPL',
                year: '2024'
            }

            const createResponse = await request(app)
                .post('/api/groups')
                .set('Authorization', `Bearer ${validToken}`)
                .send(newGroup)

            const groupId = createResponse.body.id

            // Add a player
            await request(app)
                .post(`/api/groups/${groupId}/players`)
                .set('Authorization', `Bearer ${validToken}`)
                .send({ playerId: '72783', teamId: 'RIO' })

            // Remove the player
            return request(app)
                .delete(`/api/groups/${groupId}/players/72783`)
                .set('Authorization', `Bearer ${validToken}`)
                .expect(204)
        })
    })

    describe('DELETE /api/groups/:groupId', function() {
        it('should delete group', async function() {
            // Create a group first
            const newGroup = {
                name: 'Group to Delete',
                description: 'Test',
                competition: 'PPL',
                year: '2024'
            }

            const createResponse = await request(app)
                .post('/api/groups')
                .set('Authorization', `Bearer ${validToken}`)
                .send(newGroup)

            const groupId = createResponse.body.id

            // Delete the group
            return request(app)
                .delete(`/api/groups/${groupId}`)
                .set('Authorization', `Bearer ${validToken}`)
                .expect(204)
        })

        it('should return 404 when deleting non-existent group', function() {
            return request(app)
                .delete('/api/groups/non-existent-id')
                .set('Authorization', `Bearer ${validToken}`)
                .expect(404)
        })
    })
})
