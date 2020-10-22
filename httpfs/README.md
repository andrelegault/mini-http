# httpfs
This repository consists of a file server.

## Usage

```
httpfs [-v] [-p PORT] [-d DATADIR]

-d <Data directory>   Specifies the data directory used for serving
                      files. Every request will be relative to that
                      directory. Default is DATA.
-p <port>             Specifies the port number that the server will
                      listen and serve at. Should be between 1024 and
                      65535. Default is 8080.
-v                    Prints debugging messages.
```

## Example

Running the server on port 8081 using the default data directory.
```
> java -jar <path-to-jar> -p 8081
[localhost:8081] => Server successfully started!
[localhost:8081] => Listening on port: 8081 | Data directory: <some-path>/DATA
```

## Testing httpc

Run `mvn test`

## Building httpc

Run `mvn clean compile assembly:single`.

This will produce a `.jar` file that can be run alongside any argument desired.
