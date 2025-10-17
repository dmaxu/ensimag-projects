package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.tree.TableLiteral;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class TableLiteral extends AbstractExpr {

    // Initialisation de la liste avec une valeur modifiable
    private ListExpr taille;


    @Override
    public boolean isTableLiteral() {
        return true;
    }

    // Méthode qui conserve la taille
    public void setTaille(ListExpr taille){
        this.taille = taille;
    }

    // On retourne la taille
    public ListExpr getTaille(){
        return this.taille;
    }

    @Override
    protected ListExpr getTailleExpr(DecacCompiler compiler, EnvironmentExp localEnv,
        ClassDefinition currentClass) throws ContextualError{
        // le cas d'une TableauElement
        // On retourne la taille
        TableLiteral tableauExpr = (TableLiteral) this;
        // On retourne la taille
        return tableauExpr.getTaille();
    }

    @Override
    protected void setTailleExpr(DecacCompiler compiler, EnvironmentExp localEnv,
        ClassDefinition currentClass, ListExpr newTaille) throws ContextualError{
        // On retourne la taille
        TableLiteral tableauExpr = (TableLiteral) this;
        // ON preserve la nouvelle taille
        tableauExpr.setTaille(newTaille);
    }
}
