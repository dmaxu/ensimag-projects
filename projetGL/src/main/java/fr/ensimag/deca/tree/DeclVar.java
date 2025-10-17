package fr.ensimag.deca.tree;

import java.io.PrintStream;

import javax.lang.model.element.VariableElement;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import java.util.List;

/**
 * @author gl47
 * @date 01/01/2025
 */
public class DeclVar extends AbstractDeclVar {
    private static final Logger LOG = Logger.getLogger(Main.class);

    
    final private AbstractIdentifier type;
    final private AbstractIdentifier varName;
    final private AbstractInitialization initialization;
    private ListExpr taille;

    public DeclVar(AbstractIdentifier type, AbstractIdentifier varName, AbstractInitialization initialization, ListExpr taille) {
        Validate.notNull(type);
        Validate.notNull(varName);
        Validate.notNull(initialization);
        Validate.notNull(taille);
        this.type = type;
        this.varName = varName;
        this.initialization = initialization;
        this.taille = taille;
    }

    // Retourne le nom de la variable.
    public Symbol getVarName(){
        return this.varName.getName();
    }

    @Override
    protected void verifyDeclVar(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError{
                LOG.debug("verifyDeclVar: start");

                // On vérifie si le type de la  variable est void.
                Type varType = this.type.verifyType(compiler);
                // vérifie si le type de la variable est null.
                if (varType == null){
                    throw new ContextualError("la variable ne peut pas étre de type null.", this.getLocation());
                }
                // On vérifie si la variable est de type void.
                if (varType.isVoid()) {
                    throw new ContextualError("La variable ne peut pas avoir void comme un type. ", this.getLocation());
                }
                // On vérifie si la taille n'est pas null
                if (this.taille != null && this.taille.size() != 0){
                    // On construit le symbol
                    Symbol listSymbol = compiler.createSymbol(this.taille.size() + "D " + varType.getName());
                    // On retourne le type
                    varType = varType.createTableType(compiler, this.taille.size());
                    // On vérifie l'initialisation de la variable:
                    this.initialization.verifyInitialization(compiler, varType, taille, localEnv, currentClass);
                    // On change la taille
                    if (!this.initialization.isNoInitialization()){
                        this.taille = ((Initialization) this.initialization).getExpression().getTailleExpr(compiler, localEnv, currentClass);
                    }
                }else{
                    // On vérifie l'initialisation de la variable:
                    this.initialization.verifyInitialization(compiler, varType, taille, localEnv, currentClass);
                }
                VariableDefinition varDef = new VariableDefinition(varType, this.getLocation(), this.taille);
                // On sauvegarde la variable et sa définition dans EnvironmentExp
                Symbol varSymbol = this.varName.getName();
                try{
                    // On preserve la variable dans l'environment.
                    localEnv.declare(varSymbol, varDef);
                }catch(DoubleDefException e){
                    throw new ContextualError("La variable " + varSymbol + " est définie plusieur fois.", this.varName.getLocation());
                }
                // On preserve le type
                this.varName.setType(varType);
                // on preserve la définition de la varibale
                this.varName.setDefinition(varDef);
                // Vérification
                if (!this.initialization.isNoInitialization()){
                    // On preserve le cas dynamique
                    this.varName.setDynamicType(((Initialization)this.initialization).getExpression().getType(), localEnv);
                }
                LOG.debug("verifyDeclVar: end");

    }
    
    @Override
    public void decompile(IndentPrintStream s) {
        // A FAIRE:
        type.decompile(s);
        s.print(" ");
        varName.decompile(s);
        initialization.decompile(s);
        s.println(";");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        varName.iter(f);
        initialization.iter(f);   
    }
    
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        varName.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }

    public void codeGenDeclVar(DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGenDeclVar: start");

        //give address and increment GB
        DAddr addr = new RegisterOffset(compiler.getMemoryManager().getGlobalVar(), Register.GB);
        compiler.getMemoryManager().incrementGlobalVar();
        compiler.getEnvExp().get(varName.getName()).setOperand(addr);
        //init
        initialization.codeGenInit(compiler, addr);
        LOG.debug("codeGenDeclVar: end");

    }

    public void codeGenDeclVarLocal(DecacCompiler compiler, int idx) throws CLIException {
        LOG.debug("codeGenDeclVarLocal: start");

        DAddr operand = new RegisterOffset(idx, Register.LB);
        varName.getVariableDefinition().setOperand(operand);
        this.initialization.codeGenInit(compiler,operand);
        LOG.debug("codeGenDeclVarLocal: end");

    }


}