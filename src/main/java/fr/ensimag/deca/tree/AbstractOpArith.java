package fr.ensimag.deca.tree;

import org.apache.log4j.Logger;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.*;

/**
 * Arithmetic binary operations (+, -, /, ...)
 *
 * @author gl47
 * @date 01/01/2025
 */
public abstract class AbstractOpArith extends AbstractBinaryExpr {
    private static final Logger LOG = Logger.getLogger(Main.class);


    public AbstractOpArith(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                           ClassDefinition currentClass) throws ContextualError {
        LOG.debug("verify Arith Expr: start");

        // A FAIRE : on a ajouté ce commentaire pour connaitre les fichiers que on a modifié par une simple grep -r "A FAIRE"
        // l'opérand à droite
        AbstractExpr leftOp = getRightOperand();
        // l'opérand à gauche
        AbstractExpr rightOp = getLeftOperand();
        // le type de l'opérand à droite
        Type typeLeftOperand = leftOp.verifyExpr(compiler, localEnv, currentClass);
        leftOp.setType(typeLeftOperand);
        // le type de l'opérand à gauche
        Type typeRightOperand = rightOp.verifyExpr(compiler, localEnv, currentClass);
        rightOp.setType(typeRightOperand);
        // le nom de l'opérand
        String operateur = getOperatorName();
        // Vérification des types
        if ((typeLeftOperand.isBoolean()) || typeRightOperand.isBoolean()) {
            // aucune operation arithmetique pour les booleans
            throw new ContextualError("l'operation " + operateur + " n'est ps définies pour les booleans !", getLocation());
        }
        if ((typeLeftOperand.isString()) || typeRightOperand.isString()) {
            // aucune operation arithmetique pour les booleans
            throw new ContextualError("l'operation " + operateur + " n'est pas définies pour les strings !", getLocation());
        }
        if (typeLeftOperand.isFloat() && typeRightOperand.isInt()) {
            // on vérifie si on a une opération entre un int et un float
            // on convertit la type de rightOp pour qu'il soit un float
            ConvFloat conIntFloat = new ConvFloat(rightOp);
            conIntFloat.verifyExpr(compiler, localEnv, currentClass);
            setLeftOperand(conIntFloat);
            // on preserve le type
            setType(typeLeftOperand);
            return typeLeftOperand;
        }
        if (typeLeftOperand.isInt() && typeRightOperand.isFloat()) {
            // on vérifie si on a une opération entre un int et un float
            // on convertit la type de rightOp pour qu'il soit un float
            ConvFloat conIntFloat = new ConvFloat(leftOp);
            conIntFloat.verifyExpr(compiler, localEnv, currentClass);
            setRightOperand(conIntFloat);
            // on preserve le type
            setType(typeRightOperand);
            return typeRightOperand;
        }
        // on preserve le type
        setType(typeRightOperand);
        LOG.debug("verify Arith Expr: end");
        return typeRightOperand;
    }


