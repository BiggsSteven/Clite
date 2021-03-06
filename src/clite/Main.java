/*
 * The driver code for the project
 */

package clite;

public class Main {

    public static void main(String[] args) {
        String filename = "programs/functions.cpp";
    	System.out.println("Begin parsing... " + filename);
    	Parser parser  = new Parser(new Lexer(filename));
        Program prog = parser.program();
        prog.display();      // display abstract syntax tree

        System.out.println("\nBegin type checking...");
        TypeMap map = StaticTypeCheck.typing(prog.globals);
        //StaticTypeCheck.V(prog, map);
//
        Program out = TypeTransformer.T(prog, map);
        System.out.println("\nTransformed Abstract Syntax Tree");
        out.display();      // display transformed abstract syntax tree
        StaticTypeCheck.V(out, map); // type check the transformed AST
        System.out.println("\nBegin interpreting... " + filename);
        //Semantics semantics = new Semantics( );
        //State state = semantics.M(out);
//        System.out.println("\nFinal State");
//        state.display( );   // display the final program state
    }
}
