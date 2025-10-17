package fr.ensimag.deca.tree;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.tree.Visibility;
import fr.ensimag.deca.tree.AbstractIdentifier;
import fr.ensimag.deca.tree.ListExpr;
import fr.ensimag.deca.tree.ListInitialization;
import fr.ensimag.deca.tree.Tree;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tree.DeclVar;
import fr.ensimag.deca.tree.ListDeclVar;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.ima.pseudocode.IMAProgram;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.TSTO;

import java.util.List;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.channels.UnsupportedAddressTypeException;


/**
 * @author gl47
 * @date 26/12/2024
 */

public class DeclMethod extends Tree {
    /**
     * @param type : le type de la méthode.
     * @param name : le nom de la méthode.
     * @param signature : représente une liste des params de la méthode (aprés la validation on construira type Signature).
     * @param location : le lieu de la fonction.
     * @param listVar : la liste des variables définie à l'intérieur de la méthode.
     * @param listInst : la liste des instructions à l'intérieur de la méthode.
     * @param index : l'ordre de la fonction. (commence par 0).
     */
    private AbstractIdentifier type;
    private ListExpr taille;

    public AbstractIdentifier getName() {
        return name;
    }

    private AbstractIdentifier name;
    private ListDeclParam listDeclParam;
    private Location location;
    private AbstractMethodBody methodBody;
    private int index;

    public DeclMethod(AbstractIdentifier type, ListExpr taille, AbstractIdentifier name, ListDeclParam listDeclParam, Location location, AbstractMethodBody methodBody, int index) {
        // A FAIRE :ajoute ou il est nécesaire Validate.Notnull()
        this.type = type;
        this.taille = taille;
        this.name = name;
        this.listDeclParam = listDeclParam;
        this.location = location;
        this.methodBody = methodBody;
        this.index = index;
    }

    public ListDeclVar getListDeclVar() {
        if (methodBody.isSimpleMethod()) {
            return ((MethodBody) methodBody).getListDeclVar();
        } else {
            // Les MethodAsmBody n'ont pas de ListDeclVar, retourne une liste vide ou gère autrement.
            return new ListDeclVar(); // Ou lève une exception si cela n'est pas logique dans ton contexte.
        }
    }

    public ListInst getListInst() {
        if (methodBody.isSimpleMethod()) {
            return ((MethodBody) methodBody).getListInst();
        } else {
            // Les MethodAsmBody n'ont pas de ListInst, retourne une liste vide ou gère autrement.
            return new ListInst(); // Ou lève une exception.
        }
    }

    public ListDeclParam getListDeclParam() {
        return this.listDeclParam;
    }

    /**
     * @param type : le stype de retourne de la méthode
     */
    private Type typeMethod;

    // Méthode qui permet de préserver le type 
    public void setType(Type typeMethod) {
        this.typeMethod = typeMethod;
    }

    // Retourne le type de retourne de la méthode.
    public Type getType() {
        return this.typeMethod;
    }

    // Retourne le body de la méthode
    public AbstractMethodBody getMethodBody() {
        return (this.methodBody);
    }

