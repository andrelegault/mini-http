# httpc
Software library and tool implementing HTTP/1.1 networking protocols (GET, and POST). This project uses maven.

## Usage

Performing a GET request to google.com: `httpc get http://www.google.com/`

Performing a POST request to google.com: `httpc post -h '{"Assignment": 1}' -d test:value http://www.google.com/`

Run `httpc help` for help.

## Testing httpc

Run `mvn test`