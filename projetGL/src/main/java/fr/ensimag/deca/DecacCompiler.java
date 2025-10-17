package fr.ensimag.deca;

import fr.ensimag.deca.codegen.LabelManager;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.syntax.DecaLexer;
import fr.ensimag.deca.syntax.DecaParser;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.AbstractProgram;
import fr.ensimag.deca.tree.LocationException;
import fr.ensimag.ima.pseudocode.AbstractLine;
import fr.ensimag.ima.pseudocode.IMAProgram;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.Label;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.log4j.Logger;

/**
 * Decac compiler instance.
 *
 * This class is to be instantiated once per source file to be compiled. It
 * contains the meta-data used for compiling (source file name, compilation
 * options) and the necessary utilities for compilation (symbol tables, abstract
 * representation of target file, ...).
 *
 * It contains several objects specialized for different tasks. Delegate methods
 * are used to simplify the code of the caller (e.g. call
 * compiler.addInstruction() instead of compiler.getProgram().addInstruction()).
 *
 * @author gl47
 * @date 01/01/2025
 */
public class DecacCompiler {
    private static final Logger LOG = Logger.getLogger(DecacCompiler.class);
    private boolean noCheck = false; // Drapeau pour l'option -n
    private final MemoryManager memoryManager;
    private final LabelManager labelManager;

    public void setNoCheck(boolean noCheck) {
        this.noCheck = noCheck;
    }
    public MemoryManager getMemoryManager() {
        return memoryManager;
    }
    public boolean isNoCheckEnabled() {
        return noCheck;
    }
    public LabelManager getLabelManager() {
        return labelManager;
    }
    /**
     * Portable newline character.
     */
    private static final String nl = System.getProperty("line.separator", "\n");

    public DecacCompiler(CompilerOptions compilerOptions, File source) {
        super();
        this.compilerOptions = compilerOptions;
        this.source = source;
        this.memoryManager = new MemoryManager();  // Initialize MemoryManager instance
        this.labelManager = new LabelManager();

    }

    /**
     * Source file associated with this compiler instance.
     */
    public File getSource() {
        return source;
    }

    /**
     * Compilation options (e.g. when to stop compilation, number of registers
     * to use, ...).
     */
    public CompilerOptions getCompilerOptions() {
        return compilerOptions;
    }

    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#add(fr.ensimag.ima.pseudocode.AbstractLine)
     */
    public void add(AbstractLine line) {
        program.add(line);
    }

