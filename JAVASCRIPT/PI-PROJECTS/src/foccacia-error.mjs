const ErrorCodes = {
    NOT_FOUND: 'e1',
    NOT_AUTHORIZE: 'e2',
    INVALID_OPERATION: 'e3',
    ALREADY_EXISTS: 'e4',
    INTERNAL_ERROR: 'e5'
}

function createError(errorCode, customMessage) {
    return Promise.reject({
        code: errorCode,
        error: customMessage
    })
}

function createErrorObject(errorCode, customMessage) {
    return {
        code: errorCode,
        error: customMessage
    }
}

export function notFound(customMessage) {
    return createError(ErrorCodes.NOT_FOUND, customMessage || "Resource not found")
}

export function notFoundError(customMessage) {
    return createErrorObject(ErrorCodes.NOT_FOUND, customMessage || "Resource not found")
}

export function notAuthorize(customMessage) {
    return createError(ErrorCodes.NOT_AUTHORIZE, customMessage || "Unauthorized access")
}

export function invalidOperation(customMessage) {
    return createError(ErrorCodes.INVALID_OPERATION, customMessage || "Invalid operation")
}

export function invalidOperationError(customMessage) {
    return createErrorObject(ErrorCodes.INVALID_OPERATION, customMessage || "Invalid operation")
}

export function alreadyExists(customMessage) {
    return createError(ErrorCodes.ALREADY_EXISTS, customMessage || "Resource already exists")
}

export function alreadyExistsError(customMessage) {
    return createErrorObject(ErrorCodes.ALREADY_EXISTS, customMessage || "Resource already exists")
}

export function internalError(customMessage) {
    return createError(ErrorCodes.INTERNAL_ERROR, customMessage || "Internal server error")
}

const errors = {
    NOT_FOUND: notFound,
    NOT_AUTHORIZE: notAuthorize,
    INVALID_OPERATION: invalidOperation,
    ALREADY_EXISTS: alreadyExists,
    INTERNAL_ERROR: internalError
}

export default errors
