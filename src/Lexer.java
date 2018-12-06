import java.io.EOFException;
import java.io.IOException;

public class Lexer implements ILexer {

    private Source source;
    private String currentChar;


    public Lexer(Source source) {
        this.source = source;
    }

    public Symbol nextSymbol() {
        // 1. Przesun znak w Source
        char currentChar = source.nextChar();


        // 2. Pomin białe znaki
        while (Character.isWhitespace(currentChar))
            currentChar = source.nextChar();

        // 3. Sprawdzaj




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
