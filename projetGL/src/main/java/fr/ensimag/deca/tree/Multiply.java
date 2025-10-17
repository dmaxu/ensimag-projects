package fr.ensimag.deca.tree;
import org.apache.log4j.Logger;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.*;

/**
 * @author gl47
 * @date 01/01/2025
 */
public class Multiply extends AbstractOpArith {
    private static final Logger LOG = Logger.getLogger(Main.class);
    public Multiply(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "*";
    }

    /**
     * Cette méthode génére le code de la multiplication
     * @param compiler
     * @param regLeft
     * @param regRight
     * @param isFloat
     * 
     */
    protected void codeGenMultiply(DecacCompiler compiler, DVal regLeft, GPRegister regRight, boolean isFloat){
        LOG.debug("codeGenMultiply: start");
        if (isFloat) {
            compiler.addInstruction(new MUL(regLeft, regRight));
            if (!compiler.getCompilerOptions().getNoCheck()) {
                compiler.addInstruction(new BOV(compiler.getOverflowFloat()));
            }
        } else {
            if (getRightOperand().isImmediate()||getLeftOperand().isImmediate()){
                int valeur;
                boolean state = false;
                if (getRightOperand().isImmediate()){
                    valeur = Math.abs(((IntLiteral) getRightOperand()).getValue());
                }else{
                    valeur = Math.abs(((IntLiteral) getLeftOperand()).getValue());
                }
                if (valeur%2 == 1){
                    state = true;
                    compiler.addInstruction(new LOAD(regRight, Register.getR(1)));
                }
                int shift = (int) (Math.log(valeur)/Math.log(2));
                int reste = valeur - (1<<shift);
                for (int i = 0; i < shift; i++){
                    compiler.addInstruction(new SHL(regRight));
                }
                while (reste > 0){
                    shift = (int) (Math.log(reste)/Math.log(2));
                    reste = reste - (1<<shift);
                    for (int i = 0; i < shift; i++){
                        compiler.addInstruction(new SHL(regRight));
                    }
                }
                if (state){
                    compiler.addInstruction(new ADD(Register.getR(1), regRight));
                }
            }else{
                compiler.addInstruction(new MUL(regLeft, regRight));
            }
        }
        LOG.debug("codeGenMultiply: end");

    }

}
