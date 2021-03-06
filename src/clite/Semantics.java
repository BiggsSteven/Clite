/*
 * Semantics.java implements the semantic interpreter for Clite. It is defined
 * by the functions M that use the classes in the Abstract Syntax of Clite.
 */

package clite;

import java.util.ArrayList;

public class Semantics {

    State M (Program p) {
        //Program = Declarations decpart; Block body
        //return M (p.body, initialState(p.decpart));
        
        return initialState(p.globals);
    }

    State initialState (Declarations d) {
        //Declaration = VariableRef v, Type t
        State state = new State();
        for (Declaration decl : d)
//            state.put(decl.v, Value.mkValue(decl.t));
            if (decl instanceof VariableDecl) {
                VariableDecl vd = (VariableDecl) decl; 
//                state.put(vd.v, Value.mkValue(vd.t));
                ArrayList<Value> val = new ArrayList<Value>();
                val.add(Value.mkValue(vd.t));
                state.put(vd.v, val);
            }
            else if (decl instanceof ArrayDecl) {
                ArrayDecl ad = (ArrayDecl) decl; 
//                state.put(new ArrayRef(ad.v.id, ad.s), Value.mkValue(ad.t));
                ArrayList<Value> val = new ArrayList<Value>();
                for (int i = 0; i < ad.s.intValue(); i++)
                    val.add(Value.mkValue(ad.t));
                state.put(new ArrayRef(ad.v.id, ad.s), val);
            }
        return state;
    }


    State M (Statement s, State state) {
        //Statement = Skip | Assignment | Conditional | Loop | Block
        if (s instanceof Skip) return M((Skip)s, state);
        if (s instanceof Assignment)  return M((Assignment)s, state);
        if (s instanceof Conditional)  return M((Conditional)s, state);
        if (s instanceof Loop)  return M((Loop)s, state);
        if (s instanceof Block)  return M((Block)s, state);
        throw new IllegalArgumentException("should never reach here");
    }

    State M (Skip s, State state) {
        //Skip = 
        return state;
    }

    State M (Assignment a, State state) {
        //Assignment = VariableRef target; Expression source
//        return state.update(a.target, M (a.source, state));
        if (a.target instanceof Variable)
            return state.update(a.target, M (a.source, state));
        else if (a.target instanceof ArrayRef)
            return state.update(a.target, M(((ArrayRef)a.target).index, state).intValue(), M(a.source, state));
        else
            return state;
    }

    State M (Block b, State state) {
        //Block = Statement*
        for (Statement s : b.members)
            state = M (s, state);
        return state;
    }

    State M (Conditional c, State state) {
        //Conditional = Expression test; Statement thenbranch, elsebranch
        if (M(c.test, state).boolValue( ))
            return M (c.thenbranch, state);
        else
            return M (c.elsebranch, state);
    }

    State M (Loop l, State state) {
        //Loop = Expression test; Statement body
        if (M (l.test, state).boolValue( ))
            return M(l, M (l.body, state));
        else return state;
//        while (M (l.test, state).boolValue()) {
//            state = M (l.body, state);
//        }
//        return state;
    }

    Value M (Expression e, State state) {
        //Expression = Value | VariableRef | Binary | Unary
        //VariableRef = Variable | ArrayRef

        //Value = return object
        if (e instanceof Value)
            return (Value)e;
 
        //Variable = String id;
        if (e instanceof Variable)
            return state.get(e);
//            return (Value)(state.get(e).get(0));

        //ArrayRef = String id; Expression index
        if (e instanceof ArrayRef) {
//            return (Value)(state.get(e));
            int index = ((IntValue)M(((ArrayRef)e).index, state)).intValue();
//            return (Value)(state.get(e).get(index));
            return state.get(e, index);
        }

        //Binary = Operator op; Expression term1, term2
        if (e instanceof Binary) {
            Binary b = (Binary)e;
            return applyBinary (b.op, M(b.term1, state), M(b.term2, state));
        }
 
        //Unary = Operator op; Expression term
        if (e instanceof Unary) {
            Unary u = (Unary)e;
            return applyUnary(u.op, M(u.term, state));
        }
        throw new IllegalArgumentException("should never reach here");
    }

