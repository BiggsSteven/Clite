/*
 * State.java represents a program state. It defines the set of variables
 * and their associated values that are active during interpretation.
 */

package clite;

import java.util.*;

//public class State extends HashMap<VariableRef, ArrayList<Value>> {
//
//    public State( ) { }
//
////    public State(VariableRef key, Value val) {
////        put(key, val);
////    }
//
//    public State update(VariableRef key, Value val) {
////        put(key, val);
//        if (key instanceof Variable) {
//            ArrayList<Value> value = get(key);
//            value.set(0, val);
//        }
//        else 
//            throw new IllegalArgumentException("not a variable reference");
//        return this;
//    }
//
//    public State update(VariableRef key, int id, Value val) {
////        put(key, val);
//        if (key instanceof ArrayRef) {
//            ArrayList<Value> values = get(key);
//            values.set(id, val);
//        }
//        else 
//            throw new IllegalArgumentException("not an array reference");
//        return this;
//    }
//
//    public State update (State t) {
//        for (VariableRef key : t.keySet( ))
//            put(key, t.get(key));
//        return this;
//    }
//
//    public void display( ) {
//        System.out.print("{ ");
//        String sep = "";
////        for (VariableRef key : keySet( )) {
////            System.out.print(sep + "<" + key + ", " + get(key) + ">");
////            sep = ", ";
////        }
//        for (VariableRef key : keySet( )) {
//            if (key instanceof Variable)
//                System.out.print(sep + "<" + key + ", " + get(key).get(0) + ">");
//            else
//                System.out.print(sep + "<" + key + ", " + get(key) + ">");
//            sep = ", ";
//        }
//        System.out.println(" }");
//    }
//}

// Represents the program state as it executes and simulates its runtime behavior
public class State {

    Environment env;    // Represents the environment of the active function
    Memory mem;         // Represents the current state of the entire address space

    public State( ) { 
        
        env = new Environment();
        mem = new Memory();
        
        env.setMemory(mem);
        mem.setEnvironment(env);
    }
    
    // Add information about a new VariableRef in environment and memory
    void put(VariableRef vr, ArrayList<Value> val) {
        
        if (vr instanceof Variable) {
            int addr = mem.pushVariable(val.get(0));
            env.put(vr, addr);
        }
        else if (vr instanceof ArrayRef) {
            int addr = mem.pushArrayRef(val);
            env.put(vr, addr);
        }
        else throw new IllegalArgumentException("should never reach here");
    }

    // Update the target variable's value in memory
    State update(VariableRef target, Value val) {
        
        if (target instanceof Variable) {
            mem.put(env.get(target), val);
            return this;
        }
        else throw new IllegalArgumentException("not a variable reference");
    }

    // Update the target array's value at position index in memory
    State update(VariableRef target, int index, Value val) {
        
        if (target instanceof ArrayRef) {
            DopeVector dv = (DopeVector)mem.get(env.get(target));
            int addr = dv.address + dv.type.getSize() * index;
            if (addr >= dv.address + dv.size)
                throw new IllegalArgumentException("array out of bounds");
            mem.put(addr, val);
            return this;
        }
        else throw new IllegalArgumentException("not an array reference");
    }

    // Get the value for variable e
    Value get(Expression e) {
        
        if (e instanceof Variable) {
            return (Value) mem.get(env.get(e));
        }
        else throw new IllegalArgumentException("not a variable reference");
    }

    // Get the value for array e at position index
    Value get(Expression e, int index) {
        
        if (e instanceof ArrayRef) {
            DopeVector dv = (DopeVector)mem.get(env.get(e));
            int addr = dv.address + dv.type.getSize() * index;
            if (addr >= dv.address + dv.size)
                throw new IllegalArgumentException("array out of bounds");
            return (Value) mem.get(addr);
        }
        else throw new IllegalArgumentException("not an array reference");
    }

    void display() {
        
        env.display();
        mem.display();
    }    
    
}

// Represents the environment of the active function "f";
// Maps VariableRefs in the scope of "f" with specific memory addresses
// (or first of a contiguous block of memory addresses) that hold their information
class Environment extends HashMap<VariableRef, Integer> {
    
    Memory mem;

    Environment() { }

    public void setMemory(Memory mem) {
        this.mem = mem;
    }
    
    // Get the VariableRef defined at the specified address in memory
    VariableRef find(int addr) {
        if (addr < Memory.STACK_LIMIT) {
            for (VariableRef vr : keySet()) {
                if (get(vr) == addr)
                    return vr;
            }
            throw new IllegalArgumentException("should never reach here");
        }
        else {
            for (VariableRef vr : keySet()) {
                if (vr instanceof ArrayRef) {
                    DopeVector dv = (DopeVector) mem.get(get(vr));
                    for (int i=0, a=0; a<dv.size; i++, a+=dv.type.getSize()) 
                        if (addr == dv.address + a)
                            return new ArrayRef(vr.id(), new IntValue(i));
                }
            }
        }
        throw new IllegalArgumentException("should never reach here");
    }

