# FOCCACIA Application - Implementation Report

## Table of Contents
1. [Application Overview](#application-overview)
2. [Application Structure](#application-structure)
3. [Data Storage Design](#data-storage-design)
4. [ElasticSearch to Application Model Mapping](#elasticsearch-to-application-model-mapping)
5. [Server API Documentation](#server-api-documentation)
6. [Installation and Setup Instructions](#installation-and-setup-instructions)
7. [Running Tests](#running-tests)

## Application Overview

FOCCACIA is a web application for managing football team groups. The application allows authenticated users to create, edit, and manage groups of players from different competitions. Each group belongs to a specific user and contains players from teams participating in a particular competition and season.

### Key Features
- User registration and authentication using Passport.js
- Token-based authentication for API access
- Private group management (groups are user-specific)
- Integration with external Football API for competitions and teams
- Support for both web UI and REST API
- Player management within groups (add/remove players)

## Application Structure

The FOCCACIA application follows a layered architecture with clear separation of concerns:

### Server Components

```
┌─────────────────────┐
│   foccacia-server   │  ← Express server setup and routing
└─────────────────────┘
           │
┌─────────────────────┐
│ foccacia-web-ui     │  ← Web UI controllers (Handlebars)
│ foccacia-web-api    │  ← REST API controllers
└─────────────────────┘
           │
┌─────────────────────┐
│ foccacia-services   │  ← Business logic layer
└─────────────────────┘
           │
┌─────────────────────┐
│ foccacia-*-data     │  ← Data access layer
│ fapi-teams-data     │  ← External API integration
└─────────────────────┘
```

### Key Modules

1. **foccacia-server.mjs** - Main entry point, Express server configuration
2. **foccacia-services.mjs** - Business logic and validation
3. **foccacia-web-api.mjs** - REST API endpoints
4. **foccacia-web-ui.mjs** - Web UI controllers for Handlebars views
5. **foccacia-elastic-data.mjs** - ElasticSearch data access
6. **fapi-teams-data.mjs** - External Football API integration
7. **passport-config.mjs** - Passport.js authentication configuration

### Client Components

The client side consists of:
- **Handlebars Templates** (`views/`) - Server-side rendered pages
- **CSS Stylesheets** (`css/`) - Styling for each view
- **Static Assets** - Served directly by Express

## Data Storage Design

### ElasticSearch Indices

The application uses two main ElasticSearch indices:

#### 1. Users Index (`users`)
```json
{
  "username": "string",     // Unique username
  "password": "string",     // SHA256 hashed password
  "token": "string"         // UUID token for API authentication
}
```

#### 2. Groups Index (`groups`)
```json
{
  "userId": "string",       // Reference to user document ID
  "name": "string",         // Group name
  "description": "string",  // Group description
  "competition": "string",  // Competition code (e.g., "EPL", "CL")
  "year": "string",         // Season year (e.g., "2024")
  "players": [              // Array of player objects
    {
      "playerId": "string",
      "playerName": "string",
      "teamId": "string",
      "teamName": "string",
      "teamCode": "string",
      "position": "string",
      "nationality": "string",
      "age": "number"
    }
  ]
}
```

### Data Relationships

- **Users → Groups**: One-to-Many relationship through `userId` field in groups
- **Groups → Players**: Embedded relationship (players are stored within group documents)
- **External Reference**: Groups reference external competition/team data via `competition` and `year` fields

### Index Configuration

The indices are created with default ElasticSearch mapping, allowing dynamic field mapping. The following HTTP requests set up the indices:

```http
PUT http://localhost:9200/users
PUT http://localhost:9200/groups
```

## ElasticSearch to Application Model Mapping

### User Mapping
```javascript
// ElasticSearch Document → Application Object
function aUserFromElastic(elasticUser) {
    return { 
        id: elasticUser._id,           // Document ID becomes user ID
        username: elasticUser._source.username,
        password: elasticUser._source.password,
        token: elasticUser._source.token
    };
}
```

### Group Mapping
```javascript
// ElasticSearch Document → Application Object  
function aGroupFromElastic(elasticGroup) {
    return { 
        id: elasticGroup._id,          // Document ID becomes group ID
        userId: elasticGroup._source.userId,
        name: elasticGroup._source.name,
        description: elasticGroup._source.description,
        competition: elasticGroup._source.competition,
        year: elasticGroup._source.year,
        players: elasticGroup._source.players || []
    };
}
```

### Query Patterns

1. **Find User by Username**:
```json
{
  "query": {
    "match": { "username": "targetUsername" }
  }
}
```

2. **Find User by Token**:
```json
{
  "query": {
    "match": { "token": "targetToken" }
  }
}
```

3. **Find Groups by User**:
```json
{
  "query": {
    "match": { "userId": "targetUserId" }
  }
}
```

## Server API Documentation

### Authentication

The API supports two authentication methods:
1. **Session-based** (for web UI): Uses Passport.js with local strategy
2. **Token-based** (for API): Uses Bearer tokens in Authorization header

#### Authentication Endpoints

| Method | Endpoint              | Description                          |
|--------|-----------------------|--------------------------------------|
| POST | `/users/register`     | Register a new user                  |
| POST | `/users/login`        | Authenticate and create session      |
| POST | `/api/users/register` | Register a new user and return token |
| POST | `/api/users/login`    | Authenticate and return token        |

### API Endpoints

#### Competitions
| Method | Endpoint                                       | Auth | Description                                  |
|--------|------------------------------------------------|------|----------------------------------------------|
| GET | `/api/competitions?limit={limit}`              | No | Get list of competitions with optional limit |
| GET | `/api/competitions/{code}/teams?season={year}` | No | Get teams by competition and season          |

#### Groups (Protected)
| Method | Endpoint | Auth | Description                             |
|--------|----------|------|-----------------------------------------|
| GET | `/api/groups` | Yes | List user's groups                      |
| GET | `/api/groups/{id}` | Yes | Get specific group with list of players |
| POST | `/api/groups` | Yes | Create new group                        |
| PUT | `/api/groups/{id}` | Yes | Update group                            |
| DELETE | `/api/groups/{id}` | Yes | Delete group                            |

#### Players (Protected)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/groups/{id}/players` | Yes | Add player to group |
| DELETE | `/api/groups/{id}/players/{playerId}` | Yes | Remove player from group |

### Request/Response Examples

#### Create Group
```http
POST /api/groups
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "My Dream Team",
  "description": "Best players from EPL 2024",
  "competition": "PL",
  "year": "2024"
}
```

#### Add Player to Group
```http
POST /api/groups/{groupId}/players
Authorization: Bearer {token}
Content-Type: application/json

{
  "playerId": "44",
  "teamId": "65"
}
```

### Error Handling

The API returns appropriate HTTP status codes:
- `200` - Success
- `201` - Created
- `204` - No Content (for deletions)
- `400` - Bad Request
- `401` - Unauthorized
- `404` - Not Found
- `409` - Conflict (already exists)

## Installation and Setup Instructions

### Prerequisites
- Node.js (v16 or later)
- npm or pnpm
- ElasticSearch (v7.x or v8.x)
- Git

### Step 1: Clone Repository
```bash
git clone https://github.com/isel-leic-ipw/isel-leic-ipw-pi-2526i-foccacia-leirt2526i-pi51d-g02.git
cd isel-leic-ipw-pi-2526i-foccacia-leirt2526i-pi51d-g02
```

### Step 2: Install Dependencies
```bash
npm install
# or
pnpm install
```

### Step 3: Setup ElasticSearch

1. **Install ElasticSearch** (if not already installed):
   - Download from https://www.elastic.co/downloads/elasticsearch
   - Follow installation instructions for your OS

2. **Start ElasticSearch**:
   ```bash
   # Linux/Mac
   ./bin/elasticsearch
   
   # Windows
   bin\elasticsearch.bat
   ```

3. **Create Indices**:
   ```bash
   curl -X PUT "localhost:9200/users"
   curl -X PUT "localhost:9200/groups"
   ```

### Step 4: Configure Environment
Create a `.env` file in the project root:
```env
FOOTBALL_API_URL=https://api.football-data.org/v4
FOOTBALL_API_KEY=your_api_key_here
SESSION_SECRET=your_secret_key_here
```

### Step 5: Load Test Data (Optional)
The application can be tested with in-memory data by switching imports in `foccacia-server.mjs`:
```javascript
// For production (ElasticSearch)
import foccaciaDataInit from './foccacia-elastic-data.mjs'
import fapiDataInit from './fapi-teams-data.mjs'

// For testing (In-memory)
// import foccaciaDataInit from './foccacia-data-mem.mjs'
// import fapiDataInit from './fapi-teams-data-mem.mjs'
```

### Step 6: Start Application
```bash
# Development mode (with auto-restart)
npm run dev

# Production mode
npm start
```

The application will be available at `http://localhost:8080`

### Automatic Test Data Setup
When using in-memory data modules, test data is automatically loaded including:
- Sample user "alice" with token "6092b4a4-5ee8-439b-9dcd-0fee9c5fd9b2"
- Sample groups and competitions
- Test teams and players

## Running Tests

### Prerequisites for Testing
Ensure ElasticSearch is running (for integration tests) or use in-memory modules for unit tests.

### Execute Tests
```bash
# Run all tests
npm test

# Run specific test suites
npx mocha test/service.test.js
npx mocha test/webapi.test.js
npx mocha test/fapi.test.js
```

### Test Coverage
The test suite covers:
- **Service Layer Tests** (`service.test.js`): Business logic validation
- **Web API Tests** (`webapi.test.js`): API endpoint testing
- **Football API Tests** (`fapi.test.js`): External API integration testing

### Troubleshooting

**Common Issues:**
- **ElasticSearch Connection**: Ensure ElasticSearch is running on localhost:9200
- **Football API**: Verify API key is valid and has sufficient quota
- **Port Conflicts**: Application uses port 8080, ensure it's available
- **Dependencies**: Run `npm install` if modules are missing

**Logs and Debugging:**
- Check console output for initialization errors
- ElasticSearch logs are in the ElasticSearch installation logs directory
- Set `NODE_ENV=development` for detailed error messages
