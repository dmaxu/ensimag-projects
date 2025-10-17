package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tree.AbstractIdentifier;
import fr.ensimag.deca.tree.Visibility;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.TypeDefinition;
import java.util.List;

import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import java.io.PrintStream;

/**
 * @author : gl45
 * @date : 04/01/2025
 * 
 */

public class DeclParam extends Tree{
    private AbstractIdentifier type;
    private AbstractIdentifier name;
    private ListExpr taille;
    private static final Logger LOG = Logger.getLogger(Main.class);

    public DeclParam(AbstractIdentifier type, ListExpr taille,AbstractIdentifier name){
        Validate.notNull(type);
        Validate.notNull(name);
        this.type = type;
        this.taille = taille;
        this.name = name;
    }

    public AbstractIdentifier getNameIdentifier(){
        return this.name;
    }

    protected void verifyDeclParam(DecacCompiler compiler, EnvironmentExp localEnv, Signature signature) throws ContextualError{
        LOG.debug("verifyDeclParam: start");

        // on retourne l'environment des types
        EnvironmentType environmentType = compiler.environmentType;
        // On vérifie si this.type est connue par l'environment
        TypeDefinition typeDefinition = environmentType.defOfType(this.type.getName());
        // On vérifie que typeDefinition n'est pas null, et que le type est différent de void
        if (typeDefinition == null || this.type.getName().getName().equals("void")){
            throw new ContextualError("Le type " + this.type.getName() + " est inconnue. " , this.type.getLocation());
        }
        // On retourne le type du paramétre
        Type typeParam = typeDefinition.getType();
        // On vérifie la taille
        if (this.taille != null&& this.taille.size() != 0){
            // On retourne le typeTable
            typeParam = typeParam.createTableType(compiler, this.taille.size());
        }
        // On construit la définition du paramétre
        ParamDefinition paramDefinition = new ParamDefinition(typeParam, this.taille ,this.name.getLocation());
        // On preserve que le paramétre
        paramDefinition.isMethodParameter();
        try{
            // On ajoute la paramétre à l'environment
            localEnv.declare(this.name.getName(), paramDefinition);
        }catch(DoubleDefException e){
            throw new ContextualError("Le paramétre sous le nom " + this.name.getName() + " est définie plusieur fois." , this.name.getLocation());
        }
        // On preserve la définition de la paramtére
        this.name.setDefinition(paramDefinition);
        // On preserve la définition du type
        this.type.setDefinition(typeDefinition);
        // On ajoute le type à la signature
        signature.add(typeParam);
        LOG.debug("verifyDeclParam: end");
    }

    @Override
    public void decompile(IndentPrintStream s){
        this.type.decompile(s);
        s.print(" ");
        this.name.decompile(s);
    }
    @Override
    protected void iterChildren(TreeFunction f) {
        this.type.iter(f);
        this.name.iter(f); 
    }
    
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        this.type.prettyPrint(s, prefix, false);
        this.name.prettyPrint(s, prefix, true);
    }

    protected void codeGenDeclParam(DecacCompiler compiler,int index){
        LOG.debug("codeGenDeclParam: start");

        DAddr operand = new RegisterOffset(index , Register.LB);
        ParamDefinition def = (ParamDefinition)name.getDefinition();
        def.setOperand(operand);
        LOG.debug("codeGenDeclParam: end");
    }

}
