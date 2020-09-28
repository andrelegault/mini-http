package com.comp445.httpc;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class Httpc {
    // TODO: use https://github.com/apache/commons-cli to parse arguments.
    public static boolean isVerbose = false;

    public static String usageGeneral() {
        return "Usage:\n".concat("   httpc command [arguments]\n").concat("The commands are:\n")
                .concat("   get     executes a HTTP GET request and prints the response.\n")
                .concat("   post    executes a HTTP POST request and prints the response.\n")
                .concat("   help    prints this screen.\n\n")
                .concat("Use \"httpc help [command]\" for more information about a command.");
    }

    public static String usageGet() {
        return "httpc help get\n".concat("   usage: httpc get [-v] [-h key:value] URL\n")
                .concat("Get executes a HTTP GET request for a given URL.\n")
                .concat("   -v      Prints the detail of the response such as protocol, status, and headers.\n")
                .concat("   -h key:value    Associates headers to HTTP Request with the format 'key:value'.");
    }

    public static String usagePost() {
        return "httpc help post\n".concat("    usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL\n")
                .concat("Post executes a HTTP POST request for a given URL with inline data or from file.\n")
                .concat("   get     executes a HTTP GET request and prints the response.\n")
                .concat("   post    executes a HTTP POST request and prints the response.\n")
                .concat("   help    prints this screen.\n\n")
                .concat("Use \"httpc help [command]\" for more information about a command.");
    }

    public static void main(final String[] args) {
        final int argLen = args.length;
        if (argLen == 0) {
            System.out.println(Httpc.usageGeneral());
            System.exit(1);
        } else {
            if (args[0].equalsIgnoreCase("help")) {
                if (args[1].equalsIgnoreCase("get")) {
                    System.out.println(Httpc.usageGet());
                    System.exit(1);
                } else if (args[1].equalsIgnoreCase("post")) {
                    System.out.println(Httpc.usagePost());
                    System.exit(1);
                }
            } else {
                System.out.println(Httpc.usageGeneral());
                System.exit(1);
            }

        }

        final String action = args[0];

        if (action.equalsIgnoreCase("get") || action.equalsIgnoreCase("post")) {
            final CommandLineParser parser = new DefaultParser();
            final Options options = new Options();
            final Option optVerbose = Option.builder("v").required(false).hasArg(false)
                    .desc("Makes the program verbose").build();
            options.addOption(optVerbose);
            final Option optHeaders = Option.builder("h").required(false).hasArg(true).hasArgs().valueSeparator(':')
                    .desc("Headers").build();
            options.addOption(optHeaders);
            try {
                if (action.equalsIgnoreCase("get")) {
                    final CommandLine cmdLine = parser.parse(options, args);
                    final Properties properties = cmdLine.getOptionProperties("h");
                    final Map<String, String> headers = new HashMap<String, String>((Map) properties);
                    final HttpcGet get = new HttpcGet(args[argLen - 1], headers);
                    get.connect();
                } else if (action.equalsIgnoreCase("post")) {
                    final Option optDataString = Option.builder("d").required(true).hasArg(true)
                            .desc("JSON string request body").build();
                    final Option optDataFile = Option.builder("f").required(true).hasArg(true).desc("File request body")
                            .build();
                    options.addOption(optDataString);
                    options.addOption(optDataFile);
                    final CommandLine cmdLine = parser.parse(options, args);
                    if (cmdLine.hasOption("d") || cmdLine.hasOption("f")) {
                        if (cmdLine.hasOption("d") && cmdLine.hasOption("f")) {
                            System.out.println(Httpc.usagePost());
                            System.exit(1);
                        }
                        final Properties properties = cmdLine.getOptionProperties("h");
                        final Map<String, String> headers = new HashMap<String, String>((Map) properties);
                        String data;
                        if (cmdLine.hasOption("d")) {
                            data = cmdLine.getOptionValue("d");
                        } else {
                            data = cmdLine.getOptionValue("f");
                        }
                        final HttpcPost post = new HttpcPost(args[argLen - 1], headers, data);
                        post.connect();
                    } else {
                        System.out.println(Httpc.usagePost());
                        System.exit(1);
                    }
                } else {
                    System.out.println(Httpc.usageGeneral());
                    System.exit(1);
                }
            } catch (Exception e) {
                System.out.println(Httpc.usageGeneral());
                System.exit(1);
            }
        }

    }
}
