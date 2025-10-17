package fr.ensimag.deca.tree;

import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.FloatType;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;

import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * Single precision, floating-point literal
 *
 * @author gl47
 * @date 01/01/2025
 */
public class FloatLiteral extends AbstractExpr {
    private static final Logger LOG = Logger.getLogger(Main.class);

    public float getValue() {
        return value;
    }

    private float value;

    public FloatLiteral(float value) {
        Validate.isTrue(!Float.isInfinite(value),
                "literal values cannot be infinite");
        Validate.isTrue(!Float.isNaN(value),
                "literal values cannot be NaN");
        this.value = value;
    }


    @Override
    public boolean isImmediate(){
        return true;
    }

    @Override
    public DVal getAdresse(){
        return new ImmediateFloat(value);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
                LOG.debug("verifyExpr FloatLiteral: start");
                // On construit le type float.
                FloatType floatType = new FloatType(compiler.symbolTable.create("float"));
                // On preserve le type de l'expression.
                setType(floatType);
                LOG.debug("verifyExpr FloatLiteral: end");
                return floatType;      
    }


    @Override
    public void decompile(IndentPrintStream s) {
        s.print(java.lang.Float.toHexString(value));
    }

    @Override
    String prettyPrintNode() {
        return "Float (" + getValue() + ")";
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    protected void codeGenInst(DecacCompiler compiler) {
        LOG.debug("codeGenInst FloatLiteral: start");

        ImmediateFloat val = new ImmediateFloat(this.getValue());
        compiler.addInstruction(new LOAD(val, compiler.getMemoryManager().getRegister(compiler)));
        LOG.debug("codeGenInst FloatLiteral: end");
    }

    @Override
    public boolean isLiteral(){
        return true;
    }

}
