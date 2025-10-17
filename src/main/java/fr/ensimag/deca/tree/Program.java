package fr.ensimag.deca.tree;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.LabelManager;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.IMAProgram;
import fr.ensimag.ima.pseudocode.instructions.*;

import java.io.PrintStream;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import fr.ensimag.ima.pseudocode.Label;


/**
 * Deca complete program (class definition plus main block)
 *
 * @author gl47
 * @date 01/01/2025
 */
public class Program extends AbstractProgram {
    private static final Logger LOG = Logger.getLogger(Program.class);

    public Program(ListDeclClass classes, AbstractMain main) {
        Validate.notNull(classes);  // A FAIRE : on a premiérement écrire complété la grammaire de la partie avec class.
        Validate.notNull(main);
        this.classes = classes;
        this.main = main;
    }

    public ListDeclClass getClasses() {
        return classes;
    }

    public AbstractMain getMain() {
        return main;
    }

    private ListDeclClass classes;
    private AbstractMain main;

    @Override
    public void verifyProgram(DecacCompiler compiler) throws ContextualError, DoubleDefException {
        LOG.debug("verify program: start");
        //  d'aprés la grammaire fournit dans le pdf, on commence par vérifié le non terminal : liste_classe.
        classes.verifyListClass(compiler);  //A FAIRE : on a premiérement écrire complété la grammaire de la partie avec class.
        // ON vérifié le main.
        main.verifyMain(compiler);
        LOG.debug("verify program: end");
    }


    @Override
    public void codeGenProgram(DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGenProgram: start");

        // A FAIRE: compléter ce squelette très rudimentaire de code
        classes.genVTable(compiler);
        compiler.addComment("--------------------------------------------------");
        compiler.addComment("               Start main program                 ");
        compiler.addComment("--------------------------------------------------");
        main.codeGenMain(compiler);
        compiler.addInstruction(new HALT());
        compiler.addComment("--------------------------------------------------");
        compiler.addComment("               End main program                 ");
        compiler.addComment("--------------------------------------------------");

        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addFirstInstruction(new BOV(compiler.getPile_pleine()));
        }
        
        compiler.addFirstInstruction(new ADDSP(compiler.getMemoryManager().getGlobalVar() - 1));

        // Test stack overflow
        compiler.addFirstInstruction(new TSTO(compiler.getMemoryManager().getMaxRegistersTemp() + compiler.getMemoryManager().getGlobalVar()));

        classes.codeGenObject(compiler);
        classes.codegenClasses(compiler);
        // Adding error labels
            compiler.getLabelManager().addErrorLabel(compiler.getPile_pleine(), compiler, "Erreur : Débordement de la pile");
            compiler.getLabelManager().addErrorLabel(compiler.getDivZero(), compiler, "Erreur : Division par zero");
            compiler.getLabelManager().addErrorLabel(compiler.getModZero(), compiler, "Erreur : Modulo par zero");
            compiler.getLabelManager().addErrorLabel(compiler.getReadError(), compiler, "Erreur : Erreur lecture");
            compiler.getLabelManager().addErrorLabel(compiler.getOverflowFloat(), compiler, "Erreur : Débordement flottant");
            compiler.getLabelManager().addErrorLabel(compiler.getHeapOverflow(), compiler, "Erreur : Heap overflow");
            compiler.getLabelManager().addErrorLabel(compiler.getDerefNull(), compiler, "Erreur : Déréférencement null");
            compiler.getLabelManager().addErrorLabel(compiler.getMissingReturn(), compiler, "Erreur : Return null dans fonction non void");
            compiler.getLabelManager().addErrorLabel(compiler.getIndexProblem(), compiler, "Erreur : Dépassage de la taille du tableau");
            compiler.getLabelManager().addErrorLabel(compiler.getNegativeIndexProblem(), compiler, "Erreur : Indice négative d'un tableau");
            compiler.getLabelManager().addErrorLabel(compiler.getAllocationProblem(), compiler, "Erreur : allocation avec indice <= 0");
        LOG.debug("codeGenProgram: end");

    }

    @Override
    public void decompile(IndentPrintStream s) {
        getClasses().decompile(s);
        getMain().decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        classes.iter(f);
        main.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        classes.prettyPrint(s, prefix, false); // A FAIRE :car on est encors dans la partie sans objet.
        main.prettyPrint(s, prefix, true);
    }
}