    /**
     * Génération de code : constant folding (calculs statique avant code gen) si possible, sinon code classique.
     */
    @Override
    public void codeGenInst(DecacCompiler compiler) throws CLIException {
        LOG.debug("CodeGen Arith Inst: start");

        // Récupère opérandes et opérateur
        AbstractExpr leftOp = getLeftOperand();
        AbstractExpr rightOp = getRightOperand();
        String operateur = getOperatorName();

        // Tente un constant folding (ex. int + int, float / float, etc.)
        AbstractExpr folded = tryComputeConstant(operateur, leftOp, rightOp);
        if (folded != null) {
            // On a obtenu un littéral simplifié => on génère seulement un LOAD
            folded.codeGenInst(compiler);
            return;
        }

        boolean isFloat = leftOp.getType().isFloat() || rightOp.getType().isFloat();

        if (leftOp.isImmediate()&& (!isFloat)&& (operateur.equals("*"))){
            AbstractExpr inter = leftOp;
            leftOp = rightOp;
            rightOp = inter;
        }
        Type leftType = leftOp.getType();
        Type rightType = rightOp.getType();
        
        //rightOp.codeGenInst(compiler);
        //GPRegister regLeft = compiler.getMemoryManager().getLastRegister(compiler);

        DVal adresse;
        GPRegister regRight;
        if (rightOp.isImmediate()&& !isFloat){
            // Sinon => code normal
            leftOp.codeGenInst(compiler);
            regRight = compiler.getMemoryManager().getLastRegister(compiler);
            adresse = ((IntLiteral) rightOp).getAdresse();
        }else{
            rightOp.codeGenInst(compiler);
            // Sinon => code normal
            leftOp.codeGenInst(compiler);
            regRight = compiler.getMemoryManager().getLastRegister(compiler);
            adresse = compiler.getMemoryManager().getLastRegister(compiler);
        }

        // Convertit en float si besoin
        if (isFloat) {
            if (leftType.isInt()) {
                compiler.addInstruction(new FLOAT((GPRegister)adresse, (GPRegister)adresse));
            }
            if (rightType.isInt()) {
                compiler.addInstruction(new FLOAT(regRight, regRight));
            }
        }

        // Applique l'opérateur en code IMA
        switch (operateur) {
            case "+":
                Plus instructionPLUS = (Plus) this;
                instructionPLUS.codeGenPlus(compiler, adresse, regRight, isFloat);
                break;

            case "-":
                Minus instructionMINUS= (Minus) this;
                instructionMINUS.codeGenMinus(compiler, adresse, regRight, isFloat);
                break;

            case "*":
                Multiply instructionMultiply = (Multiply) this;
                instructionMultiply.codeGenMultiply(compiler,adresse, regRight, isFloat);
                break;

            case "/":
                Divide instructionDivide = (Divide) this;
                instructionDivide.codeGenDivide(compiler, adresse, regRight, isFloat);
                break;    
            case "%":
                // On ne traite le modulo qu'en int => si isFloat=false, on l'applique
                Modulo instructionModule = (Modulo) this;
                instructionModule.codeGenModulo(compiler, adresse, regRight, isFloat);
                break;
        }

        // Mise à jour
        compiler.getMemoryManager().markUsedRegister(regRight.getNumber());
        LOG.debug("CodeGen Arith Inst: end");

    }

    /**
     * Fonction auxiliaire : constant folding.
     * Retourne un nouveau littéral (IntLiteral ou FloatLiteral) si on peut simplifier,
     * ou null si on ne peut pas.
     */
    private AbstractExpr tryComputeConstant(String operateur, AbstractExpr leftOp, AbstractExpr rightOp) {
        // Vérifier si les deux opérandes sont littéraux
        if (!leftOp.isLiteral() || !rightOp.isLiteral()) {
            return null;
        }

        // Récupère les valeurs
        boolean leftIsInt = leftOp.getType().isInt();
        boolean rightIsInt = rightOp.getType().isInt();

        switch (operateur) {
            case "+":
                return foldPlus(leftOp, rightOp, leftIsInt, rightIsInt);
            case "-":
                return foldMinus(leftOp, rightOp, leftIsInt, rightIsInt);
            case "*":
                return foldMultiply(leftOp, rightOp, leftIsInt, rightIsInt);
            case "/":
                return foldDivide(leftOp, rightOp, leftIsInt, rightIsInt);
            case "%":
                return foldModulo(leftOp, rightOp, leftIsInt, rightIsInt);
            default:
                return null;
        }
    }

    /**
     * Constant folding pour "+".
     */
    private AbstractExpr foldPlus(AbstractExpr leftOp, AbstractExpr rightOp,
                                  boolean leftIsInt, boolean rightIsInt) {
        // int + int
        if (leftIsInt && rightIsInt) {
            int vG = ((IntLiteral) leftOp).getValue();
            int vD = ((IntLiteral) rightOp).getValue();
            return new IntLiteral(vG + vD);
        }
        // float + float
        else if (leftOp.getType().isFloat() && rightOp.getType().isFloat()) {
            float vG = ((FloatLiteral) leftOp).getValue();
            float vD = ((FloatLiteral) rightOp).getValue();
            return new FloatLiteral(vG + vD);
        }
        // mix
        else {
            float vG = leftIsInt ?
                (float)((IntLiteral) leftOp).getValue() :
                ((FloatLiteral) leftOp).getValue();
            float vD = rightIsInt ?
                (float)((IntLiteral) rightOp).getValue() :
                ((FloatLiteral) rightOp).getValue();
            return new FloatLiteral(vG + vD);
        }
    }

