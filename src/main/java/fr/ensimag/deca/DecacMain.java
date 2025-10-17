package fr.ensimag.deca;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import fr.ensimag.deca.codegen.MemoryManager;
import org.apache.log4j.Logger;

/**
 * Main class for the command-line Deca compiler.
 *
 * @author gl47
 * @date 01/01/2025
 */
public class DecacMain {
    private static Logger LOG = Logger.getLogger(DecacMain.class);

    public static void main(String[] args) {
        LOG.info("Decac compiler started");
        boolean error = false;
        final CompilerOptions options = new CompilerOptions();

        // Parse command-line arguments
        try {
            options.parseArgs(args);
        } catch (CLIException e) {
            System.err.println("Error during option parsing:\n" + e.getMessage());
            options.displayUsage();
            System.exit(1);
        }

        // Handle the -b option (banner)
        if (options.getPrintBanner()) {
            System.out.println("Decac Compiler g47");
            System.out.println("JAAFOURA Mouez - MIDOU Ilyas - KACIMI Salim - Dimassi Mehdi- Kazdaghli Meriem");
            System.exit(0);
        }

        // Ensure source files are provided unless -b is used
        if (options.getSourceFiles().isEmpty()) {
            System.err.println("No source files provided. Use -h for help.");
            System.exit(1);
        }

        // Handle parallel compilation (-P option)
        if (options.getParallel()) {
            LOG.info("Parallel compilation enabled");
            ExecutorService executor = Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors()
            );
            List<Future<Boolean>> futures = new ArrayList<>();
        
            for (File source : options.getSourceFiles()) {
                DecacCompiler compiler = new DecacCompiler(options, source);
                // Create a new MemoryManager for each compiler instance
                MemoryManager memoryManager = compiler.getMemoryManager();
        
                // Submit compilation tasks to the executor
                Future<Boolean> future = executor.submit(() -> compiler.compile());
                futures.add(future);
            }
        
            // Collect results and handle errors
            for (Future<Boolean> future : futures) {
                try {
                    if (future.get()) {
                        error = true;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    LOG.error("Error during parallel compilation", e);
                    error = true;
                }
            }
        
            // Shutdown the executor
            executor.shutdown();
        } else {
            // Sequential compilation
            for (File source : options.getSourceFiles()) {
                DecacCompiler compiler = new DecacCompiler(options, source);
                MemoryManager memoryManager = compiler.getMemoryManager();
                if (options.getMaxRegisters() != 16) {
                    memoryManager.setMaxRegisters(options.getMaxRegisters()-1);
                }
                if (compiler.compile()) {
                    error = true;
                }
            }
        }

        // Exit with appropriate status code
        System.exit(error ? 1 : 0);

    }
}
