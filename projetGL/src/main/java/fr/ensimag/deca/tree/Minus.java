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
public class Minus extends AbstractOpArith {
    private static final Logger LOG = Logger.getLogger(Main.class);
    public Minus(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "-";
    }

     /**
     * Cette méthode génére le code de la soustraction
     * @param compiler
     * @param regLeft
     * @param regRight
     * @param isFloat
     * 
     */
    protected void codeGenMinus(DecacCompiler compiler, DVal regLeft, GPRegister regRight, boolean isFloat){
        LOG.debug("codeGenMinus: start");

        if (isFloat) {
            compiler.addInstruction(new SUB(regLeft, regRight));
            if (!compiler.getCompilerOptions().getNoCheck()) {
                compiler.addInstruction(new BOV(compiler.getOverflowFloat()));
            }
        } else {
            compiler.addInstruction(new SUB(regLeft, regRight));
        }
        LOG.debug("codeGenMinus: end");

    }
}
