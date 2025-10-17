package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.ListDeclMethod;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.Label;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Objects;

import java.util.*;

import org.apache.commons.lang.Validate;

/**
 * Definition of a class.
 *
 * @author gl47
 * @date 01/01/2025
 */
public class ClassDefinition extends TypeDefinition {

    private DAddr addressInVtable;

    public void setNumberOfFields(int numberOfFields) {
        this.numberOfFields = numberOfFields;
    }

    public int getNumberOfFields() {
        return numberOfFields;
    }

    public void incNumberOfFields() {
        this.numberOfFields++;
    }

    public int getNumberOfMethods() {
        return numberOfMethods;
    }

    public void setNumberOfMethods(int n) {
        Validate.isTrue(n >= 0);
        numberOfMethods = n;
    }
    
    public int incNumberOfMethods() {
        numberOfMethods++;
        return numberOfMethods;
    }

    private int numberOfFields = 0;
    private int numberOfMethods = 0;
    
    @Override
    public boolean isClass() {
        return true;
    }
    
    @Override
    public ClassType getType() {
        // Cast succeeds by construction because the type has been correctly set
        // in the constructor.
        return (ClassType) super.getType();
    };

    public ClassDefinition getSuperClass() {
        return superClass;
    }

    private final EnvironmentExp members;
    private final ClassDefinition superClass; 

    public EnvironmentExp getMembers() {
        return members;
    }

    /**
     * Cette méthode retourne le une liste des indices des méthodes qui ont été redéfinie
     * @param listeDeclMethod
     * @return
     * 
     */

    public List<Integer> getIntersectionNumberMethods( EnvironmentExp localEnv,ListDeclMethod listeDeclMethod){
        List<Integer> setNumberMethods = new ArrayList<>();
        if (listeDeclMethod != null){
            for (Symbol symb : listeDeclMethod.getListSymbolMethod()){
                if (getMembers().get(symb) != null){
                    setNumberMethods.add(((MethodDefinition) localEnv.get(symb)).getIndex());
                }
            }
            return setNumberMethods;
        }
        return setNumberMethods;
    }

    /**
     * Cette méthode nous permet de redéfinier d'une façon correcte les méthodes de la super classe dans l'environment de la sous classe
     * En prenant en considération les méthodes en commun (Override)
     * @param superClassDef
     * @param indeXDecalage
     * 
     */
    public void resetIndexMethod(ClassDefinition superClassDef, List<Integer> indeXDecalage){
        EnvironmentExp localEnvSuperClass = superClassDef.getMembers();
        for (Symbol name : localEnvSuperClass.getDictionnaireDef().keySet()){
            if (localEnvSuperClass.get(name).isMethod() && getMembers().getWithoutParent(name) == null){
                MethodDefinition oldMethod = ((MethodDefinition) localEnvSuperClass.get(name));
                int countIndexSup = 0;
                for (Integer i : indeXDecalage){
                    if (oldMethod.getIndex() > i){
                        countIndexSup++;
                    }
                }
                MethodDefinition method = new MethodDefinition(oldMethod.getType(), oldMethod.getTaille(), oldMethod.getLocation(), oldMethod.getSignature(), oldMethod.getIndex()-countIndexSup);
                method.setIsAdded();
                method.setClassIn(oldMethod.getClassIn());
                try{
                    getMembers().declare(name, method);
                }catch(DoubleDefException e){
                    throw new UnsupportedOperationException();
                }
            }
        }
    }

    public ClassDefinition(ClassType type, Location location, ClassDefinition superClass) {
        super(type, location);
        EnvironmentExp parent;
        if (superClass != null) {
            parent = superClass.getMembers();
        } else {
            parent = null;
        }
        members = new EnvironmentExp(parent);
        this.superClass = superClass;
    }
    
    /**
     * Compare cette instance de ClassDefinition avec un autre objet.
     * Deux classes sont considérées comme égales si elles ont le même type,
     * la même localisation et la même superclasse.
     *
     * @param obj L'objet à comparer avec cette instance.
     * @return true si l'objet spécifié est égal à cette instance, sinon false.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ClassDefinition other = (ClassDefinition) obj;
        return Objects.equals(getType(), other.getType()) &&
               Objects.equals(getLocation(), other.getLocation()) &&
               Objects.equals(getSuperClass(), other.getSuperClass());
    }
    /**
     * Cette méthode est une redéfinition de la méthode hashcode.
     * @return
     * 
     */

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getLocation(), getSuperClass());
    }

    /**
     * Détermine si la classe passée en paramètre est une superclasse de cette classe.
     * La méthode remonte récursivement dans la hiérarchie des superclasses.
     *
     * @param classe La classe potentiellement superclasse.
     * @return true si la classe donnée est une superclasse (directe ou indirecte) de cette classe,
     *         ou si elle est égale à cette classe. Retourne false si aucune correspondance n'est trouvée.
     */
    public boolean isSubClass(ClassDefinition classe) {
        if (this.equals(classe)) {
            return true;  
        }
        if (getSuperClass() == null) {
            return false;  
        }
        return getSuperClass().isSubClass(classe); 
    }

    public DAddr getAddressInVtable() {
        return addressInVtable;
    }

    public void setAddressInVtable(DAddr addressInVtable) {
        this.addressInVtable = addressInVtable;
    }

}
