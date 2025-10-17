package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.log4j.Logger;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.codegen.MemoryManager;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.TreeFunction;
import fr.ensimag.deca.tree.AbstractIdentifier;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.ADD;
import fr.ensimag.ima.pseudocode.instructions.BGE;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;
import fr.ensimag.ima.pseudocode.instructions.INT;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.SUB;

/**
 * @author gl47
 * @date 01/01/2025
 */

public class TypeCasting extends AbstractExpr {
    private AbstractIdentifier newType;
    private AbstractExpr variable;
    private static final Logger LOG = Logger.getLogger(Main.class);
    static private int castingNumber=0;
    public TypeCasting(AbstractIdentifier newType, AbstractExpr variable) {
        this.newType = newType;
        this.variable = variable;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler,
        EnvironmentExp localEnv, ClassDefinition currentClass)
        throws ContextualError {
        LOG.debug("verifyExpr TypeCasting: start");

        // On retourne le type de la variable.
        Type typeVariable = this.variable.verifyExpr(compiler, localEnv, currentClass);
        // On retourne le type (nouveau type)
        Type type = this.newType.verifyType(compiler);
        // On vérifie le nouveau type
        if (type.isString()){
            throw new ContextualError("Impossible de faire un transtypage en utilisant le type string.", this.newType.getLocation());
        }
        // On vérifie si typeVariable est un sous type de newType.
        if(!typeVariable.isSousType(compiler.environmentType, type)&&!((type.isInt()||type.isFloat())&&(typeVariable.isFloat()||typeVariable.isInt()))){
            // le cas d'une classe
            if (typeVariable.isClass() && this.variable.isIdentifier()){
                // Transtypage
                AbstractIdentifier identVar = (AbstractIdentifier) this.variable;
                // On vérifie que le type dynamique de la variabel est un sous type du nouveau type
                if (!identVar.getDynamicType(localEnv).isSousType(compiler.environmentType, type)){
                    throw new ContextualError("les types dynamique et statique de la variable " + identVar.getName() + " ne sont pas des sous types de " + type.getName() +".", getLocation());
                }
            }else{
                throw new ContextualError("Impossible de faire un transtypage, le type " +  typeVariable.getName()+ " n'est pas un sous type de " + type.getName() + ". " , getLocation());
            }
        }
        // On preserve le type:
        setType(type);
        LOG.debug("verifyExpr TypeCasting: end");
        // On retourne le type
        return type;
    }
   
    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        this.newType.decompile(s);
        s.print(")");
        s.print(" ");
        this.variable.decompile(s);
    }


    @Override
    protected void iterChildren(TreeFunction f) {
        newType.iter(f);
        variable.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        this.newType.prettyPrint(s, prefix, false);
        this.variable.prettyPrint(s, prefix, true);
    }

    protected void codeGenInst(DecacCompiler compiler) throws CLIException {
        LOG.debug("codeGenInst TypeCasting: start");

        variable.codeGenInst(compiler);

        if (variable.getType().equals(newType.getDefinition().getType())) {
            //do nothing
        } else if (variable.getType().isFloat() && newType.getDefinition().getType().isInt()) {
            castingNumber++;
            GPRegister reg = compiler.getMemoryManager().getLastRegister(compiler);
            // Vérifier si le registre contient une valeur négative
            compiler.addInstruction(new CMP(new ImmediateFloat(0.0f), reg)); // Comparer avec 0.0
            Label positiveLabel = new Label("positive_value"+castingNumber);
            Label endLabel = new Label("end_round"+castingNumber);

            compiler.addInstruction(new BGE(positiveLabel)); // Sauter si la valeur est positive ou zéro

            // Cas où la valeur est négative : soustraire 0.5
            compiler.addInstruction(new SUB(new ImmediateFloat(0.5f), reg));
            compiler.addInstruction(new BRA(endLabel)); // Aller à la fin

            // Cas où la valeur est positive : ajouter 0.5
            compiler.addLabel(positiveLabel);
            compiler.addInstruction(new ADD(new ImmediateFloat(0.5f), reg));

            // Fin de l'arrondi
            compiler.addLabel(endLabel);
            compiler.addInstruction(new INT(reg, compiler.getMemoryManager().getRegister(compiler)));
        } else if (variable.getType().isInt() && newType.getDefinition().getType().isFloat()) {
            GPRegister reg = compiler.getMemoryManager().getLastRegister(compiler);
            compiler.addInstruction(new FLOAT(reg, compiler.getMemoryManager().getRegister(compiler)));
        } else {
            //TODO class conversion
        }
        LOG.debug("codeGenInst TypeCasting: start");
    }

}
