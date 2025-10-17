package fr.ensimag.deca.tree;

import org.apache.log4j.Logger;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.UnaryMinusType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.OPP;

/**
 * @author gl47
 * @date 01/01/2025
 */
public class UnaryMinus extends AbstractUnaryExpr {
    private static final Logger LOG = Logger.getLogger(Main.class);
    public UnaryMinus(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
                // on retourne le type de l'opérand, car la type va resté le méme.
            LOG.debug("verifyExpr UnaryMinus: start");
                // On retourne le type del'expression
                Type operandType = getOperand().verifyExpr(compiler, localEnv, currentClass);
                // On vérifie que le type est un int ou un float.
                if (operandType.isInt() || operandType.isFloat()){
                    // on preserve le type
                    setType(operandType);
                    LOG.debug("verifyExpr UnaryMinus: end");
                    return operandType;
                }
                throw new ContextualError("Type invalide! (" + operandType.getName() +"), valide pour un type int ou un type float. " , getLocation() );
    }


    @Override
    protected String getOperatorName() {
        return "-";
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGenInst UnaryMinus: start");

        getOperand().codeGenInst(compiler);

        GPRegister reg = compiler.getMemoryManager().getLastRegister(compiler);
        compiler.addInstruction(new OPP(reg,compiler.getMemoryManager().getRegister(compiler)));
        LOG.debug("codeGenInst UnaryMinus: end");
    }

}
