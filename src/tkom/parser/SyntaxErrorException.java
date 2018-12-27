package tkom.parser;

import tkom.lexer.Symbol;
import tkom.source.TextPosition;

import java.util.List;

public class SyntaxErrorException extends Exception {
    private List<String> expected;
    private Symbol actual;

    public SyntaxErrorException(List<String> expected, Symbol actual) {
        this.expected = expected;
        this.actual = actual;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("");
        string.append("SYNTAX ERROR! ");
        string.append(actual.getPosition());
        string.append(" Expected: ");
        for (String s : expected) {
            string.append(s);
            string.append(" or ");
        }
        int index = string.lastIndexOf(" or ");
        string.delete(index, index + 4 );
        string.append(" but actual is: ");
        string.append(actual.getValue());

        return string.toString();
    }
}
