# httpc
Software library and tool implementing HTTP/1.1 networking protocols (GET, and POST). This project uses maven for dependencies, building and testing (JUnit5).

## Usage

```
httpc command [arguments] URL

The commands are:
   get     executes a HTTP GET request and prints the response.
   post    executes a HTTP POST request and prints the response.
   help    prints this screen.

Use "http help [command]" for more information about a command.
```

Performing a GET request to http://www.google.com/: `java httpc-1.0.jar get -v http://www.google.com/`

Performing a POST request to google.com: `java -jar httpc-1.0.jar post -d '{"Assignment": 1}' -h test:value http://www.google.com/`

Run `java -jar httpc-1.0.jar help` for help.

## Testing httpc

Run `mvn test`

## Building httpc

Run `mvn clean compile assembly:single`.

This will produce a `.jar` file that can be run alongside any argument desired.
