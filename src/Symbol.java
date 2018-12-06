
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
        finishComment,
        data,
        beginDoctype,
        other,
        singleQuote,
        doubleQuote,
        alphabetic,
        numeric,
        EOF;
    }

    private SymbolType type;
    private String value;

    public Symbol(SymbolType type, String value) {
        this.type = type;
        this.value = value;
    }

    public SymbolType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return type + "\t" + value;
    }

}



