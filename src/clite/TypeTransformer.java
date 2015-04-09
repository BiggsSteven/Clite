/*
 * TypeTransformer.java implements the Clite type system that supports
 * implicit type conversion. It transforms the parser generated 
 * abstract syntax tree such that all operators are explicitly typed and
 * implicit conversions are made explicit. It is defined by the functions T,
 * which use classes in the abstract syntax of Clite and return the
 * transformed syntax tree under that node.
 */

package clite;

public class TypeTransformer {
    private static Type returnType;
    private static boolean returnFound = false;
    private static TypeMap functionMap = new TypeMap();
    private static Functions dtFunction = new Functions();
    

//    public static Program T (Program p, TypeMap tm) {
//        
//        
//        
//        Block body = (Block)T(p.body, tm);
//        return new Program(p.decpart, body);
//    }

    public static Program T(Program p, TypeMap GM) {
        dtFunction.addAll(p.functions);
        //Rule 10.1
        Declarations ds = new Declarations();
        ds.addAll(p.globals);
        for (int i=0; i<p.functions.size(); i++) {
            Variable fl = new Variable(p.functions.get(i).id);
            ds.add(new VariableDecl(fl, p.functions.get(i).type));
            functionMap.put(fl, p.functions.get(i).type);
        }
        T(ds);
        //Rule 10.2
        for (Function func : p.functions){
            T(func);
        }
        //Rule 10.3
        Functions NF = new Functions();
        NF = T(p.functions,GM);
        
        return new Program(p.globals, NF);
    }
    
    public static void T (Function f){
        
        Declarations ds = new Declarations();
        ds.addAll(f.params);
        ds.addAll(f.locals);
        T(ds); 
    }
    
    public static Functions T(Functions f, TypeMap tm) {
        Functions NF = new Functions();
        for (Function func : f) {
            TypeMap fMap = new TypeMap();
            fMap.putAll(tm);
            fMap.putAll(StaticTypeCheck.typing(func.params));
            fMap.putAll(StaticTypeCheck.typing(func.locals));
            
            NF.add(T(func, fMap));
        }
        return NF;
    }
    
    public static Function T(Function f, TypeMap tm){
        
        returnType = f.type;
        returnFound = false;
        Block b = (Block)T(f.body,tm);
        Function NF = new Function(f.type,f.id,f.params,f.locals,b);

        //Rule 10.4
        if (!(returnType.equals(Type.VOID)) && !f.id.equals("main")){
            StaticTypeCheck.check((returnFound == true),
                f.id + " is a non-Void function with no Return Statement");
        }
        return NF;
    }    
    
    public static void T(Declarations d) {
        for (int i=0; i<d.size() - 1; i++)
            for (int j=i+1; j<d.size(); j++) {
                Declaration di = d.get(i);
                Declaration dj = d.get(j);
                StaticTypeCheck.check( ! (di.v.equals(dj.v)),
                       "duplicate declaration: " + dj.v);
                //System.out.println(di.v + " = " + dj.v);
            }
    }
    
