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
        return actual.getPosition() + " SYNTAX ERROR! Expected: '" + expected + "' but actual is: '" + actual.getValue() + "'";
    }
}
