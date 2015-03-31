/*
 * Semantics.java implements the semantic interpreter for Clite. It is defined
 * by the functions M that use the classes in the Abstract Syntax of Clite.
 */

package clite;

import java.util.ArrayList;

public class Semantics {

    State M(Program p) {
        //Program = Declarations decpart; Block body
        return M(p.body, initialState(p.decpart));
    }

    State initialState (Declarations d){
        //Declaration = Variable v; Type t
        //State state = new State();
        Enviroment enviroment = new Enviroment();
        Memory memory = new Memory();
        Value intUndef = new IntValue();
        IntValue memStart = new IntValue(0);
        IntValue heapStart = new IntValue(512);
        for(Declaration decl:d)
            if (decl instanceof VariableDecl) {
                VariableDecl vd = (VariableDecl) decl;
                //ArrayList<Value> val = new ArrayList<Value>();
                //val.add(Value.mkValue(vd.t));
                //state.put(vd.v, val);
                
                IntValue tempVal = new IntValue(0);
                tempVal.value = memStart.value;
                ArrayList<IntValue> val2 = new ArrayList<IntValue>();
                val2.add(tempVal);
                enviroment.put(vd.v,tempVal);
                memory.put(tempVal,Value.mkValue(vd.t));
                memStart.value = memStart.value + newAD(1,vd.t);
                
            }
            else if (decl instanceof ArrayDecl) {
                ArrayDecl ad = (ArrayDecl) decl; 
                //ArrayList<Value> val = new ArrayList<Value>();
                
                IntValue tempVal = new IntValue(memStart.value);
                ArrayList<IntValue> val2 = new ArrayList<IntValue>();
                val2.add(tempVal);
                enviroment.put(new ArrayRef(ad.v.id, ad.s),tempVal);
                memory.put(tempVal,Value.mkValue(ad.t));
                memStart.value = memStart.value + newAD(ad.s.intValue(),ad.t);
                for (int i = 0; i < ad.s.intValue(); i++){
                    //val.add(Value.mkValue(ad.t));
                    tempVal = new IntValue(heapStart.value);
                    memory.put(tempVal,Value.mkValue(ad.t));
                    heapStart.value = heapStart.value + newAD(1,ad.t);
                }
                //state.put(new ArrayRef(ad.v.id, ad.s), val);
                
                
                
            }
            
            State state = new State(enviroment, memory);
        return state;
    }
    
    int newAD (int size, Type type) {
        return size * type.getSize();
    }
    
    State M(Statement s, State state){
        if(s instanceof Skip) return M((Skip)s, state);
        if(s instanceof Assignment)return M((Assignment)s, state);
        if(s instanceof Conditional)return M((Conditional)s, state);
        if(s instanceof Loop) return M((Loop)s, state);
        if(s instanceof Block) return M((Block)s, state);
        throw new IllegalArgumentException("should never reach here");
    }
    
    State M(Skip s, State state){
        //Skip = 
        return state;
    }
    
    State M(Assignment a, State state){
        //Assignment = Variable target; Expression source
        if (a.target instanceof Variable)
            return state.update(a.target, M (a.source, state));
        else if (a.target instanceof ArrayRef)
            return state.update(a.target, ((IntValue)M(((ArrayRef)a.target).index, state)).intValue(), M(a.source, state));
        else
            return state;
    }
    
    State M(Conditional c, State state){
        //Conditional = Expression test; Statement thenbranch, elsebranch
        if (M(c.test, state).boolValue())
            return M (c.thenbranch, state);
        else
            return M (c.elsebranch, state);
        
    }
    
    State M(Loop l, State state) {
        //Loop = Expression test; Statement body
        if (M (l.test, state).boolValue())
            return M(l, M (l.body, state));
        else return state;
    }
    
    State M(Block b, State state) {
        //Block = Statement*
        for (Statement s : b.members)
            state = M (s, state);
        return state;
    }
    
    Value M(Expression e, State state){
        if(e instanceof Value) return (Value)e;
        if(e instanceof Variable) return (Value)(state.m.get(state.e.get(e)));
        if (e instanceof ArrayRef) {
//            return (Value)(state.get(e));
            int id = ((IntValue)M(((ArrayRef)e).index, state)).intValue();
            return (Value)(state.e.get(e));
        }
        if(e instanceof Binary){
            Binary b = (Binary)e;
            return applyBinary(b.op, M(b.term1, state),M(b.term2, state));
        }
        if(e instanceof Unary){
            Unary u = (Unary)e;
            return applyUnary(u.op,M(u.term, state));
        }
        throw new IllegalArgumentException("should never reach here");
    }
    
