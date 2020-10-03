package com.comp445.httpc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

// import org.json.JSONObject;

public class Httpc {
    // General usage instructions
    private final String usageGeneral = "Usage:\n".concat("   httpc command [arguments] URL\n")
            .concat("The commands are:\n").concat("   get     executes a HTTP GET request and prints the response.\n")
            .concat("   post    executes a HTTP POST request and prints the response.\n")
            .concat("   help    prints this screen.\n\n")
            .concat("Use \"httpc help [command]\" for more information about a command.");

    // Usage of the get command
    private final String usageGet = "httpc help get\n".concat("   usage: httpc get [-v] [-h key:value]* URL\n")
            .concat("Get executes a HTTP GET request for a given URL.\n")
            .concat("   -v      Prints the detail of the response such as protocol, status, and headers.\n")
            .concat("   -h key:value    Associates headers to HTTP Request with the format 'key:value'.");

    // Usage of the post command
    private final String usagePost = "httpc help post\n"
            .concat("    usage: httpc post [-v] [-h key:value]* [-d inline-data] [-f file] URL\n")
            .concat("Post executes a HTTP POST request for a given URL with inline data or from file.\n")
            .concat("   get     executes a HTTP GET request and prints the response.\n")
            .concat("   post    executes a HTTP POST request and prints the response.\n")
            .concat("   help    prints this screen.\n\n")
            .concat("Use \"httpc help [command]\" for more information about a command.");

    private final HelpFormatter formatter = new HelpFormatter();

    // All valid actions
    private final Set<String> validActions = Set.of("post", "get");
    private final Set<String> validCommands = Set.of("post", "get", "help");

    // Object that parses arguments provided through the CLI
    private final CommandLineParser parser = new DefaultParser();

    // Contains all options
    private final Options options = new Options();

    // Contains all parsed arguments
    private CommandLine cmdLine;

    // Contains the raw arguments provided
    private final String[] args;

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

    // Filename to output to
    protected String outputFilename;

    /**
     * Constructor.
     * 
     * @param args Arguments provided through CLI.
     * @throws Exception
     */
    public Httpc(final String[] args) {
        this.args = args;
        try {
            parse();
        } catch (Exception e) {
            if (this.action == null || this.action.equalsIgnoreCase("help")) {
                System.err.println(usageGeneral);
            } else {
                formatter.printHelp("httpc " + this.action + " [arguments] URL", options);
            }
            // e.printStackTrace();
        }
    }

