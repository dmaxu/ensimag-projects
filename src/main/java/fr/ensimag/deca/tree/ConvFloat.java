package fr.ensimag.deca.tree;


import org.apache.log4j.Logger;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.FloatType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;

/**
 * Conversion of an int into a float. Used for implicit conversions.
 * 
 * @author gl47
 * @date 01/01/2025
 */
public class ConvFloat extends AbstractUnaryExpr {
    private static final Logger LOG = Logger.getLogger(Main.class);

    public ConvFloat(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) {
                // A FAIRE:
                LOG.debug("verifyExpr ConvFloat: start");

                // On construit le type.
                FloatType typeFloat = new FloatType(compiler.symbolTable.create("float"));
                // On preserve le type
                this.setType(typeFloat);
                // On retourne le type
                LOG.debug("verifyExpr ConvFloat: end");

                return typeFloat;
    }


    @Override
    protected String getOperatorName() {
        return "/* conv float */";
    }

    protected void codeGenInst(DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGenInst ConvFloat: start");

        getOperand().codeGenInst(compiler);
        compiler.addInstruction(new FLOAT(compiler.getMemoryManager().getLastRegister(compiler), compiler.getMemoryManager().getRegister(compiler)) );
        LOG.debug("codeGenInst ConvFloat: end");
    }
}
