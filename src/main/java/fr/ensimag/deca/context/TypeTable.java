package fr.ensimag.deca.context;

import java.util.Objects;

import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.Location;

public abstract class TypeTable extends Type{
    private Symbol typeIn;
    // name : contient le nom du type à l'intérieur
    public TypeTable(Symbol name, Symbol typeIn){
        super(name);
        this.typeIn = typeIn;
    }

    public Symbol getTypeIn(){
        return this.typeIn;
    }

    @Override
    public boolean isTable(){
        return true;
    }

    public boolean isClassTable(){
        return false;
    }
    
    public boolean isFloatTable(){
        return false;
    }

    public boolean isIntTable(){
        return false;
    }

    public boolean isStringTable(){
        return false;
    }

    public boolean isBooleanTable(){
        return true;
    }
}