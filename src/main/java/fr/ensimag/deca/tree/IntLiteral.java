package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.IntType;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

import java.io.PrintStream;

import org.apache.log4j.Logger;

/**
 * Integer literal
 *
 * @author gl47
 * @date 01/01/2025
 */
public class IntLiteral extends AbstractExpr {
    private static final Logger LOG = Logger.getLogger(Main.class);

    public int getValue() {
        return value;
    }

    private int value;

    public IntLiteral(int value) {
        this.value = value;
    }

    @Override
    public boolean isImmediate(){
        return true;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
                LOG.debug("verifyExpr IntLiteral: start");

                // On construit le type int.
                IntType intType = new IntType(compiler.symbolTable.create("int"));
                // On preserve le type de l'expression.
                setType(intType);
                // On retourne le type
                LOG.debug("verifyExpr IntLiteral: end");

                return intType;
    }

    public DVal getAdresse(){
        return new ImmediateInteger(value);
    }


    @Override
    String prettyPrintNode() {
        return "Int (" + getValue() + ")";
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(Integer.toString(value));
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
    protected void codeGenInst(DecacCompiler compiler) {
        LOG.debug("verifyExpr codeGenInst: start");
        compiler.addInstruction(new LOAD(this.getValue(), compiler.getMemoryManager().getRegister(compiler)));
        LOG.debug("verifyExpr codeGenInst: end");

    }

    @Override
    public boolean isLiteral(){
        return true;
    }
}
