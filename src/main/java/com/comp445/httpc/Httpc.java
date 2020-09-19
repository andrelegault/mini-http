package com.comp445.httpc;

import java.util.HashMap;

public class Httpc {
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

    public static HashMap<String, String> extractHeaders(String[] args) {
        final HashMap<String, String> extractedArgs = new HashMap<String, String>();
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("-h") && Httpc.isValidHeader(args[i + 1])) {
                extractedArgs.put(args[i], args[i + 1]);
                i++;
            }
        }
        return extractedArgs;
    }

    public static String extractData(String[] args) {
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("-d")) {
                return args[i + 1];
            }
        }
        return null;
    }

    public static String extractFilename(String[] args) {
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("-f")) {
                return args[i + 1];
            }
        }
        return null;
    }

    public static boolean isValidHeader(String value) {
        String[] keyVal = value.split(":");
        return keyVal.length == 2;
    }

    public static void main(String[] args) {
        final int argLen = args.length;
        if (argLen == 0 || args[0].equalsIgnoreCase("help")) {
            System.out.println(Httpc.usageGeneral());
            System.exit(1);
        }

        if (args[0].equalsIgnoreCase("get")) {
            if (argLen < 2) {
                System.out.println(Httpc.usageGet());
                System.exit(1);
            } else {
                String endpoint = "";
                for (int i = 0; i < argLen; i++) {
                    if (args[i].equals("-v")) {
                        Httpc.isVerbose = true;
                    } else if (i == (argLen - 1)) {
                        endpoint = args[i];
                    } else if (args[i].equals("-d") || args[i].equals("-f")) {
                        System.out.println(Httpc.usageGet());
                        System.exit(1);
                    }
                }
                final HashMap<String, String> headers = Httpc.extractHeaders(args);
                final Host host = new Host(endpoint);
                HttpcGet get = new HttpcGet(host, headers);
                get.connect();
            }

        } else if (args[0].equalsIgnoreCase("post")) {
            if (argLen < 2) {
                System.out.println(Httpc.usagePost());
            } else {
                String endpoint = "";
                for (int i = 0; i < argLen; i++) {
                    if (args[i].equals("-v")) {
                        Httpc.isVerbose = true;
                    } else if (i == (argLen - 1)) {
                        endpoint = args[i];
                    }
                }
                final HashMap<String, String> headers = Httpc.extractHeaders(args);
                final String data = Httpc.extractData(args);
                final String file = Httpc.extractFilename(args);
                if (file != null && !data.isEmpty()) {
                    System.out.println(Httpc.usagePost());
                    System.exit(1);
                }
                final Host host = new Host(endpoint);
                HttpcPost post = new HttpcPost(host, headers, data);
                post.connect();
            }
        } else {
            System.out.println(Httpc.usageGeneral());
            System.exit(1);
        }
    }
}
