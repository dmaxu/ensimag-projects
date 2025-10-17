package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.Visibility;
import fr.ensimag.deca.tree.AbstractIdentifier;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.ListExpr;
import fr.ensimag.deca.tree.ListInitialization;
import fr.ensimag.deca.tree.Tree;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import java.util.List;

import org.apache.log4j.Logger;

import java.io.PrintStream;

import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * 
 * @author gl47
 * @date 26/12/2024
 */

public class DeclFieldSet extends Tree{
    /**
     * 
     * @visibility : il doit étre soit PROTECTED soit PUBLIC.
     * @type : représente de le type des attributs qui sont définits dans la méme ligne.
     * @names : une liste ordonnée qui contient les noms des attributs .
     * @inits : une liste ordonnée qui contient l'initialization des attributs. 
     */
    
    private Visibility visibility;
    private AbstractIdentifier type;
    private ListExpr taille;
    private ListDeclField listDeclField;
    private static final Logger LOG = Logger.getLogger(Main.class);

    public DeclFieldSet(Visibility visibility, AbstractIdentifier type, ListDeclField listDeclField, ListExpr taille) {
        this.visibility = visibility;
        this.type = type;
        this.listDeclField = listDeclField;
        this.taille = taille;
    }

    public void verifyDeclFieldSet(DecacCompiler compiler, EnvironmentType envType,
        Symbol superClassName,EnvironmentExp localEnv ,Symbol className) throws ContextualError, DoubleDefException{
            LOG.debug("verifyDeclFieldSet: start");

            // A FAIRE :
            // On vérifie le type
            Type typePreDef = this.type.verifyType(compiler);
            // On retourne le symbol
            Symbol typeSymbol = typePreDef.getName();
            // on vérifie si type = void :
            if (typeSymbol.getName().equals("void")){
                throw new ContextualError("Le type d'un attribut ne peux pas étre void. " , getLocation());
            }
            // On vérifie que la taille n'est pas null
            if (this.taille != null && this.taille.size() != 0){
                // On construit le symbol
                typeSymbol = compiler.createSymbol( this.taille.size() + "D " + typeSymbol);
                // On construit le type
                typePreDef.createTableType(compiler, this.taille.size());
            }
            // ,l’environnement des expressions de la super-classe, qu'il est un identificateur de champ.
            ClassDefinition superClassDef =  (ClassDefinition) envType.defOfType(superClassName);
            // On vérifie si la classe est un identificateur de champ
            if (superClassDef != null && superClassDef.getMembers().get(className) != null){
                ExpDefinition fieldTest = superClassDef.getMembers().get(className);
                if (!(fieldTest.isField())){
                    throw new ContextualError("l'identificateur sous le nom :" + className.getName() + " doit être un identificateur de champ.", getLocation());
                }
            }
            // type des attributs qui sont définies dans la méme lignes.
            TypeDefinition typeDefField = (TypeDefinition) envType.defOfType(typeSymbol);
            if (typeDefField == null){
                throw new ContextualError("le type " + typeSymbol + " n'est pas définie. " , this.type.getLocation());
            }
            // On vérifie la liste des attributs
            this.listDeclField.verifyListDeclField(compiler, localEnv, (ClassDefinition) envType.defOfType(className), typeDefField.getType(), this.visibility, this.taille);
            // On preserve la définition du type
            this.type.setDefinition(typeDefField);
            LOG.debug("verifyDeclFieldSet: end");

    }
    /**
     * Cette méthode vérifie l'initialisation d'une liste des attributs de méme type
     * @param compiler
     * @param localEnv
     * @param currentClass
     * @throws ContextualError
     * 
     */
    
    public void verifyDeclFieldSetInit(DecacCompiler compiler,  EnvironmentExp localEnv, 
        ClassDefinition currentClass) throws ContextualError{
            // On vérifie les initialisations
            this.listDeclField.verifyListDeclFieldInit(compiler, this.type.getType(), this.taille, localEnv, currentClass);
        }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(this.visibility.toString());
        s.print(" ");
        type.decompile(s);
        s.print(" ");
        listDeclField.decompile(s);
    }
    @Override
    protected void iterChildren(TreeFunction f) {
        // A FAIRE :
        type.iter(f);
        listDeclField.iter(f);
    }
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // A FAIRE:
        s.print(prefix +"[]>"  + getLocation() + " [Visibility==" + this.visibility + "] DeclField\n");
        this.type.prettyPrint(s, prefix, false);
        this.listDeclField.prettyPrint(s, prefix, true);
    }

    public ListDeclField getListDeclField() {
        return this.listDeclField;
    }
}