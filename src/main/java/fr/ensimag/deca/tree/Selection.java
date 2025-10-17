package fr.ensimag.deca.tree;

import java.io.PrintStream;
import java.lang.reflect.Field;

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
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;

/**
 * @author gl47
 * @date 05/01/2025
 * 
 */

public class Selection extends AbstractSelectionMethodCall{
    private static final Logger LOG = Logger.getLogger(Main.class);

    public Selection(AbstractExpr leftExpr, AbstractIdentifier rightIdent){
        super(leftExpr, rightIdent);
    }

    @Override
    public boolean isSelection(){
        return true;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
        ClassDefinition currentClass) throws ContextualError{
            LOG.debug("verifyExpr Selection: start");

            // on retourne le type de la classe + vérification de l'expression à gauche
            Type typeClass =  getLeftExpr().verifyExpr(compiler, localEnv, currentClass);
            // vérication que typeClass est une classe
            if (!typeClass.isClass()){
                throw new ContextualError("Invalide Type! (" + typeClass.getName()+") , l'utilisation des attributs est valable seulement pour les classes. " , getLeftExpr().getLocation());
            }
            // On retourne la définition de la class
            ClassDefinition classDefType = (ClassDefinition) compiler.environmentType.defOfType(typeClass.getName());
            // On vérifie si rightIdentifier existe dans l'env local de la class
            ExpDefinition expDefinition =  classDefType.getMembers().get(getRightIdentifier().getName());
            if ((expDefinition!=null) && expDefinition.isField()){
                // Transtypage
                FieldDefinition fieldDefinition = (FieldDefinition) expDefinition;
                // On retourne la définition de la classe
                ClassDefinition classDefinition = (ClassDefinition) compiler.environmentType.defOfType(typeClass.getName()); 
                // On vérifie si field est définie comme protected
                if (fieldDefinition.getVisibility() == Visibility.PROTECTED){
                    // Vérification
                    if (currentClass == null || !currentClass.getType().isSousType(compiler.environmentType,classDefinition.getType())){
                        throw new ContextualError("l'attribut " + getRightIdentifier().getName() + " est définie comme protected. ", getRightIdent().getLocation());
                    }
                }
                // on retourne l'environment locale àl'intérieur de la classe:
                EnvironmentExp localEnvClass = classDefinition.getMembers();
                //  On retourne le type :
                Type type = getRightIdentifier().verifyExpr(compiler, localEnvClass, classDefinition);
                // On preserve le type.
                setType(type);
                LOG.debug("verifyExpr Selection: end");
                // On retourne le type
                return type;
            }
            throw new ContextualError("La class " + typeClass.getName() + " n'a pas d'attribut " + getRightIdentifier().getName() + ". ", getLocation());
    }

    @Override
    String prettyPrintNode() {
        return  "Selection ";
    }

    @Override
    public void decompile(IndentPrintStream s) {
        getLeftExpr().decompile(s);
        s.print(".");
        getRightIdentifier().decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // A FAIRE : probléme seulement dans l'affichage prettyPrint.
        getLeftExpr().prettyPrint(s, prefix, false);
        getRightIdentifier().prettyPrint(s, prefix, true);
    }

    public AbstractIdentifier getRightIdent() {
        return this.getRightIdentifier();
    }

    protected void codeGenInst(DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGenInst Selection: start");

        getLeftExpr().codeGenInst(compiler);
        int idx = getRightIdent().getFieldDefinition().getIndex();
        compiler.addInstruction(new LOAD(new RegisterOffset(idx, compiler.getMemoryManager().getLastRegister(compiler)) , compiler.getMemoryManager().getRegister(compiler)));
        LOG.debug("codeGenInst Selection: end");
    }

    @Override
    protected void codeGenCond(boolean cond, Label label, DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGenCond Selection: start");
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
        LOG.debug("codeGenCond Selection: end");
    }

}