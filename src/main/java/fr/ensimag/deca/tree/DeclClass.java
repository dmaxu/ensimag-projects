package fr.ensimag.deca.tree;

import fr.ensimag.deca.CLIException;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.TypeDefinition;

import java.io.PrintStream;
import java.util.*;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import static fr.ensimag.ima.pseudocode.Register.*;

/**
 * Declaration of a class (<code>class name extends superClass {members}<code>).
 *
 * @author gl47
 * @date 01/01/2025
 */
public class DeclClass extends AbstractDeclClass {

    // A FAIRE : on doit ajouter les attributs que cette classe a besoin de.
    /**
     * @param name : le nom de la classe
     * @param superclass : le nom de la superclasse
     * @param listDeclMethod : une liste qui contient toutes les méthodes qui sont définit à l'intérieure de la classe.
     * @param ListDeclFieldSet : une liste qui contient toutes les attributs de la classe.
     * @param location : la location de la classe.
     */
    private AbstractIdentifier name;
    private AbstractIdentifier superclass;
    private ListDeclMethod listDeclMethod;
    private ListDeclFieldSet listDeclFieldSet;
    private Location location;
    private static final Logger LOG = Logger.getLogger(Main.class);

    public DeclClass(AbstractIdentifier name, AbstractIdentifier superclass, ListDeclMethod listDeclMethod, ListDeclFieldSet listDeclFieldSet, Location location) {
        Validate.notNull(name);
        Validate.notNull(location);
        this.name = name;
        this.superclass = superclass;
        this.listDeclMethod = listDeclMethod;
        this.listDeclFieldSet = listDeclFieldSet;
        this.location = location;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("class ");
        name.decompile(s);
        if (superclass != null) {
            s.print(" extends ");
            superclass.decompile(s);
        }
        s.println(" {");
        s.indent();
        listDeclFieldSet.decompile(s);
        listDeclMethod.decompile(s);
        s.unindent();
        s.print("}");
    };

    @Override
    protected void verifyClass(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verifyClass: start");

        // passe 1 :
        // le nom de la classe
        Symbol className = this.name.getName();
        // EnvironmentType
        EnvironmentType envType = compiler.environmentType;
        if (this.superclass == null) {
            this.superclass = new Identifier(compiler.createSymbol("Object"));
            this.superclass.setLocation(Location.BUILTIN);
        }
        // le nom de la super classe de cette classe
        Symbol superclassName = this.superclass.getName();
        // On retourne la définition de la superclasse
        ClassDefinition superclassDef = (ClassDefinition) envType.defOfType(superclassName);
        // On vérifie su superclassDef exits
        if (superclassDef == null) {
            throw new ContextualError("la super-classe " + superclassName + " n'est pas définie dans l'environment. ", getLocation());
        }
        // On preserve la définition de la superclasse
        this.superclass.setDefinition(superclassDef);
        // On preserve le type de la superclasse
        this.superclass.setType(superclassDef.getType());
        // On construit classType pour cette classe.
        ClassType classType = new ClassType(className, this.location, superclassDef);
        // On construit la définition de cette classe
        ClassDefinition classDef = new ClassDefinition(classType, this.location, superclassDef);
        // on preserve la définition de la classe
        this.name.setDefinition(classDef);
        // Construction du typeClass
        this.name.setType(classType);
        // On ajoute cette classe à envType:
        envType.addTypes(className, (TypeDefinition) classDef);
        // On conserve la définition de la classe.
        setClassDefinition(classDef);
        LOG.debug("verifyClass: end");

    }


    private ClassDefinition classDef;

    /**
     * Cette méthode préserve le définition de la classe.
     *
     * @param classDef : représente la définition de la class
     */

    protected void setClassDefinition(ClassDefinition classDef) {
        this.classDef = classDef;
    }

    /**
     * Cette méthode retourne le définition de la classe.
     *
     * @return classDef : représente la définition de la class
     */
    protected ClassDefinition getClassDefinition() {
        // retourne la définition de la classe.
        return this.classDef;
    }

