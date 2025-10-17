package fr.ensimag.deca.tools;

import static org.mockito.Mockito.never;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

/**
 * Manage unique symbols.
 * 
 * A Symbol contains the same information as a String, but the SymbolTable
 * ensures the uniqueness of a Symbol for a given String value. Therefore,
 * Symbol comparison can be done by comparing references, and the hashCode()
 * method of Symbols can be used to define efficient HashMap (no string
 * comparison or hashing required).
 * 
 * @author gl47
 * @date 01/01/2025
 */
public class SymbolTable {
    private HashMap<String, Symbol> map = new HashMap<String, Symbol>();

    /**
     * Create or reuse a symbol.
     * 
     * If a symbol already exists with the same name in this table, then return
     * this Symbol. Otherwise, create a new Symbol and add it to the table.
     */
    public Symbol create(String name) {
        Validate.notNull(name, "symbole is NULL");
        Symbol symbol;
        if (this.map.containsKey(name)) {
            symbol=this.map.get(name);
        }else{
            symbol=new Symbol(name);
            this.map.put(name, symbol);
        }
        return symbol;
    }

    public static class Symbol {
        // Constructor is private, so that Symbol instances can only be created
        // through SymbolTable.create factory (which thus ensures uniqueness
        // of symbols).
        private Symbol(String name) {
            super();
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Symbol symbol = (Symbol) obj;
            return name.equals(symbol.name);
        }
    
        @Override
        public int hashCode() {
            return name.hashCode();
        }
        
        private String name;
    }
}
