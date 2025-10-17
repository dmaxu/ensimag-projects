package fr.ensimag.deca.tree;
import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.context.VoidType;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;

import fr.ensimag.ima.pseudocode.instructions.BOV;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * @author gl47
 * @date 01/01/2025
 */
public class Main extends AbstractMain {
    private static final Logger LOG = Logger.getLogger(Main.class);
    
    private ListDeclVar declVariables;
    private ListInst insts;
    public Main(ListDeclVar declVariables,
            ListInst insts) {
        Validate.notNull(declVariables);
        Validate.notNull(insts);
        this.declVariables = declVariables;
        this.insts = insts;
    }

    @Override
    protected void verifyMain(DecacCompiler compiler) throws ContextualError, DoubleDefException {
        LOG.debug("verify Main: start");
        // On a ajouté cette ligne pour la déclaration des variables
        this.declVariables.verifyListDeclVariable(compiler, compiler.getEnvExp(), null);
        // On vérifie Listinst.
        this.insts.verifyListInst(compiler,compiler.getEnvExp() , null, new VoidType(compiler.symbolTable.create("void")));
        LOG.debug("verify Main: end");
    }

    @Override
    protected void codeGenMain(DecacCompiler compiler) throws CLIException {

        // A FAIRE: traiter les déclarations de variables.
        LOG.debug("codeGenMain Main: start");
        compiler.addComment("Variable declarations:");
        declVariables.codeGenProgram(compiler);
        compiler.addComment("End declarations:");
        compiler.addComment("Insts:");
        insts.codeGenListInst(compiler);
        compiler.addComment("End insts:");
        LOG.debug("codeGenMain Main: end");
    }
    
    @Override
    public void decompile(IndentPrintStream s) {
        s.println("{");
        s.indent();
        declVariables.decompile(s);
        insts.decompile(s);
        s.unindent();
        s.println("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        declVariables.iter(f);
        insts.iter(f);
    }
 
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        declVariables.prettyPrint(s, prefix, false); 
        insts.prettyPrint(s, prefix, true);
    }
}
