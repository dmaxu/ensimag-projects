package fr.ensimag.deca.tree;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.*;

import java.io.PrintStream;

import org.apache.log4j.Logger;

import static fr.ensimag.ima.pseudocode.Register.R0;

public class InstanceOf extends AbstractExpr {
    private AbstractExpr leftExpression;
    private AbstractIdentifier rightExpression;
    private static final Logger LOG = Logger.getLogger(Main.class);

    public InstanceOf(AbstractExpr leftExpression, AbstractIdentifier rightExpression) {
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
    }

    /**
     * @param instanceOf : une variable qui sauvegarde si leftExpression est de classe rightExpression
     */

    private boolean intanceOf;

    /**
     * Cette méthode retourne true si leftExpression est de classe rightExpression ou false sinon.
     *
     * @return
     */
    public boolean isSousClass() {
        return this.intanceOf;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler,
                           EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        LOG.debug("verifyExpr InstanceOf: start");

        // retourne le type de l'expression à gauche.
        Type typeLeftExpr = this.leftExpression.verifyExpr(compiler, localEnv, currentClass);
        Symbol symbolTypeLeftExpr = typeLeftExpr.getName();
        // retourne le type de l'expression à droite.
        Symbol symbolTypeRightExpr = this.rightExpression.getName();
        // initialization null:
        ClassDefinition classDefLeftExpr = null;
        ClassDefinition classDefRightExpr = null;
        try {
            // retourne la classDefinition de l'expression à gauche.
            classDefLeftExpr = (ClassDefinition) compiler.environmentType.defOfType(symbolTypeLeftExpr);
            // retourne la classDefinition de l'expression à droite.
            classDefRightExpr = (ClassDefinition) compiler.environmentType.defOfType(symbolTypeRightExpr);
        } catch (ClassCastException e) {
            throw new ContextualError("On utilise InstanceOf sur des classes qui sont définies dans l'environmentType. ", getLocation());
        }
        // On vérifie si classDefLeftExpr et une sous classes de  classDefRightExpr
        if (classDefLeftExpr.isSubClass(classDefRightExpr)) {
            this.intanceOf = true;
        }
        // Vérification
        if (this.leftExpression.isIdentifier()){
            // Transtypage
            AbstractIdentifier identLeft = (AbstractIdentifier) this.leftExpression;
            // On retourne le type dynamique
            Type dynamicTypeLeftExpr = identLeft.getDynamicType(localEnv);
            // Vérifcation
            if (dynamicTypeLeftExpr.isSousType(compiler.environmentType, classDefRightExpr.getType())){
                this.intanceOf = true;
            }
        }
        // On preserve le type de l'expression à droite
        this.rightExpression.setDefinition(classDefRightExpr);
        // on preserve le type boolean:
        BooleanType typeInstanceOf = new BooleanType(compiler.symbolTable.create("boolean"));
        // On preserve le type
        setType(typeInstanceOf);
        LOG.debug("verifyExpr InstanceOf: end");

        // On retourne le type.
        return typeInstanceOf;
    }

    @Override
    public void decompile(IndentPrintStream s) {
    // Décompiler l'expression gauche (leftExpression)
    leftExpression.decompile(s);
    
    // Ajouter "instanceOf" entre les deux expressions
    s.print(" instanceof ");
    
    // Décompiler l'expression droite (rightExpression)
    rightExpression.decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        this.leftExpression.iterChildren(f);
        this.rightExpression.iterChildren(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        this.leftExpression.prettyPrintChildren(s, prefix);
        this.rightExpression.prettyPrintChildren(s, prefix);
    }

    @Override
    String prettyPrintNode() {
        return this.leftExpression.prettyPrintNode() + " instanceOf " + this.rightExpression.prettyPrintNode();
    }

    @Override
    protected void codeGenCond(boolean cond, Label label, DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGenCond InstanceOf: start");
        new BooleanLiteral(this.intanceOf).codeGenCond(cond,label,compiler);
        LOG.debug("codeGenCond InstanceOf: end");

    }
}
