/*
 * State.java represents a program state. It defines the set of variables
 * and their associated values that are active during interpretation.
 */

package clite;

import java.util.*;

class Enviroment extends HashMap<VariableRef,IntValue> {
    //VariableRef = (id), IntValue = mem
    
    public Enviroment() {}
    
    public void display(){
        System.out.println("Environment:");
        System.out.print("\tVariableRef    MemoryAddr   MemoryValue(from Memory)");
        for (VariableRef key : keySet( )) {
            if (key instanceof Variable)
                System.out.print( "\n\t" + key + "\t\t" + get(key) + "\t\t MemoryValue" );
            else
                System.out.print( "\n\t" + key + "\t\t" + get(key) + "\t\t MemoryValue" );
        }    
    }
}

class Memory extends HashMap<IntValue,Value>{
    //IntValue = mem, Value = Type, and value
    
    public Memory() {}
    
    public void display(){
        System.out.println("\n\nMemory:");
        System.out.println("\n\tStatic area:");
        System.out.println("\t\tMemoryAddr \t VarRef(from Env) \t Type \t MemoryValue");
        System.out.println("\n\tRuntime Stack area:");
        System.out.println("\t\tMemoryAddr \t VarRef(from Env) \t Type \t MemoryValue");
        
        for (IntValue key :keySet()){
            if (key.intValue() < 512)
                System.out.print("\n\t\t" + key + "\t\t VarRef \t\t" + Value.mkValue(get(key).type) + "\t" + get(key));
        }
        
        System.out.println("\nHeap area:");
        System.out.println("\t\tMemoryAddr \t VarRef(from Env) \t Type \t MemoryValue");
        for (IntValue key :keySet()){
            if (key.intValue() >= 512)
                System.out.print("\n\t\t" + key  + "\t\t VarRef \t\t" + Value.mkValue(get(key).type) + "\t" + get(key) );
        } 
        System.out.println("\n");
    }
    
    
}

public class State {
    Enviroment e;
    Memory m;
    
    public State( ) { }

    public State(Enviroment env, Memory mem){
        e = env;
        m = mem;
    }
    
    public State update(VariableRef key, Value val) {
        if (key instanceof Variable) {
//            ArrayList<Value> value = get(key);
//            value.set(0, val);
            m.replace(e.get(key), val);
        }
        else 
            throw new IllegalArgumentException("not a variable reference");
        return this;
    }
//
    public State update(VariableRef key, int id, Value val) {
        if (key instanceof ArrayRef) {
//            ArrayList<Value> values = get(key);
//            values.set(id, val);
            m.replace(e.get(key), val);
            System.out.print("\n" + m.get(e.get(key)));
        }
        else 
            throw new IllegalArgumentException("not an array reference");
        return this;
    }
//
    public State update (State t) {
//        for (VariableRef key : t.keySet( ))
//            put(key, t.get(key));
        return this;
    }
//
    public void display( ) {
//        
        e.display();
        m.display();
//        
//        System.out.print("{ ");
//        String sep = "";
//        for (VariableRef key : keySet( )) {
//            if (key instanceof Variable)
//                System.out.print(sep + "<" + key + ", " + get(key).get(0) + ">");
//            else
//                System.out.print(sep + "<" + key + ", " + get(key) + ">");
//            sep = ", ";
//        }
//        System.out.println(" }");
    }
    
    
}