package fr.ensimag.deca.tree;


import org.apache.log4j.Logger;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.SLT;
import fr.ensimag.ima.pseudocode.instructions.SNE;

/**
 *
 * @author gl47
 * @date 01/01/2025
 */
public class NotEquals extends AbstractOpExactCmp {
    private static final Logger LOG = Logger.getLogger(Main.class);
    public NotEquals(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "!=";
    }

    protected void codeGenCond(boolean cond, Label label, DecacCompiler compiler) throws CLIException{
        LOG.debug("verifyClassMembers: start");
        new Equals(this.getLeftOperand(),this.getRightOperand()).codeGenCond(!cond, label, compiler);
        LOG.debug("verifyClassMembers: end");
    }

//    // Case where it's an assign and we need to do boolean algebra (not a condition)
//    @Override
//    protected void codeGenInst(DecacCompiler compiler) throws CLIException {
//        // Évaluer la première sous-expression
//        getLeftOperand().codeGenInst(compiler);
//
//
//        // Évaluer la deuxième sous-expression
//        getRightOperand().codeGenInst(compiler);
//
//
//        GPRegister regLeft = compiler.getMemoryManager().getLastRegister(compiler);
//        GPRegister regRight = compiler.getMemoryManager().getLastRegister(compiler);
//        compiler.addInstruction(new CMP(regLeft, regRight));
//        compiler.addInstruction(new SNE(compiler.getMemoryManager().getRegister(compiler)));
//
//    }

}
