package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tree.Visibility;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.util.List;

import org.apache.log4j.Logger;

public class ListDeclParam extends TreeList<DeclParam> {
    private static final Logger LOG = Logger.getLogger(Main.class);

    @Override
    public void decompile(IndentPrintStream s) {
        // A FAIRE:
        int size = getList().size();
        int count = 0;
    
        for (DeclParam declParam : getList()) {
            declParam.decompile(s);
    
            // Ajouter un point-virgule sauf pour le dernier élément
            if (count < size - 1) {
                s.print(", ");
            }
            count++;
        }
    }

    public ListExpr getListParamExpr(){
        ListExpr listParam = new ListExpr();
        for (DeclParam param : this.getList()){
            listParam.add(param.getNameIdentifier());
        }
        return listParam;
    }

    public MethodDefinition verifyListDeclParam(DecacCompiler compiler, EnvironmentExp localEnv, Symbol superClassName,
                                                AbstractIdentifier nameIdentifier, Symbol typeMethodName, ListExpr taille, Location location, int index) throws ContextualError {
        // le nome de la méthode
        LOG.debug("verifyListDeclParam: start");

        Symbol nameMethod = nameIdentifier.getName();
        // On construit la signature de la méthode.
        Signature signatureMethod = new Signature();
        // On retourne le nouveau type de la méthode redéfinis.
        TypeDefinition typeDefMethod = compiler.environmentType.defOfType(typeMethodName);
        // on retourne le type
        Type typeMethod = typeDefMethod.getType();
        // On boucle sur les paramétres de la méthode
        for (DeclParam decl : getList()) {
            // On Vérifie la déclaration de chaque paramétre
            decl.verifyDeclParam(compiler, localEnv, signatureMethod);
        }
        // On vérifie si superClassName est null
        if (superClassName != null) {
            // on retourne la définition de la super-classe
            ClassDefinition superClassDefinition = (ClassDefinition) compiler.environmentType.defOfType(superClassName);
            // On vérifie si la définition existe
            if (superClassDefinition == null) {
                throw new ContextualError("La super-classe " + superClassName + " n'est pas connue dans l'environment. ", nameIdentifier.getLocation());
            }
            // On retourne l'environmantlocale de la super-classe
            EnvironmentExp localEnvSupClass = superClassDefinition.getMembers();
            // On vérifie si la méthode est déja définie dans la superClass
            if (localEnvSupClass.get(nameMethod) != null) {
                // On retourne la définition de la superMethode
                MethodDefinition superMethodeDef = (MethodDefinition) localEnvSupClass.get(nameMethod);
                // On compare entre la signature de la super-Méthode et de la méthode.
                boolean compSignature = superMethodeDef.getSignature().equals(signatureMethod);
                if (compSignature) {
                    // On retourne l'ancien type de la méthode redéfinis.
                    Type typeMethodOld = localEnvSupClass.get(nameMethod).getType();
                    // Vérification si type de la méthode redéfinie est un sous type du type de la méthode dans sa superclasse. 
                    if (!typeMethod.equals(typeMethodOld)) {
                        throw new ContextualError("Lors de la redéfinition de la méthode " + nameMethod + ", le type " + typeMethodOld + " est différent du type " + typeMethod + ". ", nameIdentifier.getLocation());
                    }
                } else {
                    throw new ContextualError(" Un probléme dans les parametres d'entrée de la méthode redéfinie. " + nameMethod + ". \n" +
                            "\t\tAttendue : " + superMethodeDef.getSignature().toString() + "\n" +
                            "\t\tPrésenté : " + signatureMethod.toString() + "\n",
                            nameIdentifier.getLocation()
                    );
                }
            }
        }
        // La construction de la définition de la méthode
        MethodDefinition methodDefinition = new MethodDefinition(typeMethod, taille, location, signatureMethod, index);
        // On preserve la définition de la méthode
        nameIdentifier.setDefinition(methodDefinition);
        // on retourne la définition de la méthode
        LOG.debug("verifyListDeclParam: end");

        return methodDefinition;
    }

    protected void codegenListParam(DecacCompiler compiler) {
        LOG.debug("verifyListDeclParam: start");
        int index = -3;
        for (DeclParam p : getList()) {
            compiler.getMemoryManager().incrementTempInMethod();
            p.codeGenDeclParam(compiler, index--);
        }
        LOG.debug("verifyListDeclParam: end");
    }

}
