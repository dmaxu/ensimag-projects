package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.ClassDefinition;
import java.util.List;

public abstract class TableauElement extends AbstractLValue{
    
    private ListExpr taille;

    public void setTaille(ListExpr taille){
        this.taille = taille;
    }

    public ListExpr getTaille(){
        return this.taille;
    }

    @Override
    public boolean isTableauElement(){
        return true;
    }

    @Override
    protected ListExpr getTailleExpr(DecacCompiler compiler, EnvironmentExp localEnv,
        ClassDefinition currentClass) throws ContextualError{
        // On retourne la taille
        TableauElement tableauElement = (TableauElement) this;
        return tableauElement.getTaille();
    }
    @Override
    protected void setTailleExpr(DecacCompiler compiler, EnvironmentExp localEnv,
        ClassDefinition currentClass, ListExpr newTaille) throws ContextualError{
        // On retourne la taille
        TableauElement tableauElement = (TableauElement) this;
        // On modéfie la taille
        tableauElement.setTaille(newTaille);
    }
}
