package fr.ensimag.deca.tree;


import java.io.PrintStream;
import java.util.List;

import org.apache.log4j.Logger;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.tree.AbstractIdentifier;
import fr.ensimag.deca.tree.ListExpr;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Type;

import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * Cette méthode nous permet de vérifie l'application des méthode : f() (à l'intérieure des classes)
 * @author : gl47
 * @date : 02/01/2025
 * 
 */

public class MethodSansThis extends AbstractExpr{
    private AbstractIdentifier method;
    private ListExpr signatureExpr;
    private static final Logger LOG = Logger.getLogger(Main.class);
    public MethodSansThis(AbstractIdentifier method, ListExpr signature){
        this.method = method;
        this.signatureExpr = signature;
    }

    public AbstractIdentifier getRightIdentifier(){
        return this.method;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler,
        EnvironmentExp localEnv, ClassDefinition currentClass)
        throws ContextualError{
            LOG.debug("verifyExpr MethodSansThis: start");

            // On vérifie si currentClass est null.
            if (currentClass == null){
                throw new ContextualError("Expression Invalide. Valide seulement à'l'intérieure d'une classe. " , getLocation());
            }
            // On retourne la classDéfinition
            EnvironmentExp localClassEnv = currentClass.getMembers();
            // On retourne ExpDefinition
            ExpDefinition  expDefinition = localClassEnv.get(this.method.getName());
            // On vérifie si expDefinition est une méthode
            if (expDefinition == null || !expDefinition.isMethod()){
                throw new ContextualError(" La méthode utilisé sous le nom " + method.getName() + " n'est pas définie dans l'environment. " , getLocation());
            }
            // On fait un transtypage
            MethodDefinition methodDefinition = (MethodDefinition) expDefinition;
            //Type de la méthode
            Type typeMethode = methodDefinition.getType();
            // On retourne la signature de la méthode.
            Signature signatureMethod = methodDefinition.getSignature();
            // On construit une nouvelle signtaure.
            Signature signatureEntered = new Signature();
            // index
            int index = 0;
            // une boucle sur les expression
            for (AbstractExpr expr : this.signatureExpr.getList()){
                // On retourne le type de l'expression
                Type typeExp = expr.verifyExpr(compiler, localEnv, currentClass);
                // Vérification
                if (typeExp.isInt() && signatureMethod.getTypeElement(index).isFloat()){
                    // ConvFloat
                    ConvFloat exprConvFloat = new ConvFloat(expr);
                    // Vérifiaction de l'expression
                    typeExp = exprConvFloat.verifyExpr(compiler, localEnv, currentClass);
                    // On change l'expression
                    this.signatureExpr.set(index, exprConvFloat);
                }
                // On l'ajoute à signature
                signatureEntered.add(typeExp);
                // Incrémente l'indice
                index++;
            }
            // On compare entre les signatures
            if (!signatureEntered.equals(signatureMethod)){
                throw new ContextualError("probléme dans les paramétre d'entrée de la méthode " + method.getName() + ". " , getLocation());
            }
            // On preserve le type de la méthode
            setType(typeMethode);
            // On preserve la définition
            this.method.setDefinition(methodDefinition);
            // On preserve le type
            this.method.setType(typeMethode);
            LOG.debug("verifyExpr MethodSansThis: end");
            // On retourne le type
            return typeMethode;
        }

    @Override
    public void decompile(IndentPrintStream s) {
        method.decompile(s);
        s.print("(");
        if (signatureExpr != null){
            signatureExpr.decompile(s);
        }
        s.print(")");
        s.print(" ");
        s.print(";");
    }
    

    @Override
    protected void iterChildren(TreeFunction f) {
        method.iter(f);
        if (signatureExpr != null){
            signatureExpr.iter(f);
        }
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        method.prettyPrint(s, prefix, false); 
        if (signatureExpr != null){
            signatureExpr.prettyPrint(s, prefix, true);  
        }
    }

    @Override
    protected ListExpr getTailleExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError{
                // On retourne la taille.
            return getRightIdentifier().getTailleExpr(compiler, localEnv, currentClass);
        }
    
    @Override
    protected void setTailleExpr(DecacCompiler compiler, EnvironmentExp localEnv,
    ClassDefinition currentClass, ListExpr newTaille) throws ContextualError{
        // On preserve la nouvelle taille
        getRightIdentifier().setTailleExpr(compiler, localEnv, currentClass, newTaille);
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) throws CLIException{
        LOG.debug("codeGenInst MethodSansThis: start");
        MethodCall methodCall = new MethodCall(new ThisLiteral(),method,signatureExpr);
        methodCall.setLocation(getLocation());
        methodCall.codeGenInst(compiler);
        LOG.debug("codeGenInst MethodSansThis: end");

    }
}
