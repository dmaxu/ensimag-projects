package fr.ensimag.deca.tree;

import org.apache.log4j.Logger;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.NotType;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.*;

/**
 * @author gl47
 * @date 01/01/2025
 */
public class Not extends AbstractUnaryExpr {
    private static final Logger LOG = Logger.getLogger(Main.class);
    public Not(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
                // A FAIRE:
                LOG.debug("verifyExpr NOT: start");

                // On cherche le type de l'opérand
                Type typeOperand = getOperand().verifyExpr(compiler, localEnv, currentClass);
                // le symbole du type de l'opérand
                Symbol typeOperandSymbol = typeOperand.getName();
                // On vérifie si l'opérand  a un type qu'il est différent de boolean.
                if (!typeOperandSymbol.getName().equals("boolean")){
                    throw new ContextualError("Not ne peut pas étre appliquer à une expression de type " +typeOperandSymbol.getName() + ". Applicable seulement pour les booleans." , getLocation());
                }
                // On preserve le type de l'opérand
                setType(typeOperand);
                LOG.debug("verifyExpr NOT: end");
                // On retourne le type.
                return typeOperand;
    }


    @Override
    protected String getOperatorName() {
        return "!";
    }

    protected void codeGenCond(boolean cond, Label label, DecacCompiler compiler) throws CLIException {
        this.getOperand().codeGenCond(!cond, label, compiler);
    }

    // Case where it's an assign and we need to do boolean algebra (not a condition)
    @Override
    protected void codeGenInst(DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGenInst NOT: start");
        getOperand().codeGenInst(compiler);
        GPRegister reg = compiler.getMemoryManager().getLastRegister(compiler);
        compiler.addInstruction(new LOAD(new ImmediateInteger(1), Register.R0));
        compiler.addInstruction(new SUB(reg,Register.R0));
        compiler.addInstruction(new LOAD(Register.R0,compiler.getMemoryManager().getRegister(compiler)));
        LOG.debug("codeGenInst NOT: end");

    }
}
