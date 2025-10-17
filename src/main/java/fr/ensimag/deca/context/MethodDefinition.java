package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import org.apache.commons.lang.Validate;
import java.util.List;
import fr.ensimag.deca.tree.ListExpr;

/**
 * Definition of a method
 *
 * @author gl47
 * @date 01/01/2025
 */
public class MethodDefinition extends ExpDefinition {

    @Override
    public boolean isMethod() {
        return true;
    }

    public Label getLabel() {
        Validate.isTrue(label != null,
                "setLabel() should have been called before");
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public int getIndex() {
        return index;
    }

    private int index;

    @Override
    public MethodDefinition asMethodDefinition(String errorMessage, Location l)
            throws ContextualError {
        return this;
    }

    private  ListExpr params;
    private final Signature signature;
    private Label label;
    private EnvironmentExp localEnvMethod;

    public void setListParamExpr(ListExpr params){
        this.params = params;
    }

    public ListExpr getListParamExpr(){
        return this.params;
    }

    public void setLocalEnvMethod(EnvironmentExp localEnvMethod){
        this.localEnvMethod = localEnvMethod;
    }

    public EnvironmentExp getLocalEnvMethod(){
        return this.localEnvMethod;
    }

    /**
     * 
     * @param type Return type of the method
     * @param location Location of the declaration of the method
     * @param signature List of arguments of the method
     * @param index Index of the method in the class. Starts from 0.
     */
    public MethodDefinition(Type type, ListExpr taille,Location location, Signature signature, int index) {
        super(type, taille,location);
        this.signature = signature;
        this.index = index;
    }

    private Symbol className;

    public void setClassIn(Symbol className){
        this.className = className;
    }

    public Symbol getClassIn(){
        return this.className;
    }

    private boolean isAdded = false;

    public void setIsAdded(){
        this.isAdded = true;
    }
    public boolean isAdded(){
        return this.isAdded;
    }

    public void setIndex(int index){
        this.index = index;
    }

    public Signature getSignature() {
        return signature;
    }

    @Override
    public String getNature() {
        return "method";
    }

    @Override
    public boolean isExpression() {
        return false;
    }

}
