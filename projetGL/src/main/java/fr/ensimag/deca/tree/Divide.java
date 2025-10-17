package fr.ensimag.deca.tree;

import org.apache.log4j.Logger;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.*;
/**
 *
 * @author gl47
 * @date 01/01/2025
 */
public class Divide extends AbstractOpArith {
    private static final Logger LOG = Logger.getLogger(Main.class);

    public Divide(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "/";
    }

    /**
     * Cette méthode génére le code de la division
     * @param compiler
     * @param regLeft
     * @param regRight
     * @param isFloat
     * 
     */
    protected void codeGenDivide(DecacCompiler compiler, DVal regLeft, GPRegister regRight, boolean isFloat){
        LOG.debug("codeGenDivide: start");

        if (isFloat) {
            compiler.addInstruction(new DIV(regLeft, regRight));
            if (!compiler.getCompilerOptions().getNoCheck()) {
                compiler.addInstruction(new BOV(compiler.getOverflowFloat()));
            }
        } else {
            if (getRightOperand().isImmediate()){
                int valeur_reel = ((IntLiteral) getRightOperand()).getValue();
                int valeur = Math.abs(valeur_reel);
                int sign = (int) Math.signum(valeur_reel);
                int shift = (int) (Math.log(valeur)/Math.log(2));
                int reste = valeur - (1<<shift);
                if (reste == 0){
                    for (int i = 0; i < shift; i++){
                        compiler.addInstruction(new SHR(regRight));
                    }
                    if (sign == -1){
                        compiler.addInstruction(new OPP(regRight, regRight));
                    }
                }else{
                    compiler.addInstruction(new QUO(regLeft, regRight));
                }
            }else{
                compiler.addInstruction(new QUO(regLeft, regRight));
            }
        }
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new BOV(compiler.getDivZero()));
        }
        LOG.debug("codeGenDivide: end");

    }

}