    /**
     * @see fr.ensimag.ima.pseudocode.IMAProgram#addComment(java.lang.String)
     */
    public void addComment(String comment) {
        program.addComment(comment);
    }

    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#addLabel(fr.ensimag.ima.pseudocode.Label)
     */
    public void addLabel(Label label) {
        program.addLabel(label);
    }

    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#addInstruction(fr.ensimag.ima.pseudocode.Instruction)
     */
    public void addInstruction(Instruction instruction) {
        program.addInstruction(instruction);
    }

    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#addInstruction(fr.ensimag.ima.pseudocode.Instruction,
     * java.lang.String)
     */
    public void addInstruction(Instruction instruction, String comment) {
        program.addInstruction(instruction, comment);
    }

    public void addFirstInstruction(Instruction instruction) {
        program.addFirst(instruction);
    }

    public IMAProgram getProgram() {
        return program;
    }
    public void setProgram(IMAProgram program) {
        this.program = program;
    }
    
    /**
     * @see 
     * fr.ensimag.ima.pseudocode.IMAProgram#display()
     */
    public String displayIMAProgram() {
        return program.display();
    }


    
    private final CompilerOptions compilerOptions;
    private final File source;
    /**
     * The main program. Every instruction generated will eventually end up here.
     */
    private IMAProgram program = new IMAProgram();

    /** The global environment for types (and the symbolTable) */
    public final SymbolTable symbolTable = new SymbolTable();

    public final EnvironmentType environmentType = new EnvironmentType(this);

    public Symbol createSymbol(String name) {
        return symbolTable.create(name);
    }

    public EnvironmentExp getEnvExp() {
        return environmentType.envExp; // Assumes envExp is initialized and managed
    }
    /**
     * Run the compiler (parse source file, generate code)
     *
     * @return true on error
     */
    public boolean compile() {
        String sourceFile = source.getAbsolutePath();
        String destFile = null;

        //Precondition: file ends with .deca
        if (sourceFile.endsWith(".deca")) {
            destFile = sourceFile.substring(0, sourceFile.lastIndexOf(".")) + ".ass";
        } else {
            LOG.fatal("File type is not compatible");
            return false;
        }
        PrintStream err = System.err;
        PrintStream out = System.out;
        LOG.debug("Compiling file " + sourceFile + " to assembly file " + destFile);
        try {
            return doCompile(sourceFile, destFile, out, err);
        } catch (LocationException e) {
            e.display(err);
            return true;
        } catch (DecacFatalError e) {
            err.println(e.getMessage());
            return true;
        } catch (StackOverflowError e) {
            LOG.debug("stack overflow", e);
            err.println("Stack overflow while compiling file " + sourceFile + ".");
            return true;
        } catch (Exception e) {
            LOG.fatal("Exception raised while compiling file " + sourceFile
                    + ":", e);
            err.println("Internal compiler error while compiling file " + sourceFile + ", sorry.");
            return true;
        } catch (AssertionError e) {
            LOG.fatal("Assertion failed while compiling file " + sourceFile
                    + ":", e);
            err.println("Internal compiler error while compiling file " + sourceFile + ", sorry.");
            return true;
        }
    }

    /**
     * Internal function that does the job of compiling (i.e. calling lexer,
     * verification and code generation).
     *
     * @param sourceName name of the source (deca) file
     * @param destName name of the destination (assembly) file
     * @param out stream to use for standard output (output of decac -p)
     * @param err stream to use to display compilation errors
     *
     * @return true on error
     */
    private boolean doCompile(String sourceName, String destName,
            PrintStream out, PrintStream err)
            throws DecacFatalError, LocationException, DoubleDefException, CLIException {
        AbstractProgram prog = doLexingAndParsing(sourceName, err);

        if (prog == null) {
            LOG.info("Parsing failed");
            return true;
        }
        assert(prog.checkAllLocations());
        String fileName = sourceName.substring(sourceName.lastIndexOf('/') + 1); // Récupère le texte après le dernier '/'
        
        if (this.compilerOptions.getParseStop()) {
            System.out.println("//----------------------------"+fileName+"------------------------------\n");
            System.out.println(prog.decompile());
            return false;
        }
        prog.verifyProgram(this);
        if(this.compilerOptions.getVerificationStop()){
            return false;
        }
        assert(prog.checkAllDecorations());
            
        this.noCheck=compilerOptions.getNoCheck();

        prog.codeGenProgram(this);
        LOG.debug("Generated assembly code:" + nl + program.display());
        LOG.info("Output file assembly file is: " + destName);

        FileOutputStream fstream = null;
        try {
            fstream = new FileOutputStream(destName);
        } catch (FileNotFoundException e) {
            throw new DecacFatalError("Failed to open output file: " + e.getLocalizedMessage());
        }

        LOG.info("Writing assembler file ...");

        program.display(new PrintStream(fstream));
        LOG.info("Compilation of " + sourceName + " successful.");
        return false;
    }

    /**
     * Build and call the lexer and parser to build the primitive abstract
     * syntax tree.
     *
     * @param sourceName Name of the file to parse
     * @param err Stream to send error messages to
     * @return the abstract syntax tree
     * @throws DecacFatalError When an error prevented opening the source file
     * @throws DecacInternalError When an inconsistency was detected in the
     * compiler.
     * @throws LocationException When a compilation error (incorrect program)
     * occurs.
     */
    protected AbstractProgram doLexingAndParsing(String sourceName, PrintStream err)
            throws DecacFatalError, DecacInternalError {
        DecaLexer lex;
        try {
            lex = new DecaLexer(CharStreams.fromFileName(sourceName));
        } catch (IOException ex) {
            throw new DecacFatalError("Failed to open input file: " + ex.getLocalizedMessage());
        }
        lex.setDecacCompiler(this);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        DecaParser parser = new DecaParser(tokens);
        parser.setDecacCompiler(this);
        return parser.parseProgramAndManageErrors(err);
    }

    private Label pile_pleine = new Label("pile_pleine");
    private Label divZero = new Label("division_zero");
    private Label modZero = new Label("mod_zero");
    private Label overflowFloat = new Label("overflow_float");
    private Label readError = new Label("ErreurLecture");
    private Label heapOverflow = new Label("heap_overflow");
    private Label missingReturn = new Label("missing_return");
    private Label listIndexError = new Label("index_list_problem");
    private Label listNegativeIndex = new Label("index_list_nagative");
    private Label allocationProblem  = new Label("allocation_problem");

    public Label getDerefNull() {
        return derefNull;
    }

    public void setDerefNull(Label derefNull) {
        this.derefNull = derefNull;
    }

    private Label derefNull = new Label("derefNull");

    public Label getPile_pleine() {
        return pile_pleine;
    }

    public void setPile_pleine(Label pile_pleine) {
        this.pile_pleine = pile_pleine;
    }

    public Label getDivZero() {
        return divZero;
    }

    public void setDivZero(Label divZero) {
        this.divZero = divZero;
    }

    public Label getModZero() {
        return modZero;
    }

    public void setModZero(Label modZero) {
        this.modZero = modZero;
    }

    public Label getOverflowFloat() {
        return overflowFloat;
    }

    public void setOverflowFloat(Label overflowFloat) {
        this.overflowFloat = overflowFloat;
    }

    public Label getReadError() {
        return readError;
    }

    public void setReadError(Label readError) {
        this.readError = readError;
    }

    public Label getIndexProblem(){
        return listIndexError;
    }

    public Label getNegativeIndexProblem(){
        return listNegativeIndex;
    }

    public Label getAllocationProblem(){
        return allocationProblem;
    }

    public Label getHeapOverflow() {
        return heapOverflow;
    }

    public void setHeapOverflow(Label heapOverflow) {
        this.heapOverflow = heapOverflow;
    }

    public Label getMissingReturn() {
        return missingReturn;
    }

    public void setMissingReturn(Label missingReturn) {
        this.missingReturn = missingReturn;
    }
}
