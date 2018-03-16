import java.util.*;
abstract class Expression {
    public abstract long calculate();
    abstract public String toJSON();
}

enum errorKind { primary, logical, relation, term, factor, none }

class errorIn extends Exception {
    public errorIn(errorKind ek) {
        switch (ek) {   //check if addition or subtraction
            case primary: System.out.println("Oops! Error with number!" + '\n');
                break;
            case logical: System.out.println("Oops! Syntax error in logical expression!" + '\n');
                break;
            case relation: System.out.println("Oops! Syntax error in relational expression!" + '\n');
                break;
            case term: System.out.println("Oops! Syntax error in term expression!" + '\n');
                break;
            case factor: System.out.println("Oops! Syntax error in factor expression!" + '\n');
                break;
            case none: System.out.println("Oops! Syntax error in expression!" + '\n');
                break;
        }
    }

    public void exception() {

    }
}

class Primary extends Expression {
    private int value;

    public Primary(int value) {
        this.value = value;
    }

    public long calculate() {
        return value;
    }

    public String toJSON() {    //Here, we just return value and go to new line
        return value + "\n";
    }
}

class Relation extends Expression{
    enum Opcode {less, less_eq, greater, greater_eq, equal, not_eq, none}
    Opcode op;
    Expression left, right;

    public Relation(Opcode op, Expression left, Expression right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    public long calculate(){
        long l = left.calculate();
        long r = right.calculate();
        switch(op) {    //Check if we have relation operator
            case less: return  l < r ? 1 : 0;
            case less_eq: return  l <= r ? 1 : 0;
            case greater: return  l > r ? 1 : 0;
            case greater_eq: return  l >= r ? 1 : 0;
            case equal: return  l == r ? 1 : 0;
            case not_eq: return  l != r ? 1 : 0;
        }

        return 0;
    }

    public String toJSON() {
        return  "{\n" + "\"left\": " + left.toJSON() + "\"operation\": " + op + "\n" + "\"right\": " + right.toJSON() + "}\n";
        //JSON syntax
    }
}

class Logical extends Expression {
    enum Opcode {and, or, xor, none}

    Opcode op;
    Expression left, right;

    public Logical(Opcode op, Expression left, Expression right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    public long calculate(){
        long l = left.calculate();
        long r = right.calculate();
        switch ( op ) {     //check if we have Logical operators
            case and: return ((l > 0) && (r > 0)) ? 1 : 0;
            case or: return ((l > 0) || (r > 0)) ? 1 : 0;
            case xor: return ((l > 0) ^ (r > 0)) ? 1 : 0;
        }

        return 0;
    }

    public String toJSON() {
        return  "{\n" + "\"left\": " + left.toJSON() + "\"operation\": " + op + "\n" + "\"right\": " + right.toJSON() + "}\n";
        //JSON syntax
    }
}

class Term extends Expression {
    enum Opcode {plus, minus, none}
    Opcode op;
    Expression left, right;

    public Term(Opcode op, Expression left, Expression right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    public long calculate(){
        long l = left.calculate();
        long r = right.calculate();
        switch (op) {   //check if addition or subtraction
            case plus: return l + r;
            case minus: return l - r;
        }

        return 0;
    }

    public String toJSON() {
        return  "{\n" + "\"left\": " + left.toJSON() + "\"operation\": " + op + "\n" + "\"right\": " + right.toJSON() + "}\n";
        //JSON syntax
    }
}

class Factor extends Expression {
    enum Opcode {mul, div, none}
    Opcode op;
    Expression left, right;
    public Factor(Opcode op, Expression left, Expression right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    public long calculate(){
        long l = left.calculate();
        long r = right.calculate();
        switch (op) {       //check if multiplication or division
            case mul: return l * r;
            case div: return l / r;
        }

        return 0;
    }

    public String toJSON() {
        return  "{\n" + "\"left\": " + left.toJSON() + "\"operation\": " + op + "\n" + "\"right\": " + right.toJSON() + "}\n";
        //JSON syntax
    }
}


public class Program {
    public static void main(String args[]){
        Scanner scanner = new Scanner(System.in);

        String input = scanner.nextLine();
        Parser parser = new Parser(input);

        try {
            Expression expressionTree = parser.parse();
            long result = expressionTree.calculate();

            System.out.println("Result: " + result + "\n");

            System.out.println("JSON:");
            String serialized = expressionTree.toJSON();
            System.out.print(serialized);
        }
        catch (errorIn e) {

        }


    }
}