package fr.ensimag.deca.tree;


import org.apache.log4j.Logger;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.ADD;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.SEQ;
import fr.ensimag.ima.pseudocode.instructions.SGE;

/**
 * @author gl47
 * @date 01/01/2025
 */
public class Or extends AbstractOpBool {
    private static final Logger LOG = Logger.getLogger(Main.class);

    public Or(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "||";
    }


    protected void codeGenCond(boolean cond, Label label, DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGenCond OR: start");
        (new Not(new And(new Not(getLeftOperand()), new Not(getRightOperand())))).codeGenCond(cond, label, compiler);
        LOG.debug("codeGenCond OR: end");

    }

//    // Case where it's an assign and we need to do boolean algebra (not a condition)
//    @Override
//    protected void codeGenInst(DecacCompiler compiler) throws CLIException {
//
//        // Évaluer la première sous-expression
//        getLeftOperand().codeGenInst(compiler);
//
//
//        // Évaluer la deuxième sous-expression
//        getRightOperand().codeGenInst(compiler);
//
//        GPRegister regLeft = compiler.getMemoryManager().getLastRegister(compiler);
//        GPRegister regRight = compiler.getMemoryManager().getLastRegister(compiler);
//
//
//        // verify sum is bigger than 1 (means that at least a 1 exists)
//        compiler.addInstruction(new ADD(regLeft,regRight));
//        compiler.addInstruction(new CMP(new ImmediateInteger(1),regRight));
//        compiler.addInstruction(new SGE(compiler.getMemoryManager().getRegister(compiler)));
//    }

}
