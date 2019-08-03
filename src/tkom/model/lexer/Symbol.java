package tkom.model.lexer;

import tkom.model.source.TextPosition;

public class Symbol {

    public enum SymbolType {
        attributiveAssign,       // =
        beginStartTag,          // <
        beginEndTag,            // </
        beginComment,           // <!--
        finishTag,              // >
        finishSelfClosingTag,   // />
        finishComment,          // -->
        data,                   // Alphanumeric names starting with letter
        specialChar,            // &#1234
        beginDoctype,           // <!
        other,                  // -- - / , ; . itp
        singleQuote,            // '
        doubleQuote,            // "
        numeric,                // 123
        error,                  //
        EOF                     //
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

    public String getValue() {
        return value;
    }

    public TextPosition getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return type + "\t" + value + "\t" + position;
    }

}



