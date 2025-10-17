package fr.ensimag.deca;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * User-specified options influencing the compilation.
 *
 * @author gl47
 * @date 01/01/2025
 */
public class CompilerOptions {
    public static final int QUIET = 0;
    public static final int INFO = 1;
    public static final int DEBUG = 2;
    public static final int TRACE = 3;

    public int getDebug() {
        return debug;
    }

    public boolean getParallel() {
        return parallel;
    }

    public boolean getPrintBanner() {
        return printBanner;
    }

    public List<File> getSourceFiles() {
        return Collections.unmodifiableList(sourceFiles);
    }

    public boolean getParseStop() {
        return stopAfterParsing;
    }
    public boolean getVerificationStop() {
        return stopAfterVerification;
    }
    public boolean getNoCheck() {
        return noCheck;
    }
    public int getMaxRegisters(){
        return maxRegisters;
    }

    private int debug = 0;
    private boolean parallel = false;
    private boolean printBanner = false;
    private List<File> sourceFiles = new ArrayList<File>();
    private boolean stopAfterParsing = false;
    private boolean stopAfterVerification = false;
    private boolean noCheck = false;
    private int maxRegisters = 16;


    public void parseArgs(String[] args) throws CLIException {
        Logger logger = Logger.getRootLogger();
        int i = 0; // Index pour parcourir les arguments

        while (i < args.length) {
            String arg = args[i];
            switch (arg) {
                case "-b":
                    logger.debug("-b detected: print banner");
                    printBanner = true;
                    break;

                case "-p":
                    logger.debug("-p detected: stop after parsing");
                    stopAfterParsing = true;
                    break;

                case "-v":
                    logger.debug("-v detected: stop after verification");
                    stopAfterVerification = true;
                    break;

                case "-n":
                    logger.debug("-n detected: disable runtime checks");
                    noCheck = true;
                    break;

                case "-P":
                    logger.debug("-P detected: enable parallel compilation");
                    parallel = true;
                    break;

                case "-d":
                    logger.debug("-d detected: enable debug traces");
                    debug++;
                    break;

                case "-r":
                    logger.debug("-r detected: limit registers");
                    if (i + 1 >= args.length) {
                        throw new CLIException("Option '-r' requires a numeric argument between 4 and 16.");
                    }
                    try {
                        i++;
                        int registerCount = Integer.parseInt(args[i]);
                        if (registerCount < 4 || registerCount > 16) {
                            throw new CLIException("Option '-r' must be between 4 and 16.");
                        }
                        maxRegisters = registerCount;
                        logger.debug("Register count set to " + maxRegisters);
                    } catch (NumberFormatException e) {
                        throw new CLIException("Invalid argument for '-r'. Expected a number.");
                    }
                    break;

                default:
                    if (arg.startsWith("-")) {
                        throw new CLIException("Unknown option: " + arg);
                    } else {
                        logger.debug("Adding source file: " + arg);
                        sourceFiles.add(new File(arg));
                    }
            }
            i++;
        }

        // Vérification si -b est activé, qu'il n'y ait aucune autre option ou fichier source
        if (printBanner && (stopAfterParsing || stopAfterVerification || noCheck || parallel || debug > 0 || !sourceFiles.isEmpty())) {
            throw new CLIException("L'option '-b' ne peut être utilisée que si aucune autre option ni aucun fichier source n'est spécifié.");
        }

        // Vérification que -v et -p ne sont pas utilisés ensemble
        if (stopAfterParsing && stopAfterVerification) {
            throw new CLIException("Options '-v' and '-p' cannot be used together.");
        }
        // map command-line debug option to log4j's level.
        switch (getDebug()) {
            case QUIET:
                break; // keep default
            case INFO:
                logger.setLevel(Level.INFO);
                break;
            case DEBUG:
                logger.setLevel(Level.DEBUG);
                break;
            case TRACE:
                logger.setLevel(Level.TRACE);
                break;
            default:
                logger.setLevel(Level.ALL);
                break;
        }
        logger.info("Application-wide trace level set to " + logger.getLevel());

        boolean assertsEnabled = false;
        assert assertsEnabled = true; // Intentional side effect!!!
        if (assertsEnabled) {
            logger.info("Java assertions enabled");
        } else {
            logger.info("Java assertions disabled");
        }
    }

    protected void displayUsage() {
        PrintStream out = System.out;
        out.println("Usage: [[-p | -v] [-n] [-r X] [-d]* [-P] [-w] <fichier deca>...] | [-b]");
        out.println();
        out.println("Options:");
        out.println("  -b                Print the banner and exit.");
        out.println("  -p                Stop after parsing. Display the decompiled AST.");
        out.println("  -v                Stop after verification. No output if there are no errors.");
        out.println("  -n                Disable runtime checks (overflow, null pointer, etc.).");
        out.println("  -r <num>          Limit the number of registers to <num> (from 4 to 16).");
        out.println("  -P                Enable parallel compilation.");
        out.println("  -d                Enable debug traces. Repeat the option for increased verbosity.");
        out.println();
        out.println("Examples:");
        out.println("  decac -b");
        out.println("  decac -p file1.deca file2.deca");
        out.println("  decac -v -r 8 file1.deca");
        out.println();
        out.println("For more information, consult the documentation.");
    }
    
}