import express from 'express'
import { engine } from 'express-handlebars'
import cors from 'cors'
import session from 'express-session'
import passport from 'passport'
import foccaciaApiInit from './foccacia-web-api.mjs'
import foccaciaWebUiInit from './foccacia-web-ui.mjs'
import foccaciaServicesInit from './foccacia-services.mjs'
import fapiDataInit from './fapi-teams-data.mjs'
//import fapiDataInit from './fapi-teams-data-mem.mjs'
import foccaciaDataInit from './foccacia-elastic-data.mjs'
//import foccaciaDataInit from './foccacia-data-mem.mjs'
import passportConfig from './passport-config.mjs'

let foccaciaAPI;
let foccaciaWebUi;
let foccaciaServices;

try {
    const fapiData = fapiDataInit();
    const foccaciaData = foccaciaDataInit();
    foccaciaServices = foccaciaServicesInit(fapiData, foccaciaData);
    foccaciaAPI = foccaciaApiInit(foccaciaServices);
    foccaciaWebUi = foccaciaWebUiInit(foccaciaServices);
} catch (err) {
    console.error(err);
}

if (!foccaciaAPI) {
    throw new Error('Foccacia API initialization failed');
}

if (!foccaciaWebUi) {
    throw new Error('Foccacia Web UI initialization failed');
}

const app = express()

app.use(express.json())

app.use(cors())
app.use(express.static('css'))
app.use(express.urlencoded({extended: true}))

// Session configuration
app.use(session({
    secret: process.env.SESSION_SECRET || 'foccacia-secret-key-change-in-production',
    resave: false,
    saveUninitialized: false,
    cookie: {
        maxAge: 1000 * 60 * 60 * 24 * 7, // 7 days
        httpOnly: true,
        secure: false // Set to true in production with HTTPS
    }
}))

// Initialize Passport
passportConfig(passport, foccaciaServices)
app.use(passport.initialize())
app.use(passport.session())

app.set('views', './views')
app.engine('hbs', engine({ 
    defaultLayout: 'layout',
    extname: '.hbs',
    layoutsDir: './views/layouts',
    helpers: {
        section: function(name, options) {
            if (!this._sections) this._sections = {};
            this._sections[name] = options.fn(this);
            return null;
        }
    }
}));
app.set('view engine', 'hbs')

//UI routes

//Home
app.get('/', foccaciaWebUi.Home)

//Competitions
app.get('/competitions', foccaciaWebUi.getCompetitions)
app.get('/competitions/:competitionCode/teams', foccaciaWebUi.getTeamsByCompetitionSeason)

//User authentication
app.get('/users/accounts', foccaciaWebUi.getLoginForm)
app.get('/users/create', foccaciaWebUi.getCreateUserForm)
app.post('/users/register', foccaciaWebUi.registerUser)
app.post('/users/login', passport.authenticate('local', {
    successRedirect: '/groups',
    failureRedirect: '/users/accounts?error=Invalid+credentials'
}))
app.post('/users/logout', foccaciaWebUi.logoutUser)

//List and get groups - Protected routes
app.get('/groups', foccaciaWebUi.ensureAuthenticated, foccaciaWebUi.listGroups)
app.get('/groups/create', foccaciaWebUi.ensureAuthenticated, foccaciaWebUi.getCreateGroupForm)
app.get('/groups/:groupId', foccaciaWebUi.ensureAuthenticated, foccaciaWebUi.getGroup)
app.get('/groups/:groupId/edit', foccaciaWebUi.ensureAuthenticated, foccaciaWebUi.getEditGroupForm)
app.get('/groups/:groupId/add-player', foccaciaWebUi.ensureAuthenticated, foccaciaWebUi.getAddPlayerForm)

//Create, edit and delete groups - Protected routes
app.post('/groups', foccaciaWebUi.ensureAuthenticated, foccaciaWebUi.createGroup)
app.post('/groups/:groupId/edit', foccaciaWebUi.ensureAuthenticated, foccaciaWebUi.editGroup)
app.post('/groups/:groupId/delete', foccaciaWebUi.ensureAuthenticated, foccaciaWebUi.deleteGroup)
app.post('/groups/:groupId/add-player', foccaciaWebUi.ensureAuthenticated, foccaciaWebUi.addPlayerToGroup)
app.post('/groups/:groupId/remove-player', foccaciaWebUi.ensureAuthenticated, foccaciaWebUi.removePlayerFromGroup)

//API routes

//Competitions
app.get('/api/competitions', foccaciaAPI.getCompetitions)
app.get('/api/competitions/:competitionCode/teams', foccaciaAPI.getTeamsByCompetitionSeason)

app.post('/api/users/register', foccaciaAPI.createUser)
app.post('/api/users/login', passport.authenticate('local'), (req, res) => {
    res.status(200).json({ message: 'Login successful', token: req.user.token });
})

app.get('/api/groups', foccaciaAPI.authMiddleware, foccaciaAPI.listGroups)
app.get('/api/groups/:groupId', foccaciaAPI.authMiddleware, foccaciaAPI.getGroup)

app.post('/api/groups', foccaciaAPI.authMiddleware, foccaciaAPI.createGroup)
app.put('/api/groups/:groupId', foccaciaAPI.authMiddleware, foccaciaAPI.editGroup)
app.delete('/api/groups/:groupId', foccaciaAPI.authMiddleware, foccaciaAPI.deleteGroup)

app.post('/api/groups/:groupId/players', foccaciaAPI.authMiddleware, foccaciaAPI.addPlayerToGroup)
app.delete('/api/groups/:groupId/players/:playerId', foccaciaAPI.authMiddleware, foccaciaAPI.removePlayerFromGroup)

app.use((req, res) => {
    res.status(404).render('error', {
        error: 'Page not found', backLink: '/'
    });
});

app.listen(8080, () => console.log('Listening...'))