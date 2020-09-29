package com.comp445.httpc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

// import org.json.JSONObject;

public class Httpc {
    // General usage instructions
    private String usageGeneral = "Usage:\n".concat("   httpc command [arguments]\n").concat("The commands are:\n")
            .concat("   get     executes a HTTP GET request and prints the response.\n")
            .concat("   post    executes a HTTP POST request and prints the response.\n")
            .concat("   help    prints this screen.\n\n")
            .concat("Use \"httpc help [command]\" for more information about a command.");

    // Usage of the get command
    private String usageGet = "httpc help get\n".concat("   usage: httpc get [-v] [-h key:value]* URL\n")
            .concat("Get executes a HTTP GET request for a given URL.\n")
            .concat("   -v      Prints the detail of the response such as protocol, status, and headers.\n")
            .concat("   -h key:value    Associates headers to HTTP Request with the format 'key:value'.");

    // Usage of the post command
    private String usagePost = "httpc help post\n"
            .concat("    usage: httpc post [-v] [-h key:value]* [-d inline-data] [-f file] URL\n")
            .concat("Post executes a HTTP POST request for a given URL with inline data or from file.\n")
            .concat("   get     executes a HTTP GET request and prints the response.\n")
            .concat("   post    executes a HTTP POST request and prints the response.\n")
            .concat("   help    prints this screen.\n\n")
            .concat("Use \"httpc help [command]\" for more information about a command.");
    // All valid actions
    final Set<String> validActions = Set.of("post", "get");

    // Object that parses arguments provided through the CLI
    private final CommandLineParser parser = new DefaultParser();

    // Contains all options
    private final Options options = new Options();

    // Contains all parsed arguments
    private CommandLine cmdLine;

    // Contains the raw arguments provided
    private String[] args;

    // Request created using provided arguments
    protected HttpcRequest req;

    // Existence of verbose option
    protected boolean verbose = false;

    // Request headers
    protected Map<String, String> headers = new HashMap<String, String>();

    // Request data
    protected String data;

    // Request action
    protected String action;

    // Request target
    protected String target;

    /**
     * Constructor.
     * 
     * @param args Arguments provided through CLI.
     * @throws Exception
     */
    public Httpc(final String[] args) throws Exception {
        this.args = args;
        parse();
    }

    /**
     * Represents whether a string is a valid request.
     * 
     * @param check String to check.
     * @return boolean Whether the check is a request.
     */
    private boolean isRequest(final String check) {
        if (check != null && !check.isEmpty() && (validActions.contains(check))) {
            return true;
        } else {
            return false;
        }
    }

    private void preparePostOptions() {
        final Option optDataString = Option.builder("d").required(false).hasArg(true).desc("JSON string request body")
                .build();
        final Option optDataFile = Option.builder("f").required(false).hasArg(true).desc("File request body").build();
        options.addOption(optDataString);
        options.addOption(optDataFile);
    }

    /**
     * Sets the verbose and headers options.
     */
    private void prepareCommonOptions() {
        final Option optVerbose = Option.builder("v").required(false).hasArg(false).desc("Makes the program verbose")
                .build();
        final Option optHeaders = Option.builder("h").required(false).hasArg(true).hasArgs().valueSeparator(':')
                .desc("Headers").build();
        options.addOption(optVerbose);
        options.addOption(optHeaders);
    }

    /**
     * Sets the `headers` variable.
     */
    private void collectHeaders() {
        final Properties properties = cmdLine.getOptionProperties("h");
        this.headers = new HashMap<String, String>((Map) properties);
    }

    /**
     * Sets the `data` variable.
     * 
     * @throws IOException
     */
    private void collectData() throws IOException {
        this.data = cmdLine.hasOption("d") ? cmdLine.getOptionValue("d")
                : loadFileContents(Path.of(cmdLine.getOptionValue("f")));
    }

    /**
     * Loads a file's contents into a String.
     * 
     * @return String Contents of the file.
     * @throws IOException
     */
    private String loadFileContents(Path filePath) throws IOException {
        return Files.readString(filePath);
    }

    /**
     * Sets the `verbose` variable.
     */
    private void collectVerbose() {
        this.verbose = cmdLine.hasOption("v");
    }

    /**
     * Sets the `target` variable.
     */
    private void setTarget() {
        this.target = args[args.length - 1];
    }

    /**
     * Parses the arguments passed, and sets member variables accordingly.
     * 
     * @param args Arguments passed.
     * @throws ParseException
     */
    private void parse() throws Exception {
        final int argLen = args.length;
        if (argLen == 0) {
            System.err.println(usageGeneral);
        }
        this.action = args[0];
        if (this.action.equalsIgnoreCase("help")) {
            if (args[1].equalsIgnoreCase("get")) {
                System.err.println(usageGet);
            } else if (args[1].equalsIgnoreCase("post")) {
                System.err.println(usagePost);
            } else {
                System.err.println(usageGeneral);
            }
        }

        if (isRequest(this.action)) {
            prepareCommonOptions();
            setTarget();
            if (action.equalsIgnoreCase("get")) {
                cmdLine = parser.parse(options, args);
                collectVerbose();
                collectHeaders();
            } else if (action.equalsIgnoreCase("post")) {
                preparePostOptions();
                this.cmdLine = parser.parse(options, args);
                if (cmdLine.hasOption("d") ^ cmdLine.hasOption("f")) {
                    collectVerbose();
                    collectHeaders();
                    collectData();
                } else {
                    System.err.println(usagePost);
                    throw new Exception("Invalid use of -d or -f!");
                }
            } else {
                System.err.println(usageGeneral);
            }
        }
    }

    private void run() {
        if (this.action.equalsIgnoreCase("get")) {
            this.req = new HttpcGet(this.target, headers, verbose);
        } else if (this.action.equalsIgnoreCase("post")) {
            this.req = new HttpcPost(this.target, this.headers, this.data, this.verbose);
        }
    }
}
