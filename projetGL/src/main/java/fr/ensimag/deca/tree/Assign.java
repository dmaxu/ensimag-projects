package fr.ensimag.deca.tree;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.codegen.LabelManager;
import fr.ensimag.deca.context.*;

import java.nio.channels.UnsupportedAddressTypeException;
import java.util.List;

import org.apache.log4j.Logger;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;

/**
 * Assignment, i.e. lvalue = expr.
 *
 * @author gl47
 * @date 01/01/2025
 */
public class Assign extends AbstractBinaryExpr {
    private static final Logger LOG = Logger.getLogger(Main.class);

    @Override
    public AbstractLValue getLeftOperand() {
        // The cast succeeds by construction, as the leftOperand has been set
        // as an AbstractLValue by the constructor.
        return (AbstractLValue) super.getLeftOperand();
    }

    public Assign(AbstractLValue leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        // A FAIRE :
        LOG.debug("verifyExpr Assign: start");

        EnvironmentType environmentType = compiler.environmentType;
        // le Type de opérande droite
        AbstractExpr rightOperand = this.getRightOperand();
        Type rightType = rightOperand.verifyExpr(compiler, localEnv, currentClass);
        // le Type de opérande gauche
        AbstractExpr leftOperand = this.getLeftOperand();
        Type leftType = leftOperand.verifyExpr(compiler, localEnv, currentClass);
        // teste si rightType est un sous type de leftType:
        if (!rightType.isSousType(environmentType, leftType) && !(rightType.isInt() && leftType.isFloat())) {
            throw new ContextualError("On ne peut pas affectée à " + leftType.getName() + " un type " + rightType.getName() + ". ", getLocation());
        }
        // On preserve le dynamicType
        leftOperand.setDynamicType(leftType, localEnv);
        // Au cas d'une liste
        if (rightType.isTable() || leftType.isTable()) {
            ListExpr listRightExpr = getRightOperand().getTailleExpr(compiler, localEnv, currentClass);
            if (getLeftOperand().getTailleExpr(compiler, localEnv, currentClass).size() != listRightExpr.size()) {
                throw new ContextualError("Probléme dans les tailles des listes.", getLocation());
            }
            // On preserve la taille
            else {
                getLeftOperand().setTailleExpr(compiler, localEnv, currentClass, listRightExpr);
            }
        }
        // On vérifie si rightOperand est de type int et leftOperand est de type float
        if (rightType.isInt() && leftType.isFloat()) {
            // Construction de convfloat
            ConvFloat newRightOperand = new ConvFloat(rightOperand);
            // on vérifie l'expression de convFloat
            newRightOperand.verifyExpr(compiler, localEnv, currentClass);
            // On modéfie l'opérand à droite
            setRightOperand(newRightOperand);
        }
        // on insére le type de l'assigne
        this.setType(leftType);
        LOG.debug("verifyExpr Assign: end");

        return leftType;
    }

    @Override
    protected String getOperatorName() {
        return "=";
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGen Assign Inst: start");

        //:Disjonction des cas
        if (getLeftOperand().isIdentifier()) {
            // Évaluer l'opérande de droite et stocker le résultat dans un registre
            getRightOperand().codeGenInst(compiler);
            // Register
            GPRegister regRight = compiler.getMemoryManager().getLastRegister(compiler);
            compiler.getMemoryManager().markUsedRegister(regRight.getNumber());
            if (((AbstractIdentifier) this.getLeftOperand()).getDefinition().isField()) {
                compiler.addInstruction(new LOAD(((FieldDefinition) ((AbstractIdentifier) this.getLeftOperand()).getDefinition()).getOperand(), compiler.getMemoryManager().getRegister(compiler)));
                int idx = ((FieldDefinition) ((AbstractIdentifier) this.getLeftOperand()).getDefinition()).getIndex();
                compiler.addInstruction(new STORE(regRight, new RegisterOffset(idx, compiler.getMemoryManager().getLastRegister(compiler))));
            } else {
                // Générer l'instruction STORE pour stocker la valeur de regRight dans regLeft
                compiler.addInstruction(new STORE(regRight, ((AbstractIdentifier) getLeftOperand()).getExpDefinition().getOperand()));
                compiler.getMemoryManager().markUsedRegister(regRight.getNumber());
            }

        } else if (getLeftOperand().isSelection()) {
            ((Selection) getLeftOperand()).getLeftExpr().codeGenInst(compiler);
            // Évaluer l'opérande de droite et stocker le résultat dans un registre
            getRightOperand().codeGenInst(compiler);
            // Registeres
            GPRegister regRight = compiler.getMemoryManager().getLastRegister(compiler);
            GPRegister regLeft = compiler.getMemoryManager().getLastRegister(compiler);
            if (!compiler.getCompilerOptions().getNoCheck()) {
                compiler.addInstruction(new CMP(new NullOperand(), regLeft));
                compiler.addInstruction(new BEQ(compiler.getDerefNull()));
            }
            // On retourne l'index de l'attribut
            int idx = ((Selection) getLeftOperand()).getRightIdent().getFieldDefinition().getIndex();
            // On modéfie l'attribut
            compiler.addInstruction(new STORE(regRight, new RegisterOffset(idx, regLeft)));
            // On marque le regitser de l'opérand à droite
            compiler.getMemoryManager().markUsedRegister(regRight.getNumber());
        } else if (getLeftOperand().isListElement()) {
            ((ListElement) getLeftOperand()).setGetAddress(true);
            getLeftOperand().codeGenInst(compiler);
            getRightOperand().codeGenInst(compiler);

            GPRegister right = compiler.getMemoryManager().getLastRegister(compiler);
            GPRegister left = compiler.getMemoryManager().getLastRegister(compiler);

            compiler.addInstruction(new STORE(right, new RegisterOffset(0, left)));
        }
        LOG.debug("codeGen Assign Inst: end");


    }

    @Override
    protected void codeGenCond(boolean cond, Label label, DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGen Assign Cond: start");
        // Générez d'abord codeGenInst pour produire 0 ou 1 dans un registre
        this.codeGenInst(compiler);
        // Récupérer le registre qui contient la valeur de l'expression (b = false) => 0 ou 1
        GPRegister reg = compiler.getMemoryManager().getLastRegister(compiler);

        // On compare reg à 0
        compiler.addInstruction(new CMP(0, reg));

        // Si on veut "sauter" quand c'est false => cond = false
        // => on fait BEQ label (branch if equal to 0)
        if (!cond) {
            compiler.addInstruction(new BEQ(label));
        } else {
            // sinon, si cond = true => BNE label (branch si != 0)
            compiler.addInstruction(new BNE(label));
        }
        LOG.debug("codeGen Assign Cond: end");

    }

}
