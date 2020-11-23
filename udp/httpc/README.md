# httpc client
This repository consists of a CLI to construct HTTP/1.0 requests.

## Usage

```
httpc command [arguments] URL

The commands are:
   get     executes a HTTP GET request and prints the response.
   post    executes a HTTP POST request and prints the response.
   help    prints this screen.

Use "http help [command]" for more information about a command.
```

Performing a GET request to http://www.google.com/: `java -jar <path-to-jar> get -v http://www.google.com/`

Performing a POST request to google.com: `java -jar <path-to-jar> post -d '{"Assignment": 1}' -h test:value http://www.google.com/`

Run `java -jar <path-to-jar> help` for help.

## Testing httpc

Run `mvn test`

## Building httpc

Run `mvn clean compile assembly:single`.

This will produce a `.jar` file that can be run alongside any argument

The jar will be, by default, under `target/httpc-1.0-SNAPSHOT-jar-with-dependencies.jar`.