    Value applyBinary (Operator op, Value v1, Value v2) {
        StaticTypeCheck.check( ! v1.isUndef( ) && ! v2.isUndef( ),
               "reference to undef value");
        
        // INT operators
        if (op.val.equals(Operator.INT_PLUS))
            return new IntValue(v1.intValue( ) + v2.intValue( ));
        if (op.val.equals(Operator.INT_MINUS))
            return new IntValue(v1.intValue( ) - v2.intValue( ));
        if (op.val.equals(Operator.INT_TIMES))
            return new IntValue(v1.intValue( ) * v2.intValue( ));
        if (op.val.equals(Operator.INT_DIV))
            return new IntValue(v1.intValue( ) / v2.intValue( ));
        if (op.val.equals(Operator.INT_MOD))
            return new IntValue(v1.intValue( ) % v2.intValue( ));
        if (op.val.equals(Operator.INT_LT))
            return new BoolValue(v1.intValue( ) < v2.intValue( ));
        if (op.val.equals(Operator.INT_LE))
            return new BoolValue(v1.intValue( ) <= v2.intValue( ));
        if (op.val.equals(Operator.INT_EQ))
            return new BoolValue(v1.intValue( ) == v2.intValue( ));
        if (op.val.equals(Operator.INT_NE))
            return new BoolValue(v1.intValue( ) != v2.intValue( ));
        if (op.val.equals(Operator.INT_GE))
            return new BoolValue(v1.intValue( ) >= v2.intValue( ));
        if (op.val.equals(Operator.INT_GT))
            return new BoolValue(v1.intValue( ) > v2.intValue( ));
        
        // FLOAT operators
        if (op.val.equals(Operator.FLOAT_PLUS))
            return new FloatValue(v1.floatValue( ) + v2.floatValue( ));
        if (op.val.equals(Operator.FLOAT_MINUS))
            return new FloatValue(v1.floatValue( ) - v2.floatValue( ));
        if (op.val.equals(Operator.FLOAT_TIMES))
            return new FloatValue(v1.floatValue( ) * v2.floatValue( ));
        if (op.val.equals(Operator.FLOAT_DIV))
            return new FloatValue(v1.floatValue( ) / v2.floatValue( ));
        if (op.val.equals(Operator.FLOAT_LT))
            return new BoolValue(v1.floatValue( ) < v2.floatValue( ));
        if (op.val.equals(Operator.FLOAT_LE))
            return new BoolValue(v1.floatValue( ) <= v2.floatValue( ));
        if (op.val.equals(Operator.FLOAT_EQ))
            return new BoolValue(v1.floatValue( ) == v2.floatValue( ));
        if (op.val.equals(Operator.FLOAT_NE))
            return new BoolValue(v1.floatValue( ) != v2.floatValue( ));
        if (op.val.equals(Operator.FLOAT_GE))
            return new BoolValue(v1.floatValue( ) >= v2.floatValue( ));
        if (op.val.equals(Operator.FLOAT_GT))
            return new BoolValue(v1.floatValue( ) > v2.floatValue( ));
        
        // CHAR operators
        if (op.val.equals(Operator.CHAR_LT))
            return new BoolValue(v1.charValue( ) < v2.charValue( ));
        if (op.val.equals(Operator.CHAR_LE))
            return new BoolValue(v1.charValue( ) <= v2.charValue( ));
        if (op.val.equals(Operator.CHAR_EQ))
            return new BoolValue(v1.charValue( ) == v2.charValue( ));
        if (op.val.equals(Operator.CHAR_NE))
            return new BoolValue(v1.charValue( ) != v2.charValue( ));
        if (op.val.equals(Operator.CHAR_GE))
            return new BoolValue(v1.charValue( ) >= v2.charValue( ));
        if (op.val.equals(Operator.CHAR_GT))
            return new BoolValue(v1.charValue( ) > v2.charValue( ));
        
        // BOOL operators
        // Relational operations <, <=, >, >= are legal in C (and hence CLite)
        // as booleans are stored as integers in it; illegal in modern languages
        if (op.val.equals(Operator.BOOL_LT))
            throw new IllegalArgumentException("inappropriate operation with boolean operands");
//            return new BoolValue(v1.intValue( ) < v2.intValue( ));
        if (op.val.equals(Operator.BOOL_LE))
            throw new IllegalArgumentException("inappropriate operation with boolean operands");
//            return new BoolValue(v1.intValue( ) <= v2.intValue( ));
        if (op.val.equals(Operator.BOOL_EQ))
            return new BoolValue(v1.boolValue( ) == v2.boolValue( ));
        if (op.val.equals(Operator.BOOL_NE))
            return new BoolValue(v1.boolValue( ) != v2.boolValue( ));
        if (op.val.equals(Operator.BOOL_GE))
            throw new IllegalArgumentException("inappropriate operation with boolean operands");
//            return new BoolValue(v1.intValue( ) >= v2.intValue( ));
        if (op.val.equals(Operator.BOOL_GT))
            throw new IllegalArgumentException("inappropriate operation with boolean operands");
//            return new BoolValue(v1.intValue( ) > v2.intValue( ));
        
        // Boolean operators performing short-circuit evaluation
        if (op.val.equals(Operator.AND))
            return new BoolValue(v1.boolValue( ) ? v2.boolValue( ) : false);
//            return new BoolValue(v1.boolValue( ) && v2.boolValue( ));
        if (op.val.equals(Operator.OR))
            return new BoolValue(v1.boolValue( ) ? true : v2.boolValue( ));
//            return new BoolValue(v1.boolValue( ) || v2.boolValue( ));
        throw new IllegalArgumentException("should never reach here");
    }

