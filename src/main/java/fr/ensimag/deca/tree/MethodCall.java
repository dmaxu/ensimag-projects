package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.log4j.Logger;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;

/**
 * @author gl47
 * @date 01/01/2025
 */

public class MethodCall extends AbstractSelectionMethodCall{
    private ListExpr listParam;
    private static final Logger LOG = Logger.getLogger(Main.class);
    public MethodCall(AbstractExpr leftExpr, AbstractIdentifier rightIdent, ListExpr listParam){
        super(leftExpr, rightIdent);
        this.listParam = listParam;
    }
    /**
     * Récupère la liste des expressions.
     * 
     * @return la liste actuelle des expressions
     */
    public ListExpr getListParam(){
        return this.listParam;
    }
    
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
        ClassDefinition currentClass) throws ContextualError{
            LOG.debug("verifyExpr MethodCall: start");

            // on retourne le type de la classe + vérification de l'expression à gauche
            Type typeClass =  getLeftExpr().verifyExpr(compiler, localEnv, currentClass);
            // condition sur le type
            if (!typeClass.isClass()){
                throw new ContextualError("Invalide Type! (" + typeClass.getName()+") , l'utilisation des méthodes est valable seulement pour les classes. " , getLeftExpr().getLocation());
            }
            // On retourne la définition de la classe ou  la méthode est définie
            ClassDefinition classDefinition = (ClassDefinition) compiler.environmentType.defOfType(typeClass.getName()); 
            // on retourne l'environment locale à l'intérieur de la classe:
            EnvironmentExp localEnvClass = classDefinition.getMembers();
            // On retourne la définition de la méthode
            ExpDefinition expDefinition = localEnvClass.get(getRightIdentifier().getName());
            if ((expDefinition != null) && expDefinition.isMethod()){
                // Transtypage
                MethodDefinition methodDefinition = (MethodDefinition) expDefinition;
                // Type de la méthode:
                Type typeMethod = methodDefinition.getType();
                // On vérifie la liste des paramétres en entré :
                if (this.listParam.size() !=0){
                    Signature signatureParam = new Signature();
                    // On retourne la signature de la méthode.
                    Signature signatureMethod = methodDefinition.getSignature();
                    // Vérification de la taille
                    if (signatureMethod.size()==0){
                        throw new ContextualError("la méthode " + getRightIdentifier().getName() + " ne prend pas de paramétre. ", getLocation());
                    }
                    // index 
                    int index = 0;
                    // Passage sur les expressions
                    for (AbstractExpr expr : getListParam().getList()){
                        // On retourne le type de l'expression:
                        Type typeExpr = expr.verifyExpr(compiler, localEnv, currentClass);
                        // Vérification
                        if (typeExpr.isInt() && signatureMethod.getTypeElement(index).isFloat()){
                            // ConvFloat
                            ConvFloat exprConvFloat = new ConvFloat(expr);
                            // Vérification du convFloat
                            typeExpr = exprConvFloat.verifyExpr(compiler, localEnv, currentClass);
                            // On change l'expression dans listParam
                            getListParam().set(index, exprConvFloat);
                        }
                        if (typeExpr.isTable()){
                            // On ajoute la taille
                            methodDefinition.getListParamExpr().get(index).setTailleExpr(compiler, methodDefinition.getLocalEnvMethod(), currentClass, expr.getTailleExpr(compiler, localEnv, currentClass));
                        }
                        // On ajoute le type :
                        signatureParam.add(typeExpr);
                        // On incrémente l'index
                        index++;
                    }
                    // Comparaison entre les deux signatures:
                    if (!signatureMethod.equals(signatureParam)){
                        throw new ContextualError(" Un probléme dans les parametres d'entrée de la méthode " + getRightIdentifier().getName() + ". \n"+
                        "\t\tAttendue : " + signatureMethod.toString() + "\n" +
                        "\t\tPrésenté : " + signatureParam.toString() + "\n"
                        ,getLocation()
                        );
                    }
                    
                }
                // On preserve la définition de la méthode
                getRightIdentifier().setDefinition(methodDefinition);
                // On preserve le type de la définition
                getRightIdentifier().setType(typeMethod);
                // On preserve le type.
                setType(typeMethod);
                // On retourne le
                LOG.debug("verifyExpr MethodCall: end");
                return typeMethod;
            }
            throw new ContextualError("la méthode " + getRightIdentifier().getName() + " n'est pas définie à l'intérieure de la classe " + typeClass.getName() +". " , getLeftExpr().getLocation());
        }

    @Override
    String prettyPrintNode() {
        return  "MethodCall ";
    }

    @Override
    public void decompile(IndentPrintStream s) {
        getLeftExpr().decompile(s);
        s.print(".");
        getRightIdentifier().decompile(s);
        s.print("(");
        this.listParam.decompile(s);
        s.print(")");
        s.print(";");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // A FAIRE : probléme seulement dans l'affichage prettyPrint.
        getLeftExpr().prettyPrint(s, prefix, false);
        getRightIdentifier().prettyPrint(s, prefix, false);
        if (listParam != null){
            listParam.prettyPrint(s, prefix, true);
        }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) throws CLIException{
        LOG.debug("codeGenInst MethodCall: start");

        compiler.addInstruction(new ADDSP(listParam.size() + 1));
        int index = getRightIdentifier().getMethodDefinition().getIndex() + 1 ; //+1 because object.equals always there
        if (!getLeftExpr().isIdentifier()){
            getLeftExpr().codeGenInst(compiler); //Cas New directement
            compiler.addInstruction(new LOAD(compiler.getMemoryManager().getLastRegister(compiler),compiler.getMemoryManager().getRegister(compiler)));

        }
        else {
            DAddr objAddr = ((Identifier) getLeftExpr()).getExpDefinition().getOperand();

            // On empile le paramètre implicite
            compiler.addInstruction(new LOAD(objAddr, compiler.getMemoryManager().getRegister(compiler)));
        }
        int offset = 0;
        compiler.addInstruction(new STORE(compiler.getMemoryManager().getLastRegister(compiler),new RegisterOffset(offset, Register.SP)));
        // On empile les paramètres
        for (AbstractExpr param : getListParam().getList()){
            offset--;
            param.codeGenInst(compiler);
            compiler.addInstruction(new STORE(compiler.getMemoryManager().getLastRegister(compiler),new RegisterOffset(offset, Register.SP)));
        }
        //On récupère le paramètre implicite
        compiler.addInstruction(new LOAD(new RegisterOffset(0,Register.SP),compiler.getMemoryManager().getRegister(compiler)));
        GPRegister reg = compiler.getMemoryManager().getLastRegister(compiler);
        compiler.getMemoryManager().markUsedRegister(reg.getNumber());
        compiler.addInstruction(new CMP(new NullOperand(),reg));
        compiler.addInstruction(new BEQ(compiler.getDerefNull()));

        //On récupère l'adresse de la table des méthodes
        compiler.addInstruction(new LOAD(new RegisterOffset(0,compiler.getMemoryManager().getLastRegister(compiler)),compiler.getMemoryManager().getRegister(compiler)));
        compiler.addInstruction(new BSR(new RegisterOffset(index,compiler.getMemoryManager().getLastRegister(compiler))));
        compiler.addInstruction(new SUBSP(listParam.size() + 1));

        //On sauvegarde la valeur retourné dans R0 si la fonction n'est pas void
        if (!getRightIdentifier().getMethodDefinition().getType().getName().getName().equals("void")){
            compiler.addInstruction(new LOAD(Register.R0,compiler.getMemoryManager().getRegister(compiler)));
        }
        LOG.debug("codeGenInst MethodCall: end");

    }

    @Override
    protected void codeGenCond(boolean cond, Label label, DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGenCond MethodCall: start");

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
        LOG.debug("codeGenCond MethodCall: start");

    }
}