    public static Expression T (Expression e, TypeMap tm) {
        if (e instanceof Value)
            return e;
        if (e instanceof VariableRef)
            return e;
        if (e instanceof ExpressionCall){
            ExpressionCall c = (ExpressionCall)e;
            StaticTypeCheck.check(!(functionMap.get(new Variable(c.id))).equals(Type.VOID),
                    "Expression Calls must have a return type.");
            for (Function func : dtFunction){
                if (func.id.equals(c.id)){
                    StaticTypeCheck.check (c.arg.size() == func.params.size(),
                            "Arguments and Parameters are different size.");
                    
                    for(int i = 0; i < c.arg.size(); i++){
                        Type ti = ((Type)func.params.get(i).t);
                        Type tj = StaticTypeCheck.typeOf(c.arg.get(i),tm); 
                        StaticTypeCheck.check(ti.equals(tj)
                                , func.params.get(i).t + " is not equal to " + StaticTypeCheck.typeOf(c.arg.get(i),tm));
                    }
                }
                return c;
            }
            
        }
        if (e instanceof Binary) {
            Binary b = (Binary)e;
            Type typ1 = StaticTypeCheck.typeOf(b.term1, tm);
            Type typ2 = StaticTypeCheck.typeOf(b.term2, tm);
            Expression t1 = T (b.term1, tm);
            Expression t2 = T (b.term2, tm);
//            if (typ1 == Type.INT)
//                return new Binary(b.op.intMap(b.op.val), t1,t2);
//            else if (typ1 == Type.FLOAT)
//                return new Binary(b.op.floatMap(b.op.val), t1,t2);
//            else if (typ1 == Type.CHAR)
//                return new Binary(b.op.charMap(b.op.val), t1,t2);
//            else if (typ1 == Type.BOOL)
//                return new Binary(b.op.boolMap(b.op.val), t1,t2);
            if (typ1 == Type.FLOAT || typ2 == Type.FLOAT)
                if (typ1 == Type.INT)
                    return new Binary(b.op.floatMap(b.op.val), new Unary(new Operator(Operator.I2F), t1), t2);
                else if (typ2 == Type.INT)
                    return new Binary(b.op.floatMap(b.op.val), t1, new Unary(new Operator(Operator.I2F), t2));
                else   
                    return new Binary(b.op.floatMap(b.op.val), t1,t2);
            else if (typ1 == Type.INT || typ2 == Type.INT)
                if (typ1 == Type.CHAR)
                    return new Binary(b.op.intMap(b.op.val), new Unary(new Operator(Operator.C2I), t1), t2);
                else if (typ2 == Type.CHAR)
                    return new Binary(b.op.intMap(b.op.val), t1, new Unary(new Operator(Operator.C2I), t2));
                else   
                    return new Binary(b.op.intMap(b.op.val), t1,t2);
            else if (typ1 == Type.CHAR || typ2 == Type.CHAR)
                return new Binary(b.op.charMap(b.op.val), t1,t2);
            else if (typ1 == Type.BOOL || typ2 == Type.BOOL)
                return new Binary(b.op.boolMap(b.op.val), t1,t2);
            throw new IllegalArgumentException("should never reach here");
        }
        if (e instanceof Unary) {
            Unary u = (Unary) e;
//            Type typ1 = StaticTypeCheck.typeOf(u.term, tm);
//            Expression term = T(u.term, tm);
//            Operator op = u.op;
//            if (u.op.equals(Operator.NOT))
//                ;
//            else if (u.op.equals(Operator.NEG)) {
//                if (typ1== Type.INT)
//                    op = op.intMap(op.val);
//                else if (typ1== Type.FLOAT)
//                    op = op.floatMap(op.val);
//            }
//            else if (u.op.equals(Operator.FLOAT))
//                op = op.intMap(op.val);
//            else if (u.op.equals(Operator.CHAR))
//                op = op.intMap(op.val);
//            else if (u.op.equals(Operator.INT)) {
//                if (typ1== Type.FLOAT)
//                    op = op.floatMap(op.val);
//                else if (typ1== Type.CHAR)
//                    op = op.charMap(op.val);
//            }
//            else {
//                throw new IllegalArgumentException("should never reach here");
//            }
//            return new Unary(op, term);
            Type type = StaticTypeCheck.typeOf(u.term, tm);
            Expression term = T(u.term, tm);
            if (type == Type.FLOAT)
                return new Unary(u.op.floatMap(u.op.val), term);
            else if (type == Type.INT)
                return new Unary(u.op.intMap(u.op.val), term);
            else if (type == Type.CHAR)
                return new Unary(u.op.charMap(u.op.val), term);
            else if (type == Type.BOOL)
                return new Unary(u.op.boolMap(u.op.val), term);
            else {
                throw new IllegalArgumentException("should never reach here");
            }
        }
        throw new IllegalArgumentException("should never reach here");
    }

    public static Statement T (Statement s, TypeMap tm) {
        if (s instanceof Skip) return s;
        if (s instanceof Assignment) {
            Assignment a = (Assignment)s;
            VariableRef target = a.target;
            Expression src = T (a.source, tm);
            Type ttype = (Type)tm.get(a.target);
            Type srctype = StaticTypeCheck.typeOf(a.source, tm);
            if (ttype == Type.FLOAT) {
                if (srctype == Type.INT) {
                    src = new Unary(new Operator(Operator.I2F), src);
                    srctype = Type.FLOAT;
                }
            }
            else if (ttype == Type.INT) {
                if (srctype == Type.CHAR) {
                    src = new Unary(new Operator(Operator.C2I), src);
                    srctype = Type.INT;
                }
            }
            StaticTypeCheck.check( ttype == srctype,
                      "bug in assignment to " + target);
            return new Assignment(target, src);
        }
        if (s instanceof Conditional) {
            Conditional c = (Conditional)s;
            Expression test = T (c.test, tm);
            Statement tbr = T (c.thenbranch, tm);
            Statement ebr = T (c.elsebranch, tm);
            return new Conditional(test,  tbr, ebr);
        }
        if (s instanceof Loop) {
            Loop l = (Loop)s;
            Expression test = T (l.test, tm);
            Statement body = T (l.body, tm);
            return new Loop(test, body);
        }
        if (s instanceof Block) {
            Block b = (Block)s;
            Block out = new Block();
            for (Statement stmt : b.members)
                out.members.add(T(stmt, tm));
            return out;
        }
        if (s instanceof Return)
        {
            //Rule 10.5
            StaticTypeCheck.check(!(returnType.equals(Type.VOID)),    
                "Return is not a valid Statement in a Void Function");
            Return r = (Return)s;
            //Rule 10.4
            Return q = new Return(r.target,T(r.returned,tm));
            StaticTypeCheck.check(returnType.equals(StaticTypeCheck.typeOf(q.returned,tm)),
                "The returned type does not match the fuction type;");
            returnFound = true;
            return q;
        }
        if (s instanceof StatementCall){
            
        }
        throw new IllegalArgumentException("should never reach here");
    }


//    public static void main(String args[]) {
//    	System.out.println("Begin parsing... " + args[0]);
//    	Parser parser  = new Parser(new Lexer(args[0]));
//        Program prog = parser.program();
//        prog.display();           // display abstract syntax tree
//        System.out.println("\nBegin type checking...");
//        System.out.println("\nType map:");
//        TypeMap map = StaticTypeCheck.typing(prog.decpart);
//        map.display();
//        StaticTypeCheck.V(prog);
//        Program out = T(prog, map);
//        System.out.println("\nTransformed Abstract Syntax Tree");
//        out.display();
//    } //main

    } // class TypeTransformer
