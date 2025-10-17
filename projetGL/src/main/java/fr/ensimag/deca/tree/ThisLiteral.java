package fr.ensimag.deca.tree;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

import java.io.PrintStream;
import java.util.Collections;

import org.apache.log4j.Logger;

public class ThisLiteral extends AbstractExpr{
    private static final Logger LOG = Logger.getLogger(Main.class);
    public Object getValue(){
        return null;
    }
    
    @Override
    public boolean isThisLiteral(){
        return true;
    }

    public ThisLiteral(){

    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
        ClassDefinition currentClass) throws ContextualError {
            LOG.debug("verifyExpr ThisLiteral: start");
            // On vérifie que CurrentClass n'est pas null
            if (currentClass != null){
                // On construit le type de la classe.
                Type thisType = currentClass.getType();
                // On preserve le type
                setType(thisType);
                LOG.debug("verifyExpr ThisLiteral: end");
                // On retourne le type
                return thisType;
            }
            throw new ContextualError("l'identificateur THis est utilisé seulement à l'intérieur d'une classe. " , getLocation());
    }
   
    @Override
    String prettyPrintNode() {
        return "This";
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("this");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    protected void codeGenInst(DecacCompiler compiler) throws CLIException{
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), compiler.getMemoryManager().getRegister(compiler)));
    }

    // A FAIRE A FAIRE

    protected VariableDefinition getVariableDefinition(){
        // A FAIRE : attention
        VariableDefinition temp = new VariableDefinition(this.getType(),this.getLocation(),new ListExpr());
        temp.setOperand(new RegisterOffset(-2, Register.LB));
        return temp;
    }

}
