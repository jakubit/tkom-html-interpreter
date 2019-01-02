package tkom.parser;

import tkom.source.TextPosition;

public class UnexpectedEOFException extends Exception {
    private String expected;
    private TextPosition position;

    public UnexpectedEOFException(String expected, TextPosition position) {
        this.expected = expected;
        this.position = position;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append("UNEXPECTED END OF FILE! ");
        string.append(position);
        if (!expected.equals("")) {
            string.append(" Expected: ");
            string.append(expected);
        }
        return string.toString();
    }
}
