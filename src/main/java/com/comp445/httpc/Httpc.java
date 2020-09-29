package com.comp445.httpc;

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

public class Httpc {
    private String usageGeneral = "Usage:\n".concat("   httpc command [arguments]\n").concat("The commands are:\n")
            .concat("   get     executes a HTTP GET request and prints the response.\n")
            .concat("   post    executes a HTTP POST request and prints the response.\n")
            .concat("   help    prints this screen.\n\n")
            .concat("Use \"httpc help [command]\" for more information about a command.");

    private String usageGet = "httpc help get\n".concat("   usage: httpc get [-v] [-h key:value]* URL\n")
            .concat("Get executes a HTTP GET request for a given URL.\n")
            .concat("   -v      Prints the detail of the response such as protocol, status, and headers.\n")
            .concat("   -h key:value    Associates headers to HTTP Request with the format 'key:value'.");

    private String usagePost = "httpc help post\n"
            .concat("    usage: httpc post [-v] [-h key:value]* [-d inline-data] [-f file] URL\n")
            .concat("Post executes a HTTP POST request for a given URL with inline data or from file.\n")
            .concat("   get     executes a HTTP GET request and prints the response.\n")
            .concat("   post    executes a HTTP POST request and prints the response.\n")
            .concat("   help    prints this screen.\n\n")
            .concat("Use \"httpc help [command]\" for more information about a command.");
    final Set<String> validActions = Set.of("post", "get");

    private final CommandLineParser parser = new DefaultParser();
    private final Options options = new Options();
    private CommandLine cmdLine;
    private String[] args;

    protected HttpcRequest req;
    protected boolean verbose = false;
    protected Map<String, String> headers = new HashMap<String, String>();
    protected String data;
    protected String action;
    protected String target;

    public Httpc(final String[] args) throws ParseException {
        this.args = args;
        parse();
    }

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
     */
    private void collectData() {
        this.data = cmdLine.hasOption("d") ? cmdLine.getOptionValue("d") : cmdLine.getOptionValue("f");
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
                    throw new Exception("Missing option!");
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
