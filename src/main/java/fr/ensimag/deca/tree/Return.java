package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.log4j.Logger;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.RTS;

/**
 * Cette classe nous permet de vérifie si les identificateurs en retourne valide des conditions de contexte.
 * 
 * 
 * @author gl47
 * @date 28/12/024
 */

 // A FAIRE :

public class Return extends AbstractInst{
    private AbstractExpr returnedExpr;
    private static final Logger LOG = Logger.getLogger(Main.class);
    public Return(AbstractExpr returnedExpr){
        this.returnedExpr = returnedExpr;
    }

    public AbstractExpr getExpr(){
        return this.returnedExpr;
    }

    public void setExpr(AbstractExpr returnedExpr){
        this.returnedExpr = returnedExpr;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGenInst Return: start");
        returnedExpr.codeGenInst(compiler);
        compiler.addInstruction(new LOAD(compiler.getMemoryManager().getLastRegister(compiler), Register.R0));
        compiler.getMemoryManager().restoreRegisters(compiler);
        compiler.addInstruction(new RTS());
        LOG.debug("codeGenInst Return: end");
    }


    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType) throws ContextualError{
                LOG.debug("verifyInst Return: start");
                // EnvironmentType :
                EnvironmentType environmentType = compiler.environmentType;
                // Vérification du type du retourn.
                if (returnType.isVoid()){
                    throw new ContextualError("return ne peut pas étre utiliser à l'intérieure d'une instruction de type void." , getLocation());
                }
                // Vérification si le returnType est null
                Type exprType = this.returnedExpr.verifyExpr(compiler, localEnv, currentClass);

                // teste si exprTpe est un sous type de typeDefReturned.getType():
                if (!exprType.isSousType(environmentType, returnType)&& !(exprType.isInt() && returnType.isFloat())&& !(exprType.isNull())){
                    throw new ContextualError("On ne peut pas retourner à " + exprType.getName() + " un type " + returnType.getName() + ". " , getLocation());
                }
                // Vérification
                if (exprType.isInt() && returnType.isFloat()){
                    // On convertit le int à un float
                    ConvFloat exprFloat = new ConvFloat(this.returnedExpr);
                    // Vérification
                    exprFloat.verifyExpr(compiler, localEnv, currentClass);
                    // On sauvegrade la nouvelle expression
                    setExpr(exprFloat);
                }
                LOG.debug("verifyInst Return: end");
            }
    
    @Override
    public void decompile(IndentPrintStream s) {
        s.print("return ");
        getExpr().decompileInst(s);
    }
    @Override
    protected void iterChildren(TreeFunction f) {
        returnedExpr.iter(f);
    }
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        returnedExpr.prettyPrint(s, prefix, true);
    }
}
