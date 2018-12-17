package tkom.lexer;

import tkom.source.TextPosition;

public class Symbol {

    public enum SymbolType {
        attrributeAssing,
        beginStartTag,
        beginEndTag,
        beginComment,
        finishTag,
        finishSelfClosingTag,
        finishComment,
        data,
        specialChar,
        beginDoctype,
        other,
        singleQuote,
        doubleQuote,
        numeric,
        error,
        EOF
    }

    private SymbolType type;
    private String value;
    private TextPosition position;

    public Symbol(SymbolType type, String value, TextPosition position) {
        this.type = type;
        this.value = value;
        this.position = position;
    }

    public SymbolType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type + "\t" + value + "\t" + position;
    }

}



