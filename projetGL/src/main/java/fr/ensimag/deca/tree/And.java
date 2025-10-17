package fr.ensimag.deca.tree;



import org.apache.log4j.Logger;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.LabelManager;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.SEQ;

/**
 * @author gl47
 * @date 01/01/2025
 */
public class And extends AbstractOpBool {
    private static final Logger LOG = Logger.getLogger(Main.class);

    public And(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "&&";
    }


    protected void codeGenCond(boolean cond, Label label, DecacCompiler compiler) throws CLIException {
        LOG.debug("CodeGen And: start");

        if (cond) {
            Label E_Fin = compiler.getLabelManager().getLabel("E_Fin");
            getLeftOperand().codeGenCond(false, E_Fin, compiler);
            getRightOperand().codeGenCond(true, label, compiler);
            compiler.addLabel(E_Fin);
        } else {
            getLeftOperand().codeGenCond(false, label, compiler);
            getRightOperand().codeGenCond(false, label, compiler);
        }
        LOG.debug("CodeGen And: end");

    }

//    // Case where it's an assign and we need to do boolean algebra (not a condition)
//    @Override
//    protected void codeGenInst(DecacCompiler compiler) throws CLIException{
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
//        // verify both equal
//        compiler.addInstruction(new CMP(regLeft, regRight));
//        compiler.addInstruction(new SEQ(Register.R0));
//        // verify equal 1
//        compiler.addInstruction(new CMP(new ImmediateInteger(1), regRight));
//        compiler.addInstruction(new SEQ(Register.R1));
//        compiler.addInstruction(new CMP(Register.R1,Register.R0));
//
//        compiler.addInstruction(new SEQ(compiler.getMemoryManager().getRegister(compiler)));
//
//    }

}
