import { Strategy as LocalStrategy } from 'passport-local'

export default function configurePassport(passport, foccaciaServices) {
    // Local Strategy for username/password authentication
    passport.use(new LocalStrategy(
        {
            usernameField: 'username',
            passwordField: 'password'
        },
        async (username, password, done) => {
            try {
                const user = await foccaciaServices.authenticateUser(username, password)
                if (!user) {
                    return done(null, false, { message: 'Invalid username or password' })
                }
                return done(null, user)
            } catch (error) {
                return done(error)
            }
        }
    ))

    // Serialize user to session
    passport.serializeUser((user, done) => {
        done(null, user.token)
    })

    // Deserialize user from session
    passport.deserializeUser(async (token, done) => {
        try {
            const user = await foccaciaServices.getUserByToken(token)
            done(null, user)
        } catch (error) {
            done(error)
        }
    })
}

