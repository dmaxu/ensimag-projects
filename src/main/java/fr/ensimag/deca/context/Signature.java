package fr.ensimag.deca.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Signature of a method (i.e. list of arguments)
 *
 * @author gl47
 * @date 01/01/2025
 */
public class Signature {
    List<Type> args = new ArrayList<Type>();

    public void add(Type t) {
        args.add(t);
    }

    public Type getTypeElement(int index){
        return args.get(index);
    }
    
    public Type paramNumber(int n) {
        return args.get(n);
    }
    
    public int size() {
        return args.size();
    }

    // Cette méthode nous permet de retourner la signature.
    @Override
    public String toString(){
        // mot de début:
        String signString = "";
        // Boucle sur les types dans la signature
        for (Type type : this.args){
            // Ajoute du type.
            signString += type.getName().getName() + " ";
        }
        // Ajoute du point
        signString += ".";
        return signString;
    }

    @Override
    public boolean equals(Object obj) {
        // On compare la référence.
        if (this == obj) {
            return true; 
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;  
        }
        Signature other = (Signature) obj;
        // Comparaison de la taille
        if (this.size() != other.size()) {
            return false;
        }
        return Objects.equals(this.args, other.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(args); 
    }
}