    @Override
    protected void verifyClassMembers(DecacCompiler compiler)
            throws ContextualError {
        LOG.debug("verifyClassMembers: start");

        // EnvironmentType
        EnvironmentType envType = compiler.environmentType;
        // on retourne le symbol de la superclass
        Symbol superClassSymbol = this.superclass.getName();
        ClassDefinition superClasse = (ClassDefinition) envType.defOfType(superClassSymbol);
        // Environment locale
        EnvironmentExp localEnvParent = superClasse.getMembers();
        // la définition de la classe.
        ClassDefinition def = getClassDefinition();
        // Pour les Attributs:
        //  l'environment locale pour les attributs
        EnvironmentExp localEnvFieldSet = new EnvironmentExp(localEnvParent);
        // On modéfie indexDecalage
        List<Integer> indexDecalageMethods = superClasse.getIntersectionNumberMethods(def.getMembers(), this.listDeclMethod);  
        // On ajoute le nombre des fields pour la superclass
        def.setNumberOfMethods(superClasse.getNumberOfMethods() - indexDecalageMethods.size());
        // On ajoute le nombres des attributs de la superclass
        def.setNumberOfFields(superClasse.getNumberOfFields());
        // Vérification
        if (this.listDeclFieldSet != null) {
            try {
                localEnvFieldSet = this.listDeclFieldSet.verifyListDeclFieldSet(compiler, envType, localEnvParent, superClassSymbol, this.name.getName());
            } catch (DoubleDefException e) {
                throw new UnsupportedOperationException("A FAIRE: error verifyClassMember");
            }
        }
        // on fusionne les deux environements :
        def.getMembers().fusionner(localEnvFieldSet);
        //  l'environment locale pour les méthodes
        if (this.listDeclMethod != null) {
            this.listDeclMethod.verifyListDeclMethod(compiler, envType, superClassSymbol, def,def.getMembers());
        }
        // On resete 
        def.resetIndexMethod(superClasse, indexDecalageMethods);
        LOG.debug("verifyClassMembers: end");

    }

