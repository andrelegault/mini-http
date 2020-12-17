# mini-http
mini-http is a software library and tool implementing HTTP/1.0. This project uses maven for dependencies, building and testing.

There are 2 implementations of this project: a TCP and a UDP implementation.

There are 2 components in each implementation: httpc and httpfs.

## httpc
httpc is a command-line tool used to form HTTP/1.0 requests.

## httpfs
httpfs is a file server capable of accepting HTTP/1.0 requests.

## Testing
Run `mvn test` to test components.

To test multi-threading features:
1. `chmod +x <tcp|udp>_multi_threading_test.sh`
2. `./<tcp|udp>_multi_threading_test.sh`

## Building
Run `mvn clean package` to build the project.