    Value applyBinary(Operator op, Value v1, Value v2){
        StaticTypeCheck.check(!v1.isUndef() && ! v2.isUndef(), "reference to undef value");
        if(op.val.equals(Operator.INT_PLUS))
            return new IntValue(v1.intValue() + v2.intValue());
        if(op.val.equals(Operator.INT_MINUS))
            return new IntValue(v1.intValue() - v2.intValue());
        if (op.val.equals(Operator.INT_TIMES))
            return new IntValue(v1.intValue() * v2.intValue());
        if (op.val.equals(Operator.INT_DIV))
            return new IntValue(v1.intValue() / v2.intValue());
        if (op.val.equals(Operator.INT_MOD))
            return new IntValue(v1.intValue() % v2.intValue());
        if (op.val.equals(Operator.INT_LT))
            return new BoolValue(v1.intValue() < v2.intValue());
        if (op.val.equals(Operator.INT_LE))
            return new BoolValue(v1.intValue() <= v2.intValue());
        if (op.val.equals(Operator.INT_EQ))
            return new BoolValue(v1.intValue() == v2.intValue());
        if (op.val.equals(Operator.INT_NE))
            return new BoolValue(v1.intValue() != v2.intValue());
        if (op.val.equals(Operator.INT_GE))
            return new BoolValue(v1.intValue() >= v2.intValue());
        if (op.val.equals(Operator.INT_GT))
            return new BoolValue(v1.intValue() > v2.intValue());
        if (op.val.equals(Operator.FLOAT_PLUS))
            return new FloatValue(v1.floatValue() + v2.floatValue());
        if (op.val.equals(Operator.FLOAT_MINUS))
            return new FloatValue(v1.floatValue() - v2.floatValue());
        if (op.val.equals(Operator.FLOAT_TIMES))
            return new FloatValue(v1.floatValue() * v2.floatValue());
        if (op.val.equals(Operator.FLOAT_DIV))
            return new FloatValue(v1.floatValue() / v2.floatValue());
        if (op.val.equals(Operator.FLOAT_MOD))
            return new FloatValue(v1.floatValue() % v2.floatValue());
        if (op.val.equals(Operator.FLOAT_LT))
            return new BoolValue(v1.floatValue() < v2.floatValue());
        if (op.val.equals(Operator.FLOAT_LE))
            return new BoolValue(v1.floatValue() <= v2.floatValue());
        if (op.val.equals(Operator.FLOAT_EQ))
            return new BoolValue(v1.floatValue() == v2.floatValue());
        if (op.val.equals(Operator.FLOAT_NE))
            return new BoolValue(v1.floatValue() != v2.floatValue());
        if (op.val.equals(Operator.FLOAT_GE))
            return new BoolValue(v1.floatValue() >= v2.floatValue());
        if (op.val.equals(Operator.FLOAT_GT))
            return new BoolValue(v1.floatValue() > v2.floatValue());
        if (op.val.equals(Operator.CHAR_LT))
            return new BoolValue(v1.charValue() < v2.charValue());
        if (op.val.equals(Operator.CHAR_LE))
            return new BoolValue(v1.charValue() <= v2.charValue());
        if (op.val.equals(Operator.CHAR_EQ))
            return new BoolValue(v1.charValue() == v2.charValue());
        if (op.val.equals(Operator.CHAR_NE))
            return new BoolValue(v1.charValue() != v2.charValue());
        if (op.val.equals(Operator.CHAR_GE))
            return new BoolValue(v1.charValue() >= v2.charValue());
        if (op.val.equals(Operator.CHAR_GT))
            return new BoolValue(v1.charValue() > v2.charValue());
        if (op.val.equals(Operator.BOOL_LT))
            return new BoolValue(v1.intValue() < v2.intValue());
        if (op.val.equals(Operator.BOOL_LE))
            return new BoolValue(v1.intValue() <= v2.intValue());
        if (op.val.equals(Operator.BOOL_EQ))
            return new BoolValue(v1.boolValue() == v2.boolValue());
        if (op.val.equals(Operator.BOOL_NE))
            return new BoolValue(v1.boolValue() != v2.boolValue());
        if (op.val.equals(Operator.BOOL_GE))
            return new BoolValue(v1.intValue() >= v2.intValue());
        if (op.val.equals(Operator.BOOL_GT))
            return new BoolValue(v1.intValue() > v2.intValue());
        if (op.val.equals(Operator.AND))
            return new BoolValue(v1.boolValue( ) ? v2.boolValue( ) : false);
//            return new BoolValue(v1.boolValue( ) && v2.boolValue( ));
        if (op.val.equals(Operator.OR))
            return new BoolValue(v1.boolValue( ) ? true : v2.boolValue( ));
//            return new BoolValue(v1.boolValue( ) || v2.boolValue( ));
        throw new IllegalArgumentException("should never reach here");  
    }
    
        Value applyUnary (Operator op, Value v) {
        StaticTypeCheck.check( ! v.isUndef(),
               "reference to undef value");
        if (op.val.equals(Operator.NOT))
            return new BoolValue(!v.boolValue());
        else if (op.val.equals(Operator.INT_NEG))
            return new IntValue(-v.intValue());
        else if (op.val.equals(Operator.FLOAT_NEG))
            return new FloatValue(-v.floatValue());
        else if (op.val.equals(Operator.I2F))
            return new FloatValue((float)(v.intValue()));
        else if (op.val.equals(Operator.F2I))
            return new IntValue((int)(v.floatValue()));
        else if (op.val.equals(Operator.C2I))
            return new IntValue((int)(v.charValue()));
        else if (op.val.equals(Operator.I2C)){
            if (v.intValue() <= 255 && v.intValue() >= 0 ){
              return new CharValue((char)(v.intValue()));  
            }
            else {
                throw new IllegalArgumentException("should never reach here: "
                                                    + v.intValue() + " is out or CharValue range");
            }
        }
        throw new IllegalArgumentException("should never reach here: "
                                           + op.toString());
    }
   
        
        
            public static void main(String args[]) {
    	System.out.println("Begin parsing... " + args[0]);
    	Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display();

        System.out.println("\nBegin type checking..." + args[0]);
        System.out.println("\nType map:");
        TypeMap map = StaticTypeCheck.typing(prog.decpart);
        map.display();

        StaticTypeCheck.V(prog);
        Program out = TypeTransformer.T(prog, map);
        System.out.println("\nTransformed Abstract Syntax Tree");
        out.display();

        System.out.println("\nBegin interpreting..." + args[0]);
        Semantics semantics = new Semantics( );
        State state = semantics.M(out);
        System.out.println("\nFinal State");
        state.display( );
    }
        
}

