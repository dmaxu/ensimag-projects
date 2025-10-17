package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import org.apache.log4j.Logger;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.BooleanType;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 *
 * @author gl47
 * @date 01/01/2025
 */
public abstract class AbstractOpCmp extends AbstractBinaryExpr {
    private static final Logger LOG = Logger.getLogger(Main.class);

    public AbstractOpCmp(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
                LOG.debug("verify Oper Comp Expr: start");

                // retourne le type de l'opérande à gauche
                Type leftOpType = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
                // retourne le type de l'opérande à droite
                Type rightOpType = getRightOperand().verifyExpr(compiler, localEnv, currentClass);
                // On retourne le nom de l'opérateur utilisée:
                String operateur = getOperatorName();
                // On construit le type boolean.
                BooleanType booleanType = new BooleanType(compiler.symbolTable.create("boolean"));
                // on vérifie si les types des opérandes sont int ou float ou les deux.
                if((leftOpType.isInt() || leftOpType.isFloat()) && (rightOpType.isInt() || rightOpType.isFloat())){
                    if (leftOpType.isInt() && rightOpType.isFloat()){
                        // Construction de convFloat
                        ConvFloat newLeftOperand = new ConvFloat(getLeftOperand());
                        // On vérifie l'expression de convFloat
                        newLeftOperand.verifyExpr(compiler, localEnv, currentClass);
                        // on modéfie l'opérand à gauche
                        setLeftOperand(newLeftOperand);
                    }
                    else if (leftOpType.isFloat() && rightOpType.isInt()){
                        // Construction de convFloat
                        ConvFloat newRightOperand = new ConvFloat(getRightOperand());
                        // On vérifie l'expression de convFloat
                        newRightOperand.verifyExpr(compiler, localEnv, currentClass);
                        // on modéfie l'opérand à gauche
                        setRightOperand(newRightOperand);
                    }
                    // on préserve le type de l'expression
                    setType(booleanType);
                    return booleanType;
                }else if(!(leftOpType.isSousType(compiler.environmentType, rightOpType)||rightOpType.isSousType(compiler.environmentType, leftOpType))){
                    throw new ContextualError("Les opérandes doivent étre de méme type. Impossible de comparé entre " + leftOpType.getName() + " et " + rightOpType.getName() + ".", getLocation());
                }
                // Teste:
                if (operateur.equals(">=") || operateur.equals("<=") || operateur.equals(">") || operateur.equals("<")){
                    throw new ContextualError("Il est impossible d'effectuer l'opération " + operateur + " à des opérandes de types différents de int ou float.", getLocation());
                }
                // on préserve le type de l'expression
                setType(booleanType);
                LOG.debug("verify Oper Comp Expr: end");
                return booleanType;
    }
}
