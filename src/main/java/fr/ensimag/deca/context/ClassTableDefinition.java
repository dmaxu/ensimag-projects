package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.Location;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.Type;

public class ClassTableDefinition extends TypeDefinition{
    private ClassDefinition classDef;

    public ClassTableDefinition(Type type, Location location, ClassDefinition classDef){
        super(type , location);
        this.classDef = classDef;
    }
}