    /**
     * Constant folding pour "-".
     */
    private AbstractExpr foldMinus(AbstractExpr leftOp, AbstractExpr rightOp,
                                   boolean leftIsInt, boolean rightIsInt) {
        // int - int
        if (leftIsInt && rightIsInt) {
            int vG = ((IntLiteral) leftOp).getValue();
            int vD = ((IntLiteral) rightOp).getValue();
            return new IntLiteral(vG - vD);
        }
        // float - float
        else if (leftOp.getType().isFloat() && rightOp.getType().isFloat()) {
            float vG = ((FloatLiteral) leftOp).getValue();
            float vD = ((FloatLiteral) rightOp).getValue();
            return new FloatLiteral(vG - vD);
        }
        // mix
        else {
            float vG = leftIsInt ?
                (float)((IntLiteral) leftOp).getValue() :
                ((FloatLiteral) leftOp).getValue();
            float vD = rightIsInt ?
                (float)((IntLiteral) rightOp).getValue() :
                ((FloatLiteral) rightOp).getValue();
            return new FloatLiteral(vG - vD);
        }
    }

    /**
     * Constant folding pour "*".
     */
    private AbstractExpr foldMultiply(AbstractExpr leftOp, AbstractExpr rightOp,
                                      boolean leftIsInt, boolean rightIsInt) {
        // int * int
        if (leftIsInt && rightIsInt) {
            int vG = ((IntLiteral) leftOp).getValue();
            int vD = ((IntLiteral) rightOp).getValue();
            return new IntLiteral(vG * vD);
        }
        // float * float
        else if (leftOp.getType().isFloat() && rightOp.getType().isFloat()) {
            float vG = ((FloatLiteral) leftOp).getValue();
            float vD = ((FloatLiteral) rightOp).getValue();
            return new FloatLiteral(vG * vD);
        }
        // mix
        else {
            float vG = leftIsInt ?
                (float)((IntLiteral) leftOp).getValue() :
                ((FloatLiteral) leftOp).getValue();
            float vD = rightIsInt ?
                (float)((IntLiteral) rightOp).getValue() :
                ((FloatLiteral) rightOp).getValue();
            return new FloatLiteral(vG * vD);
        }
    }

    /**
     * Constant folding pour "/".
     * Attention division par zéro (int ou float).
     * On laisse le code normal gerer le cas x / 0 meme si on pourra le traiter ici.
     */
    private AbstractExpr foldDivide(AbstractExpr leftOp, AbstractExpr rightOp,
                                    boolean leftIsInt, boolean rightIsInt) {
        // int / int
        if (leftIsInt && rightIsInt) {
            int vG = ((IntLiteral) leftOp).getValue();
            int vD = ((IntLiteral) rightOp).getValue();
            if (vD == 0) {
                return null; // on ne simplifie pas -> code normal gère l'erreur
            }
            return new IntLiteral(vG / vD);
        }
        // float / float
        else if (!leftIsInt && !rightIsInt) {
            float vG = ((FloatLiteral) leftOp).getValue();
            float vD = ((FloatLiteral) rightOp).getValue();
            if (vD == 0.0f) {
                return null;
            }
            return new FloatLiteral(vG / vD);
        }
        // mix
        else {
            float vG = leftIsInt ?
                (float)((IntLiteral) leftOp).getValue() :
                ((FloatLiteral) leftOp).getValue();
            float vD = rightIsInt ?
                (float)((IntLiteral) rightOp).getValue() :
                ((FloatLiteral) rightOp).getValue();
            if (vD == 0.0f) {
                return null;
            }
            return new FloatLiteral(vG / vD);
        }
    }

    /**
     * Constant folding pour "%".
     * On ne gère que int % int (Deca ne définit pas le modulo en float).
     * On laisse le code normal gerer le cas x % 0 meme si on pourra le traiter ici.
     */
    private AbstractExpr foldModulo(AbstractExpr leftOp, AbstractExpr rightOp,
                                    boolean leftIsInt, boolean rightIsInt) {
        // int % int uniquement
        if (leftIsInt && rightIsInt) {
            int vG = ((IntLiteral) leftOp).getValue();
            int vD = ((IntLiteral) rightOp).getValue();
            if (vD == 0) {
                return null; // laisse le code normal gérer l'erreur
            }
            return new IntLiteral(vG % vD);
        }
        // Sinon on ne fait pas de folding
        return null;
    }

}
