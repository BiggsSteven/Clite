/*
 * TypeMap.java implements the type map for storing
 * the variables declared in the program.
 */

package clite;

import java.util.*;

public class TypeMap extends HashMap<Variable, Type> {

  public void display () {
    System.out.print("{ ");
    String sep = "";
    for (Variable key : keySet() ) {
        System.out.print(sep + "<" + key + ", " + get(key).getId() + ">");
        sep = ", ";
    }
    System.out.println(" }");
  }
}