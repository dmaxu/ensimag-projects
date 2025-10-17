package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;

import org.apache.log4j.Logger;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.instructions.*;

/**
 *
 * @author gl47
 * @date 01/01/2025
 */
public class Modulo extends AbstractOpArith {
    private static final Logger LOG = Logger.getLogger(Main.class);
    public Modulo(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
                LOG.debug("verifyExpr Modulo: start");

                // on retourne le type de l'opérand à gauche
                Type typeLeftOperand = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
                // On returne le type de l'opérand à droite
                Type typeRightOperand = getRightOperand().verifyExpr(compiler, localEnv, currentClass);
                // On vérifie le type de l'opérand à gauche
                if (!typeLeftOperand.isInt()){
                    throw new ContextualError("L'opérand à gauche doit étre de type Int et pas de type " + typeLeftOperand.getName() + "." , getLocation());
                }
                // On vérifie le type de l'opérand à droite
                if (!typeRightOperand.isInt()){
                    throw new ContextualError("L'opérand à droite doit étre de type Int et pas de type " + typeRightOperand.getName() + "." , getLocation());
                }
                // On preserve le type
                setType(typeLeftOperand);
                LOG.debug("verifyExpr Modulo: end");
                // On retourne le type
                return typeLeftOperand;
    }

    @Override
    protected String getOperatorName() {
        return "%";
    }

     /**
     * Cette méthode génére le code du calcule du reste
     * @param compiler
     * @param regLeft
     * @param regRight
     * @param isFloat
     * 
     */
    protected void codeGenModulo(DecacCompiler compiler, DVal regLeft, GPRegister regRight, boolean isFloat){
        LOG.debug("codeGenModulo: start");
        // On ne traite le modulo qu'en int => si isFloat=false, on l'applique
        if (!isFloat) {
            compiler.addInstruction(new REM(regLeft, regRight));
        }
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new BOV(compiler.getModZero()));
        }
        LOG.debug("codeGenModulo: end");
    }

}
