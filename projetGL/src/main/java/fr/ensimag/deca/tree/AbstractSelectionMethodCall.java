package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ExpDefinition;
import java.util.List;

import fr.ensimag.deca.context.Type;

public abstract class AbstractSelectionMethodCall extends AbstractLValue{

    private AbstractExpr leftExpr;
    private AbstractIdentifier rightIdent;

    public AbstractSelectionMethodCall(AbstractExpr leftExpr, AbstractIdentifier rightIdent){
        this.rightIdent = rightIdent;
        this.leftExpr = leftExpr;
    }

    public AbstractIdentifier getRightIdentifier(){
        return this.rightIdent;
    }

    public AbstractExpr getLeftExpr(){
        return this.leftExpr;
    }

    @Override
    public boolean isSelectionCall(){
        return true;
    }

    @Override
    protected ListExpr getTailleExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError{
            // Transtypage
            AbstractSelectionMethodCall  methodCall = (AbstractSelectionMethodCall) this;
            // On retourne l'identificateur
            AbstractIdentifier identifier = methodCall.getRightIdentifier();
            // On retourne l'identificateur
            AbstractExpr leftExpr = methodCall.getLeftExpr();
            // On retourne le type
            Type leftType = leftExpr.verifyExpr(compiler, localEnv, currentClass);
            // Transtypage de la class
            ClassDefinition leftClassDef = (ClassDefinition) compiler.environmentType.defOfType(leftType.getName());
            // On retourne l'environment de la classe
            EnvironmentExp localEnvClass = leftClassDef.getMembers();
            // On retourne la définition
            ExpDefinition exprDefinition = localEnvClass.get(identifier.getName());
            // On retourne la taille
            return exprDefinition.getTaille();
    }

    @Override
    protected void setTailleExpr(DecacCompiler compiler, EnvironmentExp localEnv,
        ClassDefinition currentClass, ListExpr newTaille) throws ContextualError{
        // Transtypage
        AbstractSelectionMethodCall  methodCall = (AbstractSelectionMethodCall) this;
        // On retourne l'identificateur
        AbstractIdentifier identifier = methodCall.getRightIdentifier();
        // On retourne l'identificateur
        AbstractExpr leftExpr = methodCall.getLeftExpr();
        // On retourne le type
        Type leftType = leftExpr.verifyExpr(compiler, localEnv, currentClass);
        // Transtypage de la class
        ClassDefinition leftClassDef = (ClassDefinition) compiler.environmentType.defOfType(leftType.getName());
        // On retourne l'environment de la classe
        EnvironmentExp localEnvClass = leftClassDef.getMembers();
        // On retourne la définition
        ExpDefinition exprDefinition = localEnvClass.get(identifier.getName());
        // ON preserve la taille
        exprDefinition.setTaille(newTaille);
    }
}