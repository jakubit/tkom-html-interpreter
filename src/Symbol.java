
public class Symbol {

    public enum SymbolType {
        attrributeAssing,
        attributeName,
        attributeValue,
        beginStartTag,
        beginEndTag,
        beginComment,
        tagName,
        finishTag,
        finishSelfClosingTag,
        finishComment,
        data,
        specialChar,
        beginDoctype,
        other,
        singleQuote,
        doubleQuote,
        name,
        numeric,
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



