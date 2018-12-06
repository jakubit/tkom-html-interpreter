import java.io.IOException;

public class Lexer implements ILexer {

    private Source source;
    private Symbol currentSymbol;
    private char currentChar;

    public Lexer(Source source) {
        this.source = source;
    }

    public Symbol nextSymbol() {
        nextChar();

        // 1. Pomin białe znaki
        while (Character.isWhitespace(currentChar))
            nextChar();

        // 2. Sprawdzaj
        if(currentChar == '<') {
            currentSymbol = processOpeningTags();
        } else if(currentChar == '=') {
            currentSymbol = new Symbol(Symbol.SymbolType.attrributeAssing, "=");
        } else if(currentChar == '>') {
            currentSymbol = new Symbol(Symbol.SymbolType.finishTag, ">");
        } else if(currentChar == '-') {
            if(currentChar == '-') {
                source.mark();
                nextChar();
                if(currentChar == '>')
                    return new Symbol(Symbol.SymbolType.finishComment, "-->");
                else 

            }
        } else
            return new Symbol(Symbol.SymbolType.alphabetic, String.valueOf(currentChar));

        return currentSymbol;
    }

    private Symbol processOpeningTags() {
        if(currentChar == '<') {
            nextChar();
            if(currentChar == '!') {
                nextChar();
                if(currentChar == '-') {
                    nextChar();
                    if(currentChar == '-')
                        return new Symbol(Symbol.SymbolType.beginComment, "<!--");
                    else
                        return new Symbol(Symbol.SymbolType.other, "<!-?");
                }
                return new Symbol(Symbol.SymbolType.beginDoctype, "<!");
            } else if(currentChar == '/') {
                return new Symbol(Symbol.SymbolType.beginEndTag, "</");
            } else {
                return new Symbol(Symbol.SymbolType.beginStartTag, "<");
            }
        }

        return new Symbol(Symbol.SymbolType.other, "other");
    }


    private void nextChar() {
        try {
            currentChar = source.nextChar();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
