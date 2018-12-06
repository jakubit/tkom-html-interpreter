import java.io.IOException;

public class Lexer implements ILexer {

    private Source source;


    public Lexer(Source source) {
        this.source = source;

    }

    public Symbol nextSymbol() {
        /*
        * TODO
        * Refactor:
        * despaghettize
        * improve cooperation with Source
        * try to get rid of get/unget char
        * breakup to smaller functions
        * */

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
            source.mark();
            source.nextChar();
            if(source.getCurrentChar() == '-') {
                source.mark();
                source.nextChar();
                if(source.getCurrentChar() == '>')
                    return new Symbol(Symbol.SymbolType.finishComment, "-->");
                else {
                    source.back();
                    return new Symbol(Symbol.SymbolType.data, "--");
                }
            } else {
                source.back();
                return new Symbol(Symbol.SymbolType.data, "-");
            }
        } else if(source.getCurrentChar() == '\"') {
            return new Symbol(Symbol.SymbolType.doubleQuote, "\"");
        } else if(source.getCurrentChar() == '\'') {
            return new Symbol(Symbol.SymbolType.singleQuote, "\'");
        } else if(Character.isLetter(source.getCurrentChar())) {
            StringBuilder value = new StringBuilder("");
            while (Character.isLetter(source.getCurrentChar())) {
                source.mark();
                value.append(String.valueOf(source.getCurrentChar()));
                source.nextChar();
            }
            source.back();
            return new Symbol(Symbol.SymbolType.alphabetic, value.toString());
        } else if(Character.isDigit(source.getCurrentChar())) {
            StringBuilder value = new StringBuilder("");
            while (Character.isDigit(source.getCurrentChar())) {
                source.mark();
                value.append(String.valueOf(source.getCurrentChar()));
                source.nextChar();
            }
            source.back();
            return new Symbol(Symbol.SymbolType.numeric, value.toString());
        } else if(source.getCurrentChar() == '\uFFFF') {
            return new Symbol(Symbol.SymbolType.EOF, "EOF");
        }

        return new Symbol(Symbol.SymbolType.other, String.valueOf(source.getCurrentChar()));
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
