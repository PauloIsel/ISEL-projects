import dotenv from 'dotenv';

dotenv.config();

const URI_PREFIX = process.env.ELASTIC_URI || 'http://localhost:9200';

export function fetchElastic(method, path, body = undefined) {
    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(body)
    };

    // console.log(`Fetching Elastic: ${method} ${URI_PREFIX + path}`);
    return fetch(URI_PREFIX + path, options)
        // .then(response => console.log(`Elastic response status: ${response.status}`) || response)
        .then(response => response.json())
}