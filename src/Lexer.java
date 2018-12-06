import java.io.IOException;

public class Lexer implements ILexer {

    private Source source;


    public Lexer(Source source) {
        this.source = source;

    }

    public Symbol nextSymbol() {
        // 1. Przesun znak w Source
        source.nextChar();


        // 2. Pomin białe znaki
        while (Character.isWhitespace(source.getCurrentChar()))
            source.nextChar();

        // 2. Sprawdzaj
        if(source.getCurrentChar() == '<') {

            // ZNACZNIKI OTWIERJACE: <, <!, <!--, </
            return openingTags();
        }
        else if(source.getCurrentChar() == '=')
            return new Symbol(Symbol.SymbolType.attrributeAssing, "=");
        else if(source.getCurrentChar() == '>')
            return new Symbol(Symbol.SymbolType.finishTag, ">");
        else if(source.getCurrentChar() == '-') {
            source.nextChar();
            if(source.getCurrentChar() == '-') {
                source.mark();
                source.nextChar();
                if(source.getCurrentChar() == '>')
                    return new Symbol(Symbol.SymbolType.finishComment, "-->");
                else {
                    source.back();
                    return new Symbol(Symbol.SymbolType.alphabetic, "--");
                }
            }
        } else if(source.getCurrentChar() == '\"') {
            return new Symbol(Symbol.SymbolType.doubleQuote, "\"");
        } else if(source.getCurrentChar() == '\'') {
            return new Symbol(Symbol.SymbolType.singleQuote, "\'");
        } else if(Character.isLetter(source.getCurrentChar())) {
            return new Symbol(Symbol.SymbolType.alphabetic, String.valueOf(source.getCurrentChar()));
        }

        return new Symbol(Symbol.SymbolType.EOI, "EOI");
    }

    private Symbol openingTags() {
        // 4 mozliwosci: <, <!, <!--, </

        source.mark();
        source.nextChar();
        if(source.getCurrentChar() == '!') {
            source.mark();
            source.nextChar();
            if(source.getCurrentChar() == '-') {
                source.mark();
                source.nextChar();
                if(source.getCurrentChar() == '-')
                    return new Symbol(Symbol.SymbolType.beginComment, "<!--");
                else {
                    source.back();
                    return new Symbol(Symbol.SymbolType.other, "OtherSymbol");
                }
            } else {
                source.back();
                return new Symbol(Symbol.SymbolType.beginDoctype, "<!");
            }
        }
        else if(source.getCurrentChar() == '/')
            return new Symbol(Symbol.SymbolType.beginEndTag, "</");
        else {
            source.back();
            return new Symbol(Symbol.SymbolType.beginStartTag, "<");
        }
    }


}
