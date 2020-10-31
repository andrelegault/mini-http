# mini-http
mini-http is a software library and tool implementing HTTP/1.0. This project uses maven for dependencies, building and testing.

There are currently 2 components in this library: httpc and httpfs.

## httpc
httpc is a command-line tool used to form HTTP/1.0 requests that are to be sent to servers on the internet.

## httpfs
httpfs is a file server capable of accepting HTTP/1.0 requests.

## Testing
Run `mvn test` in either the `httpc` or `httpfs` directory to test components.

To test multi-threading features:
1. `chmod +x multi_threading_test.sh`
2. `./multi_threading_test.sh`

## Building
Run `mvn clean compile assembly:single` in either `httpc` or `httpfs` to build a deployable jar file.