    /**
     * Represents whether a string is a valid request.
     * 
     * @param check String to check.
     * @return boolean Whether the check is a request.
     */
    private boolean isRequest(final String check) {
        if (check != null && !check.isEmpty()) {
            for (String validAction : validActions) {
                if (validAction.equalsIgnoreCase(check.toLowerCase())) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    /**
     * Adds the options necessary for the post, and help commands.
     */
    private void prepareCommonOptions() {
        final Option optVerbose = Option.builder("v").required(false).hasArg(false)
                .desc("Prints the detail of the response such as protocol, status, and headers.").build();
        final Option optHeaders = Option.builder("h").required(false).hasArgs().valueSeparator(':')
                .desc("Associates headers to HTTP Request with the format 'key:value'.").build();
        final Option optOutputFilename = Option.builder("o").required(false).hasArg()
                .desc("Writes to the specified file").build();
        options.addOption(optVerbose);
        options.addOption(optHeaders);
        options.addOption(optOutputFilename);
    }

    private void prepareGetOptions() {
        prepareCommonOptions();
    }

    /**
     * Adds the options necessary for the post command.
     */
    private void preparePostOptions() {
        prepareCommonOptions();
        final Option optDataString = Option.builder("d").required(false).hasArg(true)
                .desc("Associates an inline data to the body HTTP POST request.").build();
        final Option optDataFile = Option.builder("f").required(false).hasArg(true)
                .desc("Associates the content of a file to the body HTTP POST request.").build();
        options.addOption(optDataString);
        options.addOption(optDataFile);
    }

    /**
     * Sets the `headers` variable.
     */
    private void setHeaders() {
        final Properties properties = cmdLine.getOptionProperties("h");
        this.headers = new HashMap<String, String>((Map) properties);
    }

    /**
     * Sets the `data` variable.
     * 
     * @throws IOException
     */
    private void setData() throws IOException {
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
    private void setVerbose() {
        this.verbose = cmdLine.hasOption("v");
    }

    private Set<String> getOptionValues() {
        Set<String> vals = new HashSet<String>();
        for (Option o : cmdLine.getOptions()) {
            if (o.getValue() != null) {
                if (o.getValues().length > 1) {
                    String val = "";
                    vals.add(val.toLowerCase());
                } else {
                    vals.add(o.getValue());
                }
            }
        }
        return vals;
    }

    /**
     * Searches for a URL in the list of args (excluding options). Sets the target
     * if possible, throws otherwise.
     * 
     * @throws Exception
     */
    private void setTarget() throws Exception {
        String testTarget = args[args.length - 1];
        if (testTarget != null && (testTarget.isEmpty() || !testTarget.startsWith("http://"))) {
            throw new Exception("Error providing target");
        } else {
            this.target = testTarget;
        }
        // String urlArg = "";
        // int possibleUrlArgs = 0;
        // final Set<String> vals = getOptionValues();
        // for (String arg : args) {
        // if (!arg.isEmpty() && arg.charAt(0) != '-' && !isRequest(arg) &&
        // !arg.equalsIgnoreCase("help")
        // && !vals.contains(arg.toLowerCase())) {
        // urlArg = arg;
        // possibleUrlArgs++;
        // System.out.println(urlArg);
        // }
        // }
        // if (possibleUrlArgs != 1) {
        // throw new Exception("Error providing target");
        // } else {
        // this.target = urlArg;
        // }
    }

    /**
     * Sets the outputFilename.
     */
    private void setOutputFilename() {
        this.outputFilename = cmdLine.getOptionValue("o");
    }

    /**
     * Parses the arguments passed, and sets member variables accordingly.
     * 
     * @param args Arguments passed.
     * @throws ParseException
     */
    private void parse() throws Exception {
        final int argLen = args.length;

        if (argLen < 1) {
            throw new Exception("Insufficient arguments provided!");
        }

        this.action = args[0];
        if (this.action.equalsIgnoreCase("help")) {
            if (args[1].equalsIgnoreCase("get")) {
                prepareGetOptions();
                this.action = "get";
                throw new Exception();
            } else if (args[1].equalsIgnoreCase("post")) {
                preparePostOptions();
                this.action = "post";
                throw new Exception();
            } else {
                throw new Exception();
            }
        }

        if (isRequest(this.action)) {
            if (action.equalsIgnoreCase("get")) {
                prepareGetOptions();
                cmdLine = parser.parse(options, args);
                setTarget();
                setVerbose();
                setHeaders();
                setOutputFilename();
            } else if (action.equalsIgnoreCase("post")) {
                preparePostOptions();
                this.cmdLine = parser.parse(options, args);
                setTarget();
                if (cmdLine.hasOption("d") ^ cmdLine.hasOption("f")) {
                    setVerbose();
                    setHeaders();
                    setData();
                    setOutputFilename();
                } else {
                    throw new Exception("Invalid use of -d or -f");
                }
            } else {
                throw new Exception("Invalid use of -d or -f");
            }
        }
    }

    private void run() {
        if (this.action.equalsIgnoreCase("get")) {
            this.req = new HttpcGet(this.target, this.headers, this.verbose, this.outputFilename);
        } else if (this.action.equalsIgnoreCase("post")) {
            this.req = new HttpcPost(this.target, this.headers, this.data, this.verbose, this.outputFilename);
        }
        if (this.req != null) {
            this.req.connect();
        }
    }

    public static void main(final String[] args) {
        final Httpc httpc = new Httpc(args);
        if (httpc.req != null) {
            httpc.run();
        }
    }
}
