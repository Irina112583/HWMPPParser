import java.util.Stack;

public class Parser {
    private String input;
    private int currentChar = 0;
    private Stack par = new Stack();
    public Parser(String input) {
        this.input = input.replaceAll("\\s+","");   //Delete all spaces}
    }
    public Expression parse() throws errorIn {  //We have only one public method, cause we parse hole expression (not only logical etc.)
        Expression temp = parseLogical();
        if (par.empty()) return temp;           //if our stack is not empty, then we missed right parenthesis
        else {
            System.out.println("Right parenthesis was missed");
            throw new errorIn(errorKind.none);
        }
    }

    private Expression parseLogical() throws errorIn{   //Here, we parse logical expressions
        Expression result = parseRelation();
        while (true) {
            Logical.Opcode op = parseLogOperator(); //call parseLogicalOperator, which is trying to find logical operators
            if (op != Logical.Opcode.none) {
                Expression right = parseRelation();  //if found logical operator, then deal with right expression.
                result = new Logical(op, result, right);
            } else break;
        }
        return result;
    }

    private Expression parseRelation() throws errorIn {   //The same as with logical
        Expression result = parseTerm();
        Relation.Opcode op = parseRelationOperator();
        if (op != Relation.Opcode.none) {
            Expression right = parseTerm();
            result = new Relation(op, result, right);
        }

        op = parseRelationOperator();   // we can have only one relation operation per expression (and per parenthesis)
        if (op != Relation.Opcode.none) {
            System.out.println("Too many relational operators.");
            throw new errorIn(errorKind.relation);
        }

        return result;


    }

    private Expression parseTerm() throws errorIn {    //the same as with logical
        Expression result = parseFactor();
        while (true) {
            Term.Opcode op = parseTermOperator();
            if (op != Term.Opcode.none) {
                Expression right = parseFactor();

                if (result == null) result = new Primary(0);    //If we haven't got left value (e.g. -2). Handle + and - before number.
                if (right == null) {
                    System.out.println("Expected number. There is no value after the " + op + " operator");      //Checks if we have a number after operator
                    throw new errorIn(errorKind.term);
                }
                result = new Term(op, result, right);
            } else break;
        }
        return result;
    }

    private Expression parseFactor() throws errorIn{  //the same as with logical
        int flag = 0;
        Expression result = parsePrimary();
        while (true) {
            Factor.Opcode op = parseFactorOperator();
            if (op != Factor.Opcode.none) {
                Expression right = parsePrimary();
                result = new Factor(op, result, right);
            } else {
                if (currentChar < input.length() && input.charAt(currentChar) == '(') { //If we missed operator, throw exception
                    System.out.println("Expected an operator. Nothing was received");
                    throw new errorIn(errorKind.none);
                } else break;
            }
        }
        return result;
    }

    private Logical.Opcode parseLogOperator() {    //try to find logical operator.
        if (input.substring(currentChar).startsWith("and")) {
            currentChar+=3;
            return Logical.Opcode.and;
        }
        else if (input.substring(currentChar).startsWith("or")) {
            currentChar+=2;
            return Logical.Opcode.or;
        }
        else if (input.substring(currentChar).startsWith("xor")) {
            currentChar+=3;
            return Logical.Opcode.xor;
        }
        return Logical.Opcode.none;
    }

    private Relation.Opcode parseRelationOperator() {  //try to find relation operator.
        if (input.substring(currentChar).startsWith("<=")) {
            currentChar+=2;
            return Relation.Opcode.less_eq;
        }
        else if (input.substring(currentChar).startsWith("<")) {
            currentChar+=1;
            return Relation.Opcode.less;
        }
        else if (input.substring(currentChar).startsWith(">=")) {
            currentChar+=2;
            return Relation.Opcode.greater_eq;
        }
        else if (input.substring(currentChar).startsWith(">")) {
            currentChar+=1;
            return Relation.Opcode.greater;
        }
        else if (input.substring(currentChar).startsWith("==")) {
            currentChar+=2;
            return Relation.Opcode.equal;
        }
        else if (input.substring(currentChar).startsWith("!=")) {
            currentChar+=2;
            return Relation.Opcode.not_eq;
        }
        return Relation.Opcode.none;
    }

    private Term.Opcode parseTermOperator() {   //try to find term operator.
        if (input.substring(currentChar).startsWith("+")) {
            currentChar++;
            return Term.Opcode.plus;
        }
        else if (input.substring(currentChar).startsWith("-")) {
            currentChar++;
            return Term.Opcode.minus;
        }
        return Term.Opcode.none;
    }

    private Factor.Opcode parseFactorOperator() {    //try to find factor operator.
        if (input.substring(currentChar).startsWith("*")) {
            currentChar++;
            return Factor.Opcode.mul;
        }
        else if (input.substring(currentChar).startsWith("/")) {
            currentChar++;
            return Factor.Opcode.div;
        }
        return Factor.Opcode.none;
    }

    private Expression parsePrimary() throws errorIn {     // Parse primary. Here, we find new expression (in brackets) or integer value
        Expression result = null;
        char curr;
        if (currentChar >= input.length()) {        //Checks if we have a number after operator
            System.out.println("There is no number after operator.");
            throw new errorIn(errorKind.primary);
        } else { curr = input.charAt(currentChar);}

        if (Character.isDigit(curr)) {
            result = parseInt();    //if we have digit, then cal ParseInt
        } else if (curr == '(') {   //If we have new expression, call parseLogical and do all steps again.
            currentChar++;
            par.push(1);    //push element in a stack, to show that left parenthesis was received
            result = parseLogical();

            /*if (currentChar >= input.length()) {    //Check if we have right parenthesis
                System.out.println("Right parenthesis was missed");
                throw new errorIn(errorKind.none);
            } else currentChar++; */

        } else if (curr != '-' && curr != '+'){     //Checks if we have a number after operator. "+" and "-" are exceptions, cause we can have negative numbers at the beginning of expression
            System.out.println("Expected number. \"" + curr + "\" is not a number");
            throw new errorIn(errorKind.primary);
        }

        if (currentChar < input.length() && input.charAt(currentChar) == ')') { //Pop from stack if it's not empty. Otherwise, we have a syntax error.
            if (par.empty()) {
                System.out.println("Exceed parenthesis was received");
                throw new errorIn(errorKind.none);
            } else {
                par.pop();
                currentChar++;
            }
        }
        return result;
    }

    private Expression parseInt() throws errorIn{
        char curr = input.charAt(currentChar);
        int val = 0;

        while (Character.isDigit(curr) && currentChar < input.length()) {  //if not the end of the length and current char is digit
            val = val * 10 + Character.getNumericValue(curr);   //multiply by 10 to receive 2-digit and 3-digit numbers
            currentChar++;
            if (currentChar < input.length())
                curr = input.charAt(currentChar);
        }

        Expression result = new Primary(val);
        return result;

    }
}
