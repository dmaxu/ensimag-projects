package fr.ensimag.deca.tree;

import org.apache.log4j.Logger;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.instructions.*;

/**
 * @author gl47
 * @date 01/01/2025
 */
public class Plus extends AbstractOpArith {
    private static final Logger LOG = Logger.getLogger(Main.class);

    public Plus(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }
 

    @Override
    protected String getOperatorName() {
        return "+";
    }

    /**
     * Cette méthode génére le code de l'addition
     * @param compiler
     * @param regLeft
     * @param regRight
     * @param isFloat
     * 
     */
    protected void codeGenPlus(DecacCompiler compiler, DVal regLeft, GPRegister regRight, boolean isFloat){
        LOG.debug("codeGenPlus: start");

        if (isFloat) {
            compiler.addInstruction(new ADD(regLeft, regRight));
            if (!compiler.getCompilerOptions().getNoCheck()) {
                compiler.addInstruction(new BOV(compiler.getOverflowFloat()));
            }
        } else {
            compiler.addInstruction(new ADD(regLeft, regRight));
        }
        LOG.debug("codeGenPlus: end");

    }
}