    // Display the environment map
    void display() {
        
        System.out.println("\nEnvironment:");
        System.out.println("\n    VariableRef  MemoryAddr  MemoryValue(from Memory)");
        for (VariableRef vr : keySet()) {
            if (vr instanceof Variable)
                System.out.printf("    %-12s %-11d %s\n", vr, get(vr), mem.get(get(vr)));
            else if (vr instanceof ArrayRef)
                System.out.printf("    %-12s %-11d %s\n", vr, get(vr), mem.get(get(vr)));
            else throw new IllegalArgumentException("should never reach here");
        }
    }
    
}

// Represents the kind of values stored in memory;
// Valid values are Value and DopeVector objects
interface MemValue {

    // Returns the size of this object; used to reserve space on the stack
    int getSize();  
}

// Represents a dope vector for arrays
class DopeVector implements MemValue {
    
    int address;    // addr(A[0]) for this array, A
    int size;       // Total amount of memory allocated for A in the heap
    Type type;      // Type of data stored in A

    public DopeVector(int address, int size, Type type) {
        this.address = address;
        this.size = size;
        this.type = type;
    }

    // Specifies the size of the dope vector for A in the runtime stack
    @Override
    public int getSize() {
        int intSize = new Type("int").getSize();
        return 2 * intSize + type.getSize();
    }

    @Override
    public String toString() {
        return "DopeVector{" + "addr[0]=" + address + ", size=" + size + ", type=" + type.getId() + '}';
    }
}

// Represents the program's address space;
// Maps memory addresses with the values held in them
class Memory extends TreeMap<Integer, MemValue> {
    
    Environment env;
    
    final static int STACK_LIMIT = 512; // Sets the limits on how big the stack and heap areas
    final static int HEAP_LIMIT = 512;  // are allowed to grow; simulates memory management at run-time
    
    int s = 0;              // Captures the address where the static area ends
    int a = 0;              // Next available memory address in stack area
    int h = STACK_LIMIT;    // Next available memory address in heap area
    int n = h + HEAP_LIMIT; // Represents the total memory available

    Memory() { }

    public void setEnvironment(Environment env) {
        this.env = env;
    }

    // Store a variable in the stack area
    int pushVariable(Value val) {
        
        int typeSize = val.type().getSize();
        if (a + typeSize > STACK_LIMIT)
            throw new IllegalArgumentException("Stack overflow!");
        
        int addr = a;
        put(a, val);
        a += typeSize;
        
        return addr;
    }

    // Store an array; dope vector in the stack area, contents in the heap area
    int pushArrayRef(ArrayList<Value> val) {
        
        Type type = val.get(0).type();
        int typeSize = type.getSize();
        int arraySize = val.size();
        int heapSize = arraySize * typeSize;
        
        if (h + heapSize > n)
            throw new IllegalArgumentException("Heap overflow!");
        
        DopeVector dv = new DopeVector(h, heapSize, type);

        if (a + dv.getSize() > STACK_LIMIT)
            throw new IllegalArgumentException("Stack overflow!");
        
        int addr = a;
        put(a, dv);
        a += dv.getSize();
        
        for (Value v : val) {
            put(h, v);
            h += typeSize;
        }
        
        return addr;
    }

    // Display the memory map
    void display() {

        System.out.println("\nMemory:");
        for (Integer addr : keySet()) {

            if (addr == 0) {
                System.out.println("\n  Static area:");
                System.out.println("    MemoryAddr  VarRef(from Env)  Type     MemoryValue");
            }
            if (addr == s) {
                System.out.println("\n  Runtime Stack area:");
                System.out.println("    MemoryAddr  VarRef(from Env)  Type     MemoryValue");
            }
            if (addr == STACK_LIMIT) {
                System.out.println("\n  Heap area:");
                System.out.println("    MemoryAddr  VarRef(from Env)  Type     MemoryValue");
            }

            MemValue mv = get(addr);
            if (mv instanceof Value)
                System.out.printf("    %-11d %-17s %-8s %s\n", addr, env.find(addr), ((Value)mv).type().getId(), mv);
            else if (mv instanceof DopeVector)
                System.out.printf("    %-11d %-17s %-8s %s\n", addr, env.find(addr), ((DopeVector)mv).type.getId(), mv);
            else throw new IllegalArgumentException("should never reach here");

            if (addr == lastKey() && addr < STACK_LIMIT) {
                System.out.println("\n  Heap area:");
                System.out.println("    MemoryAddr  VarRef(from Env)  Type     MemoryValue");
            }
        }
    }
}