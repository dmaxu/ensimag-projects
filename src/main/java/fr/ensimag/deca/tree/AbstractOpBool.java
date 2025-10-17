package fr.ensimag.deca.tree;

import org.apache.log4j.Logger;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

/**
 *
 * @author gl47
 * @date 01/01/2025
 */
public abstract class AbstractOpBool extends AbstractBinaryExpr {
    private static final Logger LOG = Logger.getLogger(Main.class);

    public AbstractOpBool(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    // A FAIRE : probléme : boolean x = !false && true;
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
                LOG.debug("verify Bool Expr: start");

                // on retourne le type de l'opérand à gauche.
                Type typeLeftOp = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
                String typeLeftOpName = typeLeftOp.getName().getName();
                // on vérifie si le type de l'opérand à gauche est boolean.
                if (!typeLeftOpName.equals("boolean")){
                    // On enléve une erreur.
                    throw new ContextualError("le type de l'opérande à gauche n'est pas un boolean. " , getLeftOperand().getLocation());
                }
                // on retourne le type de l'opérand à droite.
                Type typeRightOp = getRightOperand().verifyExpr(compiler, localEnv, currentClass);
                String typeRightOpName = typeRightOp.getName().getName();
                // on vérifie si le type de l'opérand à droite est boolean.
                if (!typeRightOpName.equals("boolean")){
                    // On enléve une erreur.
                    throw new ContextualError("le type de l'opérand à droite n'est pas un boolean. " , getRightOperand().getLocation());
                }
                // on preserve le type de l'expression
                setType(typeLeftOp);
                LOG.debug("verify Bool Expr: end");

                return typeLeftOp;
    }









}
