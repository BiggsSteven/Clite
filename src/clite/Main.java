/*
 * The driver code for the project
 */

package clite;

public class Main {

    public static void main(String[] args) {
        String filename = "programs/hello.cpp";
    	System.out.println("Begin parsing... " + filename);
    	Parser parser  = new Parser(new Lexer(filename));
        Program prog = parser.program();
        prog.display();      // display abstract syntax tree

//        System.out.println("\nBegin type checking...");
//        System.out.println("\nType map:");
//        TypeMap map = StaticTypeCheck.typing(prog.decpart);
//        map.display();
//        StaticTypeCheck.V(prog);
    }
}