    public void verifyDeclMethod(DecacCompiler compiler, EnvironmentType envType,
                                 Symbol superClassName, Symbol className,EnvironmentExp localEnv, EnvironmentExp localEnvParent, int index) throws ContextualError {
        // A FAIRE :
        Type typePredefinie = this.type.verifyType(compiler);
        // On retourne le symbol
        Symbol typeSymbol = typePredefinie.getName();
        // teste:
        if (envType.defOfType(typeSymbol) == null) {
            throw new ContextualError("Le type " + this.type.getName() + " n'est pas définie dans l'environment. ", getLocation());
        }
        // On vérifie la taille
        if (this.taille != null && this.taille.size() != 0) {
            typeSymbol = compiler.createSymbol(this.taille.size() + "D " + typeSymbol.getName());
            // On construit le type Tableau
            typePredefinie.createTableType(compiler, this.taille.size());
        }
        // On retourne la définition du nouvel type de la méthode redéfinis.
        TypeDefinition methodTypeDef = envType.defOfType(typeSymbol);
        // On preserve la définition du type
        this.type.setDefinition(methodTypeDef);
        // type de la méthode:
        Type typeMethod = methodTypeDef.getType();
        // On preserve le type dans this.type
        this.type.setType(typeMethod);
        // On préserve le type de la méthode:
        setType(typeMethod);
        // Definition de la méthode  + on vérifie la liste des declaration  des paramétres de la méthode:
        MethodDefinition methodDef = this.listDeclParam.verifyListDeclParam(compiler, localEnv, superClassName, this.name, typeMethod.getName(), this.taille, this.location, index);
        // On preserve la définition de la méthode:
        this.name.setDefinition(methodDef);
        // On preserve le nom de la class de la méthode
        methodDef.setClassIn(className);
        // On preserve les parametre
        methodDef.setListParamExpr(this.listDeclParam.getListParamExpr());
        methodDef.setLocalEnvMethod(localEnv);
        try {
            localEnvParent.declare(this.name.getName(), methodDef);
        } catch (DoubleDefException e) {
            throw new ContextualError("La méthode sous le nom " + this.name.getName() + " est définit plusieur fois ", this.location);
        }
        // On preserve l'environmentExp de la méthode
        setMethodLocalEnv(localEnv);
    }

    public void setClassIn(Symbol classTypeName){
        // On preserve la class ou la méthode est définie
        ((MethodDefinition) getMethodLocalEnv().get(this.name.getName())).setClassIn(classTypeName);
    }

    private EnvironmentExp localEnvMethod;

    /**
     * Cette méthode préserve l'environment local de la méthode dans localEnvMethod
     *
     * @param localEnvMethod
     */

    public void setMethodLocalEnv(EnvironmentExp localEnvMethod) {
        this.localEnvMethod = localEnvMethod;
    }

    /**
     * Cette méthode retourne son environment locale.
     *
     * @return EnvironmentExp
     */

    public EnvironmentExp getMethodLocalEnv() {
        return this.localEnvMethod;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        // A FAIRE:
        type.decompile(s);
        s.print(" ");
        name.decompile(s);
        s.print("(");
        listDeclParam.decompile(s);
        s.println("){");
        s.indent();
        methodBody.decompile(s);
        s.unindent();
        s.print("}");

    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // A FAIRE :
        this.type.iter(f);
        this.name.iter(f);
        this.listDeclParam.iter(f);
        this.methodBody.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        if (type != null) {
            type.prettyPrint(s, prefix, false);
        }
        if (name != null) {
            name.prettyPrint(s, prefix, false);
        }
        if (listDeclParam != null) {
            listDeclParam.prettyPrint(s, prefix, false);
        }
        if (this.methodBody != null) {
            this.methodBody.prettyPrint(s, prefix, true);
        }
    }

    protected void codegenMethod(DecacCompiler compiler, String className) throws CLIException {
        compiler.addLabel(new Label("code." + className + "." + this.name.getName()));
        compiler.getMemoryManager().initTempInMethod();
        IMAProgram program = compiler.getProgram();
        IMAProgram methodProgram = new IMAProgram();
        compiler.setProgram(methodProgram);
        compiler.getMemoryManager().saveRegisters(compiler);
        //Creating method arguments
        listDeclParam.codegenListParam(compiler);

        methodBody.codegenMethBody(compiler);
        compiler.addLabel(new Label("fin." + className + "." + this.name.getName()));

        if (this.getType().getName().getName().equals("void")) {
            compiler.getMemoryManager().restoreRegisters(compiler);
            compiler.addInstruction(new RTS()); //if void no need for return

        } else if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new BRA(compiler.getMissingReturn()));
        }
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addFirstInstruction(new BOV(compiler.getPile_pleine()));
        }
        compiler.addFirstInstruction(new TSTO(compiler.getMemoryManager().getMaxRegistersTemp()));
        program.append(methodProgram);
        compiler.setProgram(program);

    }


}