    @Override
    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verifyClassBody: start");
        // A FAIRE;
        // On vérifie les intilisations des attributs
        this.listDeclFieldSet.verifyListDeclFieldSetInit(compiler, getClassDefinition().getMembers(), getClassDefinition());
        // On vérifie la validité de toutes les méthodes.
        for (DeclMethod declMethod : this.listDeclMethod.getList()) {
            //Environment local de la méthode
            EnvironmentExp localEnvMethod = declMethod.getMethodLocalEnv();
            // Type de retourne de la méthode:
            Type typeMethod = declMethod.getType();
            // la liste des variables
            ListDeclVar listVar = declMethod.getListDeclVar();

            AbstractMethodBody methodBody = declMethod.getMethodBody();
            // On vérifie le body de la méthode
            if (methodBody.isSimpleMethod()) {
                ((MethodBody) methodBody).verifyMethodBody(compiler, localEnvMethod, this.classDef, typeMethod);
            }
        }
        LOG.debug("verifyClassBody: end");

    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        if (name != null) {
            name.prettyPrint(s, prefix, false);
        }
        if (superclass != null) {
            superclass.prettyPrint(s, prefix, false);
        }
        if (listDeclFieldSet != null) {
            listDeclFieldSet.prettyPrint(s, prefix, false);
        }
        if (listDeclMethod != null) {
            listDeclMethod.prettyPrint(s, prefix, true);
        }
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        name.iter(f);
        if (superclass != null) superclass.iter(f);
        listDeclFieldSet.iter(f);
        listDeclMethod.iter(f);
    }

    public void genClassTable(DecacCompiler compiler, LabelOperand objectEquals) {
        LOG.debug("genClassTable: start");

        compiler.addComment("Construction de la table des methodes de " + this.name.getName());
        compiler.addInstruction(new LEA(new RegisterOffset(compiler.getMemoryManager().getLastTablePos(), GB), R0));
        compiler.getMemoryManager().setLastTablePos(compiler.getMemoryManager().getGlobalVar());
        compiler.addInstruction(new STORE(R0, new RegisterOffset(compiler.getMemoryManager().getGlobalVar(), GB)));
        name.getClassDefinition().setAddressInVtable(new RegisterOffset(compiler.getMemoryManager().getGlobalVar(), GB));
        ((ClassType) name.getType()).getDefinition().setAddressInVtable(new RegisterOffset(compiler.getMemoryManager().getGlobalVar(), GB));
        compiler.getMemoryManager().incrementGlobalVar();
        compiler.addInstruction(new LOAD(objectEquals, R0));
        compiler.addInstruction(new STORE(R0, new RegisterOffset(compiler.getMemoryManager().getGlobalVar(), GB)));
        compiler.getMemoryManager().incrementGlobalVar();

        //Inherited methods
        ClassDefinition currClass = this.superclass.getClassDefinition(); //variable to iterate through superclasses
        for (Symbol methodName : currClass.getMembers().getDictionnaireDef().keySet()) {
            if (currClass.getMembers().getDictionnaireDef().get(methodName).isMethod() && ((MethodDefinition)this.getClassDefinition().getMembers().getWithoutParent(methodName)).isAdded()) {
                MethodDefinition methodDef =  ((MethodDefinition)this.getClassDefinition().getMembers().getWithoutParent(methodName));
                //System.out.println(methodDef.getClassIn().getName());
                Label methLabel = new Label("code." + methodDef.getClassIn() +  "." + methodName.getName());
                LabelOperand methLabelOperand = new LabelOperand(methLabel);
                compiler.addInstruction(new LOAD(methLabelOperand, R0));
                compiler.addInstruction(new STORE(R0, new RegisterOffset(compiler.getMemoryManager().getGlobalVar(), GB)));
                compiler.getMemoryManager().incrementGlobalVar();
            }
        }

        for (DeclMethod declMethod : this.listDeclMethod.getList()) {
            Label methLabel = new Label("code." + this.name.getName() + "." + declMethod.getName().getName());
            LabelOperand methLabelOperand = new LabelOperand(methLabel);
            compiler.addInstruction(new LOAD(methLabelOperand, R0));
            compiler.addInstruction(new STORE(R0, new RegisterOffset(compiler.getMemoryManager().getGlobalVar(), GB)));
            compiler.getMemoryManager().incrementGlobalVar();
        }
        LOG.debug("genClassTable: end");

    }


    public void codegenClass(DecacCompiler compiler) throws CLIException, CLIException {
        //TODO: add TSTO at this bloc
        LOG.debug("codegenClass: start");

        compiler.addComment("--------------------------------------------------");
        compiler.addComment("Classe " + this.name.getName().getName());
        compiler.addComment("--------------------------------------------------");


        //init
        compiler.addComment("Initialisation des champs de " + this.name.getName());
        Label initLabel = new Label("init." + this.name.getName()); // no need to use label manager because there shouldn't be 2 classes with the same name
        compiler.addLabel(initLabel);

        compiler.getMemoryManager().initTempInMethod();
        IMAProgram program = compiler.getProgram();
        IMAProgram methodProgram = new IMAProgram();
        compiler.setProgram(methodProgram);


        compiler.getMemoryManager().saveRegisters(compiler);

        listDeclFieldSet.codegenFieldSet(compiler);

        // initialising inherited fields
        if (!superclass.getName().getName().equals("Object")) {
            compiler.addComment("Init des champs hérités de " + superclass.getName().getName());
            compiler.addInstruction(new LOAD(new RegisterOffset(-2, LB), compiler.getMemoryManager().getRegister(compiler)));
            compiler.addInstruction(new PUSH(compiler.getMemoryManager().getLastRegister(compiler)));
            compiler.addInstruction(new BSR(new Label("init." + superclass.getName().getName())));
            compiler.addInstruction(new SUBSP(1));
        }


        compiler.getMemoryManager().restoreRegisters(compiler);
        compiler.addInstruction(new RTS());
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addFirstInstruction(new BOV(compiler.getPile_pleine()));
        }
        compiler.addFirstInstruction(new TSTO(compiler.getMemoryManager().getMaxRegistersTemp()));
        program.append(methodProgram);
        compiler.setProgram(program);

        listDeclMethod.codegenListMethod(compiler, this.name.getName().getName());
        LOG.debug("codegenClass: end");

    }


}