    Value applyUnary (Operator op, Value v) {
        StaticTypeCheck.check( ! v.isUndef( ),
               "reference to undef value");
        if (op.val.equals(Operator.NOT))
            return new BoolValue(!v.boolValue( ));
        else if (op.val.equals(Operator.INT_NEG))
            return new IntValue(-v.intValue( ));
        else if (op.val.equals(Operator.FLOAT_NEG))
            return new FloatValue(-v.floatValue( ));
        else if (op.val.equals(Operator.I2F))
            return new FloatValue((float)(v.intValue( )));
        else if (op.val.equals(Operator.F2I))
            return new IntValue((int)(v.floatValue( )));
        else if (op.val.equals(Operator.C2I))
            return new IntValue((int)(v.charValue( )));
        else if (op.val.equals(Operator.I2C))
            return new CharValue((char)(v.intValue( )));
        throw new IllegalArgumentException("should never reach here");
    }

//    public static void main(String args[]) {
//    	System.out.println("Begin parsing... " + args[0]);
//    	Parser parser  = new Parser(new Lexer(args[0]));
//        Program prog = parser.program();
//        prog.display();
//
//        System.out.println("\nBegin type checking..." + args[0]);
//        System.out.println("\nType map:");
//        TypeMap map = StaticTypeCheck.typing(prog.decpart);
//        map.display();
//
//        StaticTypeCheck.V(prog);
//        Program out = TypeTransformer.T(prog, map);
//        System.out.println("\nTransformed Abstract Syntax Tree");
//        out.display();
//
//        System.out.println("\nBegin interpreting..." + args[0]);
//        Semantics semantics = new Semantics( );
//        State state = semantics.M(out);
//        System.out.println("\nFinal State");
//        state.display( );
//    }
}