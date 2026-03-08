import {expect} from 'chai'
import fapiDataInit from "../src/fapi-teams-data-mem.mjs";
import foccaciaDataInit from "../src/foccacia-data-mem.mjs";
import foccaciaServicesInit from "../src/foccacia-services.mjs";

describe('Foccacia Services Tests', function() {
    const fapiData = fapiDataInit();
    const foccaciaData = foccaciaDataInit();
    const services = foccaciaServicesInit(fapiData, foccaciaData);

   describe('getCompetitions', function() {
        it('should return all competitions when no limit is provided', async function() {
            const result = await services.getCompetitions();
            expect(result).to.be.an('array');
            expect(result).to.have.lengthOf.at.least(2);
            expect(result[0]).to.have.property('code');
            expect(result[0]).to.have.property('name');
        });

        it('should return limited competitions when limit is provided', async function() {
            const result = await services.getCompetitions(1);
            expect(result).to.be.an('array');
            expect(result).to.have.lengthOf(1);
        });

        it('should return all competitions when limit is invalid', async function() {
            const result = await services.getCompetitions('invalid');
            expect(result).to.be.an('array');
            expect(result).to.have.lengthOf.at.least(2);
        });
    });

    describe('getTeamsByCompetitionSeason', function() {
        it('should return teams for valid competition and season', async function() {
            const result = await services.getTeamsByCompetitionSeason('PPL', '2024');
            expect(result).to.be.an('array');
            expect(result).to.have.lengthOf.at.least(2);
            expect(result[0]).to.have.property('teamId');
            expect(result[0]).to.have.property('teamName');
            expect(result[0]).to.have.property('players');
        });

        it('should throw error e1 when competition does not exist', async function() {
            try {
                await services.getTeamsByCompetitionSeason('INVALID', '2024');
                expect.fail('Should have thrown error');
            } catch (error) {
                expect(error).to.have.property('code', 'e1');
                expect(error).to.have.property('error');
                expect(error.error).to.include('Competition');
            }
        });

        it('should throw error e1 when season does not exist', async function() {
            try {
                await services.getTeamsByCompetitionSeason('PPL', '2099');
                expect.fail('Should have thrown error');
            } catch (error) {
                expect(error).to.have.property('code', 'e1');
                expect(error).to.have.property('error');
                expect(error.error).to.include('Competition');
            }
        });
    });

    describe('createUser', function() {
        it('should create a new user successfully', async function() {
            const username = 'testuser_' + Date.now();
            const password = 'password123';
            const result = await services.createUser(username, password);
            expect(result).to.have.property('username', username);
            expect(result).to.have.property('token');
            expect(result.token).to.be.a('string');
        });

        it('should throw error e4 when username already exists', async function() {
            try {
                await services.createUser('alice', 'password123');
                expect.fail('Should have thrown error');
            } catch (error) {
                expect(error).to.have.property('code', 'e4');
                expect(error).to.have.property('error');
                expect(error.error).to.include('Username');
            }
        });
    });

    describe('getUserByToken', function() {
        it('should return user when token is valid', async function() {
            const result = await services.getUserByToken('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2');
            expect(result).to.have.property('username', 'alice');
            expect(result).to.have.property('token', '6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2');
        });

        it('should return undefined when token is invalid', async function() {
            const result = await services.getUserByToken('invalid-token');
            expect(result).to.be.undefined;
        });
    });

    describe('getUserGroups', function() {
        it('should return groups for valid token', async function() {
            const result = await services.getUserGroups('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2');
            expect(result).to.be.an('array');
            expect(result).to.have.lengthOf(1);
            expect(result[0]).to.have.property('id');
            expect(result[0]).to.have.property('name');
            expect(result[0]).to.not.have.property('players'); // players should not be in list view
        });

        it('should return empty array for token with no groups', async function() {
            const result = await services.getUserGroups('non-existent-token');
            expect(result).to.be.an('array');
            expect(result).to.have.lengthOf(0);
        });
    });

    describe('getUserGroupDetail', function() {
        it('should return group details for valid token and groupId', async function() {
            const result = await services.getUserGroupDetail('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', 'f5c44c50-cfdc-434c-a113-12d59f94480b');
            expect(result).to.have.property('id', 'f5c44c50-cfdc-434c-a113-12d59f94480b');
            expect(result).to.have.property('name');
            expect(result).to.have.property('players');
            expect(result.players).to.be.an('array');
        });

        it('should throw error e1 when group does not exist', async function() {
            try {
                await services.getUserGroupDetail('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', 'invalid-group-id');
                expect.fail('Should have thrown error');
            } catch (error) {
                expect(error).to.have.property('code', 'e1');
                expect(error).to.have.property('error');
                expect(error.error).to.include('Group');
            }
        });
    });

    describe('createGroupForUser', function() {
        it('should create a new group successfully', async function() {
            const newGroup = {
                name: 'Test Group',
                description: 'Test Description',
                competition: 'PPL',
                year: '2024'
            };
            const result = await services.createGroupForUser('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', newGroup);
            expect(result).to.have.property('id');
            expect(result).to.have.property('name', 'Test Group');
            expect(result).to.have.property('players');
            expect(result.players).to.be.an('array').with.lengthOf(0);
        });
    });

    describe('updateGroupForUser', function() {
        it('should update group successfully', async function() {
            const updates = {
                name: 'Updated Group Name',
                description: 'Updated Description'
            };
            const result = await services.updateGroupForUser('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', 'f5c44c50-cfdc-434c-a113-12d59f94480b', updates);
            expect(result).to.have.property('name', 'Updated Group Name');
            expect(result).to.have.property('description', 'Updated Description');
        });

        it('should throw error e1 when group does not exist', async function() {
            try {
                await services.updateGroupForUser('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', 'invalid-group-id', {name: 'Test'});
                expect.fail('Should have thrown error');
            } catch (error) {
                expect(error).to.have.property('code', 'e1');
            }
        });

        it('should update only name when only name is provided', async function() {
            const newGroup = {
                name: 'Original Name',
                description: 'Original Description',
                competition: 'PPL',
                year: '2024'
            };
            const created = await services.createGroupForUser('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', newGroup);
            
            const result = await services.updateGroupForUser('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', created.id, {name: 'Updated Name'});
            expect(result).to.have.property('name', 'Updated Name');
            expect(result).to.have.property('description', 'Original Description');
        });

        it('should update only description when only description is provided', async function() {
            const newGroup = {
                name: 'Original Name',
                description: 'Original Description',
                competition: 'PPL',
                year: '2024'
            };
            const created = await services.createGroupForUser('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', newGroup);
            
            const result = await services.updateGroupForUser('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', created.id, {description: 'Updated Description'});
            expect(result).to.have.property('name', 'Original Name');
            expect(result).to.have.property('description', 'Updated Description');
        });
    });

    describe('deleteGroupForUser', function() {
        it('should delete group successfully', async function() {
            const newGroup = {
                name: 'Group to Delete',
                description: 'Will be deleted',
                competition: 'PPL',
                year: '2024'
            };
            const created = await services.createGroupForUser('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', newGroup);
            
            const result = await services.deleteGroupForUser('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', created.id);
            expect(result).to.be.undefined;
            
            try {
                await services.getUserGroupDetail('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', created.id);
                expect.fail('Should have thrown error');
            } catch (error) {
                expect(error).to.have.property('code', 'e1');
            }
        });

        it('should throw error e1 when group does not exist', async function() {
            try {
                await services.deleteGroupForUser('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', 'invalid-group-id');
                expect.fail('Should have thrown error');
            } catch (error) {
                expect(error).to.have.property('code', 'e1');
            }
        });
    });

    describe('addPlayerToGroup', function() {
        it('should add player to group successfully', async function() {
            const newGroup = {
                name: 'Add Player Test Group',
                description: 'Test adding players',
                competition: 'PPL',
                year: '2024'
            };
            const group = await services.createGroupForUser('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', newGroup);
            
            const player = {
                playerId: '105208',
                teamId: 'RIO'
            };
            const result = await services.addPlayerToGroup('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', group.id, player);
            expect(result).to.have.property('playerId', '105208');
            expect(result).to.have.property('playerName');
            expect(result).to.have.property('position');
        });

        it('should throw error e1 when group does not exist', async function() {
            const player = {playerId: '105208', teamId: 'RIO'};
            try {
                await services.addPlayerToGroup('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', 'invalid-group-id', player);
                expect.fail('Should have thrown error');
            } catch (error) {
                expect(error).to.have.property('code', 'e1');
            }
        });

        it('should throw error e4 when player already exists in group', async function() {
            // Create a fresh group for this test
            const newGroup = {
                name: 'Duplicate Player Test Group',
                description: 'Test duplicate players',
                competition: 'PPL',
                year: '2024'
            };
            const group = await services.createGroupForUser('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', newGroup);
            
            const player = {playerId: '105208', teamId: 'RIO'};
        
            await services.addPlayerToGroup('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', group.id, player);
            
            try {
                await services.addPlayerToGroup('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', group.id, player);
                expect.fail('Should have thrown error');
            } catch (error) {
                expect(error).to.have.property('code', 'e4');
            }
        });

        it('should throw error e3 when group already has 11 players', async function() {
            const newGroup = {
                name: 'Full Team Test',
                description: 'Test max players',
                competition: 'PPL',
                year: '2024'
            };
            const group = await services.createGroupForUser('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', newGroup);
            
            // Add 11 players to the group
            const playerIds = ['72783', '105208', '263610', '4266', '98870', '193588', '193589', '193590', '193591', '193592', '193593'];
            for (const playerId of playerIds) {
                await services.addPlayerToGroup('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', group.id, {playerId, teamId: 'RIO'});
            }
            
            // Try to add a 12th player
            const player12 = {playerId: '193594', teamId: 'RIO'};
            try {
                await services.addPlayerToGroup('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', group.id, player12);
                expect.fail('Should have thrown error');
            } catch (error) {
                expect(error).to.have.property('code', 'e3');
            }
        });

        it('should throw error e3 when team does not exist in competition', async function() {
            // Create a fresh group for this test
            const newGroup = {
                name: 'Invalid Team Test Group',
                description: 'Test invalid team',
                competition: 'PPL',
                year: '2024'
            };
            const group = await services.createGroupForUser('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', newGroup);
            
            const player = {playerId: '105208', teamId: 'INVALID'};
            try {
                await services.addPlayerToGroup('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', group.id, player);
                expect.fail('Should have thrown error');
            } catch (error) {
                expect(error).to.have.property('code', 'e3');
            }
        });

        it('should throw error e3 when player does not exist in team', async function() {
            // Create a fresh group for this test
            const newGroup = {
                name: 'Invalid Player Test Group',
                description: 'Test invalid player',
                competition: 'PPL',
                year: '2024'
            };
            const group = await services.createGroupForUser('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', newGroup);
            
            const player = {playerId: 'p99999', teamId: 'RIO'};
            try {
                await services.addPlayerToGroup('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', group.id, player);
                expect.fail('Should have thrown error');
            } catch (error) {
                expect(error).to.have.property('code', 'e3');
            }
        });
    });

    describe('removePlayerFromGroup', function() {
        it('should remove player from group successfully', async function() {
            const newGroup = {
                name: 'Remove Player Test Group',
                description: 'Test removing players',
                competition: 'PPL',
                year: '2024'
            };
            const group = await services.createGroupForUser('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', newGroup);
    
            const player = {playerId: '105208', teamId: 'RIO'};
            await services.addPlayerToGroup('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', group.id, player);
            
            const result = await services.removePlayerFromGroup('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', group.id, '105208');
            expect(result).to.be.undefined;
        });

        it('should throw error e1 when group does not exist', async function() {
            try {
                await services.removePlayerFromGroup('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', 'invalid-group-id', '105208');
                expect.fail('Should have thrown error');
            } catch (error) {
                expect(error).to.have.property('code', 'e1');
            }
        });

        it('should throw error e1 when player does not exist in group', async function() {
            // Create a fresh group for this test
            const newGroup = {
                name: 'Remove Player Test',
                description: 'Test removing non-existent player',
                competition: 'PPL',
                year: '2024'
            };
            const group = await services.createGroupForUser('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', newGroup);
            
            try {
                await services.removePlayerFromGroup('6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2', group.id, 'p99999');
                expect.fail('Should have thrown error');
            } catch (error) {
                expect(error).to.have.property('code', 'e1');
            }
        });
    });
})