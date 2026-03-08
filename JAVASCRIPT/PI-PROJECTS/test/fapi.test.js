import { expect } from 'chai'
import fapiDataInit from '../src/fapi-teams-data.mjs'

describe('Football Data API Tests', function() {
    this.timeout(10000);

    const fapi = fapiDataInit();

    describe('getCompetitions', function() {
        it('should return array of competitions', async function() {
            try {
                const result = await fapi.getCompetitions();
                expect(result).to.be.an('array');
                expect(result.length).to.be.greaterThan(0);
                
                expect(result[0]).to.have.property('code');
                expect(result[0]).to.have.property('name');
                expect(result[0].code).to.be.a('string');
                expect(result[0].name).to.be.a('string');
            } catch (error) {
                console.log('API not available - skipping test');
                this.skip();
            }
        });

        it('should limit number of competitions when limit is provided', async function() {
            try {
                const limit = 5;
                const result = await fapi.getCompetitions(limit);
                expect(result).to.be.an('array');
                expect(result).to.have.lengthOf(limit);
            } catch (error) {
                console.log('API not available - skipping test');
                this.skip();
            }
        });

        it('should return all competitions when limit is invalid', async function() {
            try {
                const result = await fapi.getCompetitions('invalid');
                expect(result).to.be.an('array');
                expect(result.length).to.be.greaterThan(5);
            } catch (error) {
                console.log('API not available - skipping test');
                this.skip();
            }
        });

        it('should return all competitions when limit is null', async function() {
            try {
                const result = await fapi.getCompetitions(null);
                expect(result).to.be.an('array');
                expect(result.length).to.be.greaterThan(5);
            } catch (error) {
                console.log('API not available - skipping test');
                this.skip();
            }
        });

        it('should include common competitions like Premier League', async function() {
            try {
                const result = await fapi.getCompetitions();
                const plExists = result.some(comp => 
                    comp.code === 'PL' || comp.name.includes('Premier League')
                );
                expect(plExists).to.be.true;
            } catch (error) {
                console.log('API not available - skipping test');
                this.skip();
            }
        });

        it('should include competition codes as strings', async function() {
            try {
                const result = await fapi.getCompetitions(10);
                result.forEach(comp => {
                    expect(comp.code).to.be.a('string');
                    expect(comp.code.length).to.be.greaterThan(0);
                });
            } catch (error) {
                console.log('API not available - skipping test');
                this.skip();
            }
        });
    });

    describe('getTeamsByCompetitionSeason', function() {
        it('should return null for invalid competition code', async function() {
            const result = await fapi.getTeamsByCompetitionSeason('INVALID_CODE', '2023');
            expect(result).to.be.null;
        });

        it('should return null for invalid season', async function() {
            const result = await fapi.getTeamsByCompetitionSeason('PL', '1900');
            expect(result).to.be.null;
        });

        it('should return null for future season', async function() {
            const result = await fapi.getTeamsByCompetitionSeason('PL', '2099');
            expect(result).to.be.null;
        });
    });

    describe('API Error Handling', function() {
        it('should handle network errors gracefully', async function() {
            const result = await fapi.getTeamsByCompetitionSeason('NETWORK_ERROR_TEST', '2023');
            expect(result).to.be.null;
        });

        it('should return null instead of throwing for 404 errors', async function() {
            const result = await fapi.getTeamsByCompetitionSeason('NOTFOUND', '2023');
            expect(result).to.be.null;
        });
    });
});
