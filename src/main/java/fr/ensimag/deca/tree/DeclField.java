package fr.ensimag.deca.tree;


import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tree.AbstractIdentifier;
import fr.ensimag.deca.tree.Visibility;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.Type;
import java.util.List;

import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;


import java.io.PrintStream;

/**
 * @author : gl47
 * @date: 01/01/2025
 * 
 */

public class DeclField extends Tree{
    private static final Logger LOG = Logger.getLogger(Main.class);

    private AbstractIdentifier name;
    private AbstractInitialization init;
    
    public DeclField(AbstractIdentifier name, AbstractInitialization init){
        Validate.notNull(name);
        Validate.notNull(init);
        this.name = name;
        this.init = init;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        // A FAIRE:
        name.decompile(s);
        init.decompile(s);
    }

    protected void verifyDeclField(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass, Type typeName, ListExpr taille,Visibility visbility)
            throws ContextualError{
                LOG.debug("verifyDeclField: start");

                // On incrément le nombre des fiels
                currentClass.incNumberOfFields();
                // On retourne le nom l'identificateur:
                Symbol nomIdentifictauer = this.name.getName();
                // On construit la définition:
                FieldDefinition fieldDefinition = new FieldDefinition(typeName, getLocation(), visbility, currentClass, currentClass.getNumberOfFields(), taille);
                // On preserve la définition : 
                this.name.setDefinition(fieldDefinition);
                try{
                    // On ajoute l'attribut à l'environment locale
                    localEnv.declare(this.name.getName(), fieldDefinition);   
                }catch(DoubleDefException e){
                    throw new ContextualError("L'attribut' " + this.name.getName() + " est définie plusieur fois. ", this.name.getLocation());
                }
                LOG.debug("verifyDeclField: end");

            }
    
    /**
     * Cette méthode vérifie l'initialisation d'une attributs
     * @param compiler
     * @param typeName
     * @param taille
     * @param localEnv
     * @param currentClass
     * @throws ContextualError
     * 
     */
    protected void verifyDeclFieldInit(DecacCompiler compiler, Type typeName, ListExpr taille, EnvironmentExp localEnv, 
        ClassDefinition currentClass )throws ContextualError{
            // On vérifie l'initialisation
            this.init.verifyInitialization(compiler, typeName, taille, localEnv, currentClass);
            // On preserve la taille
            this.name.setTailleExpr(compiler, localEnv, currentClass, taille);
            // Vérification
            if (!this.init.isNoInitialization()){
                // On preserve le type dynamique
                this.name.setDynamicType(((Initialization) this.init).getExpression().getType(), localEnv);
            }
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        this.name.iter(f);
        this.init.iter(f); 
    }
    
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        this.name.prettyPrint(s, prefix, false);
        this.init.prettyPrint(s, prefix, true);
    }

    protected void codeGenField(DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGenField: start");

        DAddr addr = new RegisterOffset(-2, Register.LB);
        this.name.getFieldDefinition().setOperand(addr);
        this.init.codeGenInitField(compiler, this.name.getDefinition().getType());

        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), compiler.getMemoryManager().getRegister(compiler)));
        RegisterOffset regOff = new RegisterOffset( name.getFieldDefinition().getIndex(), compiler.getMemoryManager().getLastRegister(compiler));
        compiler.addInstruction(new STORE(compiler.getMemoryManager().getLastRegister(compiler), regOff));
        LOG.debug("codeGenField: end");



    }

}