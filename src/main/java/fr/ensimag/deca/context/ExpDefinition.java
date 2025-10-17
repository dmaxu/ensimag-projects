package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.deca.tree.ListExpr;
import java.util.List;

/**
 * Definition associated to identifier in expressions.
 *
 * @author gl47
 * @date 01/01/2025
 */
public abstract class ExpDefinition extends Definition {

    public void setOperand(DAddr operand) {
        this.operand = operand;
    }
    
    public boolean isMethodParameter() {
        return false;
    }

    public boolean isVariable(){
        return false;
    }

    public boolean isField(){
        return false;
    }

    public DAddr getOperand() {
        return operand;
    }
    private DAddr operand;

    private ListExpr taille;
    private int size;
    private Type dynamicType;

    public ExpDefinition(Type type, ListExpr taille, Location location) {
        super(type, location);
        this.taille = taille;
        this.dynamicType = type;
    }

    public void setDynamicType(Type dynamicType){
        this.dynamicType = dynamicType;
    }

    public Type getDynamicType(){
        return this.dynamicType;
    }

    public ListExpr getTaille(){
        return this.taille;
    }

    public void setTaille(ListExpr  newTaille){
        this.taille = newTaille;
    }
}
