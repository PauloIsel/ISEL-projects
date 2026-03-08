const ERRORS_MAPPER = {
    e1: 404,    // Not found
    e2: 401,    // Unauthorized
    e3: 400,    // Invalid operation
    e4: 409,    // Already exists
    e5: 500     // Internal server error
}

const DEFAULT_ERROR = {
    status: 500, 
    message: `An internal error occurred. Contact your system administrator`
}

export function getErrorResponse(error) {
    const status = ERRORS_MAPPER[error.code]
    return status ?  
        {
            status: status, 
            message: error.error
        } 
        : DEFAULT_ERROR
}