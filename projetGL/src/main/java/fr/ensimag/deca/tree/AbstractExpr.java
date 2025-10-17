package fr.ensimag.deca.tree;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;

import org.apache.log4j.Logger;

import java.io.PrintStream;
import java.util.List;

import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.commons.lang.Validate;

/**
 * Expression, i.e. anything that has a value.
 *
 * @author gl47
 * @date 01/01/2025
 */
public abstract class AbstractExpr extends AbstractInst {
    private static final Logger LOG = Logger.getLogger(Main.class);
    /**
     * @return true if the expression does not correspond to any concrete token
     *         in the source code (and should be decompiled to the empty string).
     */
    boolean isImplicit() {
        return false;
    }

    public boolean isThisLiteral(){
        return false;
    }

    public boolean isLiteral(){
        return false;
    }

    public boolean isListLiteral(){
        return false;
    }

    public boolean isSelectionCall(){
        return false;
    }

    public boolean isIdentifier(){
        return false;
    }

    public boolean isTableLiteral() {
        return false;
    }

    public boolean isTableau(){
        return false;
    }

    public boolean isTableauElement(){
        return false;
    }

    public boolean isImmediate(){
        return false;
    }

    public boolean isSelection(){
        return false;
    }

    public boolean isListElement(){return false;}

    public DVal getAdresse(){
        throw new UnsupportedOperationException("n'est pas un literal.");
    }

    /**
     * Get the type decoration associated to this expression (i.e. the type computed
     * by contextual verification).
     */
    public Type getType() {
        return type;
    }

    protected void setType(Type type) {
        Validate.notNull(type);
        this.type = type;
    }

    private Type type;

    @Override
    protected void checkDecoration() {
        if (getType() == null) {
            throw new DecacInternalError("Expression " + decompile() + " has no Type decoration");
        }
    }

    /**
     * Verify the expression for contextual error.
     * 
     * implements non-terminals "expr" and "lvalue"
     * of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler     (contains the "env_types" attribute)
     * @param localEnv
     *                     Environment in which the expression should be checked
     *                     (corresponds to the "env_exp" attribute)
     * @param currentClass
     *                     Definition of the class containing the expression
     *                     (corresponds to the "class" attribute)
     *                     is null in the main bloc.
     * @return the Type of the expression
     *         (corresponds to the "type" attribute)
     */
    public abstract Type verifyExpr(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError;

    /**
     * Verify the expression in right hand-side of (implicit) assignments
     * 
     * implements non-terminal "rvalue" of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler     contains the "env_types" attribute
     * @param localEnv     corresponds to the "env_exp" attribute
     * @param currentClass corresponds to the "class" attribute
     * @param expectedType corresponds to the "type1" attribute
     * @return this with an additional ConvFloat if needed...
     */
    public AbstractExpr verifyRValue(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass,
            Type expectedType)
            throws ContextualError {
        // Compatibilité pour l’affectation : on vérifie si assign_compatible(env, T1,
        // T2) page 75.
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        // on vérifie le type d'instruction
        LOG.debug("verify Inst: start");
        Type argType = this.verifyExpr(compiler, localEnv, currentClass);
        // On preserve le type
        this.setType(argType);
        LOG.debug("verify Inst: end");
    }

    /**
     * Verify the expression as a condition, i.e. check that the type is
     * boolean.
     *
     * @param localEnv
     *                     Environment in which the condition should be checked.
     * @param currentClass
     *                     Definition of the class containing the expression, or
     *                     null in
     *                     the main program.
     */
    void verifyCondition(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
                // on retourne le type de l'expression
                LOG.debug("verify Condition: start");

                Type typeExpr = verifyExpr(compiler, localEnv, currentClass);
                // On vérifie que le type est boolean
                if (!typeExpr.isBoolean()){
                    throw new ContextualError("Type invalide (" +typeExpr.getName() + ")! valable seulement pur un type Boolean. " , getLocation());
                }
                LOG.debug("verify Condition: end");

    }
    
    /**
     * Generate code to print the expression
     *
     * @param compiler
     */
    protected void codeGenPrint(DecacCompiler compiler, Boolean printHex) throws CLIException {
        LOG.debug("CodeGen Print: start");

        this.codeGenInst(compiler);
        GPRegister reg = (GPRegister) compiler.getMemoryManager().getLastRegister(compiler);
        compiler.addInstruction(new LOAD(reg, Register.getR(1)));
        if (getType().isInt()){
            compiler.addInstruction(new WINT());
        }
        else if (getType().isFloat()){
            if (printHex){
                compiler.addInstruction(new WFLOATX());
            }
            else {
                compiler.addInstruction(new WFLOAT());
            }
        }
        else if (getType().isBoolean()){
            compiler.addInstruction(new WINT());
        }
        LOG.debug("CodeGen Print: end");

    }

    protected ListExpr getTailleExpr(DecacCompiler compiler, EnvironmentExp localEnv,
        ClassDefinition currentClass) throws ContextualError{
        throw new UnsupportedOperationException("completed");
    }

    protected void setTailleExpr(DecacCompiler compiler, EnvironmentExp localEnv,
        ClassDefinition currentClass, ListExpr newTaille) throws ContextualError{
        throw new UnsupportedOperationException("completed");
    }

    protected void setDynamicType(Type type, EnvironmentExp localEnv){
        
    }


    @Override
    protected void decompileInst(IndentPrintStream s) {
        decompile(s);
        s.print(";");
    }

    @Override
    protected void prettyPrintType(PrintStream s, String prefix) {
        Type t = getType();
        if (t != null) {
            s.print(prefix);
            s.print("type: ");
            s.print(t);
            s.println();
        }
    }

    protected void codeGenInst(DecacCompiler compiler) throws CLIException{
        LOG.debug("CodeGen Inst: start");
        Label falseLabel = compiler.getLabelManager().getLabel("falseLabel");
        Label endLabel = compiler.getLabelManager().getLabel("endLabel");
        GPRegister reg = compiler.getMemoryManager().getRegister(compiler);
        codeGenCond(false,falseLabel,compiler);
        compiler.addInstruction(new LOAD(1,reg));
        compiler.addInstruction(new BRA(endLabel));
        compiler.addLabel(falseLabel);
        compiler.addInstruction(new LOAD(0,reg));
        compiler.addLabel(endLabel);
        LOG.debug("CodeGen Inst: end");

    }

    protected void codeGenCond(boolean cond, Label label, DecacCompiler compiler) throws CLIException{

    }



}