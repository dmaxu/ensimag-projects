package fr.ensimag.deca.tree;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.BooleanType;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

import java.io.PrintStream;

import org.apache.log4j.Logger;

/**
 * @author gl47
 * @date 01/01/2025
 */
public class BooleanLiteral extends AbstractExpr {
    private static final Logger LOG = Logger.getLogger(Main.class);

    private boolean value;

    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                           ClassDefinition currentClass) throws ContextualError {
        // on construit le type boolean
        LOG.debug("verifyExpr BooleanLiteral: start");

        BooleanType booleanType = new BooleanType(compiler.symbolTable.create("boolean"));
        // on preserve le type de l'expression
        this.setType(booleanType);
        LOG.debug("verifyExpr BooleanLiteral: end");
        return booleanType;
    }


    @Override
    public void decompile(IndentPrintStream s) {
        s.print(Boolean.toString(value));
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    String prettyPrintNode() {
        return "BooleanLiteral (" + value + ")";
    }

    protected void codeGenCond(boolean cond, Label label, DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGen BooleanLiteral Cond: start");
        if (this.getValue()) {
            if (cond)
                compiler.addInstruction(new BRA(label));
        } else {
            (new Not(new BooleanLiteral(true))).codeGenCond(cond, label, compiler);
        }
        LOG.debug("codeGen BooleanLiteral Cond: end");

    }

//    protected void codeGenInst(DecacCompiler compiler) throws CLIException {
//        compiler.addInstruction(new LOAD(this.getValue() ? 1 : 0, compiler.getMemoryManager().getRegister(compiler)));
//    }
}
