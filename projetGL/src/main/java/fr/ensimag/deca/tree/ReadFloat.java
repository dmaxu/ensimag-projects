package fr.ensimag.deca.tree;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.FloatType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.RFLOAT;

import java.io.PrintStream;

import org.apache.log4j.Logger;

/**
 *
 * @author gl47
 * @date 01/01/2025
 */
public class ReadFloat extends AbstractReadExpr {
    private static final Logger LOG = Logger.getLogger(Main.class);
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
                LOG.debug("verifyExpr ReadFloat: start");
                //  Cette méthode crée un type float dans le compiler , et on le retourne 
                FloatType floatType = new FloatType(compiler.symbolTable.create("float"));
                // on preserve le type de l'expression ReadFloat.
                this.setType(floatType);
                LOG.debug("verifyExpr ReadFloat: end");
                return floatType;
    }


    @Override
    public void decompile(IndentPrintStream s) {
        s.print("readFloat()");
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
    protected void codeGenInst(DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGenInst ReadFloat: start");
        compiler.addInstruction(new RFLOAT());
        compiler.addInstruction(new LOAD(Register.R1, compiler.getMemoryManager().getRegister(compiler)));
        super.codeGenInst(compiler);
        LOG.debug("codeGenInst ReadFloat: end");
    }

}
