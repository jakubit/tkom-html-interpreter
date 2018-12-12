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

        /*
        * TODO
        * Po konsultacji z Gawkowskim:
        * 1. Pamietac poczatek tokenu w tekscie/pliku DONE
        * 2. Nie rozbijac tokenow: h1 -> name, a nie alphabets numeric
        * 3. Sprobowac ogarnac to: attr = "value 123" zeby "value 123" -> doubleQuoted
        * */

        // 1. Przesun znak w Source
        source.nextChar();


        // 2. Pomin białe znaki
        while (Character.isWhitespace(source.getCurrentChar()))
            source.nextChar();

        // 3. Zapisz sobie pozycje, bo tu zaczyna sie token
        TextPosition textPosition = new TextPosition(source.getTextPosition());

        // 4. Sprawdzaj
        if(source.getCurrentChar() == '<') {

            // ZNACZNIKI OTWIERJACE: <, <!, <!--, </
            return openingTags();
        }
        else if(source.getCurrentChar() == '=')
            return new Symbol(Symbol.SymbolType.attrributeAssing, "=", textPosition);
        else if(source.getCurrentChar() == '>')
            return new Symbol(Symbol.SymbolType.finishTag, ">", textPosition);
        else if(source.getCurrentChar() == '-') {
            source.mark();
            source.nextChar();
            if(source.getCurrentChar() == '-') {
                source.mark();
                source.nextChar();
                if(source.getCurrentChar() == '>')
                    return new Symbol(Symbol.SymbolType.finishComment, "-->", textPosition);
                else {
                    source.back();
                    return new Symbol(Symbol.SymbolType.other, "--", textPosition);
                }
            } else {
                source.back();
                return new Symbol(Symbol.SymbolType.other, "-", textPosition);
            }
        } else if(source.getCurrentChar() == '/') {
            source.mark();
            source.nextChar();
            if(source.getCurrentChar() == '>')
                return new Symbol(Symbol.SymbolType.finishSelfClosingTag, "/>", textPosition);
            else {
                source.back();
                return new Symbol(Symbol.SymbolType.other, "/", textPosition);
            }
        } else if(source.getCurrentChar() == '\"') {
            return new Symbol(Symbol.SymbolType.doubleQuote, "\"", textPosition);
        } else if(source.getCurrentChar() == '\'') {
            return new Symbol(Symbol.SymbolType.singleQuote, "\'", textPosition);
        } else if(Character.isLetter(source.getCurrentChar())) {
            StringBuilder value = new StringBuilder("");
            while (Character.isLetterOrDigit(source.getCurrentChar())) {
                source.mark();
                value.append(String.valueOf(source.getCurrentChar()));
                source.nextChar();
            }
            source.back();
            return new Symbol(Symbol.SymbolType.name, value.toString(), textPosition);
        } else if(Character.isDigit(source.getCurrentChar())) {
            StringBuilder value = new StringBuilder("");
            while (Character.isDigit(source.getCurrentChar())) {
                source.mark();
                value.append(String.valueOf(source.getCurrentChar()));
                source.nextChar();
            }
            source.back();
            return new Symbol(Symbol.SymbolType.numeric, value.toString(), textPosition);
        } else if(source.getCurrentChar() == '&') {
            StringBuilder value = new StringBuilder("&");
            source.mark();
            source.nextChar();
            while(Character.isLetterOrDigit(source.getCurrentChar()) || source.getCurrentChar() == '#') {
                source.mark();
                value.append(String.valueOf(source.getCurrentChar()));
                source.nextChar();
            }
            source.back();
            return new Symbol(Symbol.SymbolType.specialChar, value.toString(), textPosition);
        }
        else if(source.getCurrentChar() == '\uFFFF') {
            return new Symbol(Symbol.SymbolType.EOF, "EOF", textPosition);
        }

        return new Symbol(Symbol.SymbolType.other, String.valueOf(source.getCurrentChar()), textPosition);
    }

    private Symbol openingTags() {
        // 4 mozliwosci: <, <!, <!--, </
        TextPosition textPosition = new TextPosition(source.getTextPosition());

        source.mark();
        source.nextChar();
        if(source.getCurrentChar() == '!') {
            source.mark();
            source.nextChar();
            if(source.getCurrentChar() == '-') {
                source.mark();
                source.nextChar();
                if(source.getCurrentChar() == '-')
                    return new Symbol(Symbol.SymbolType.beginComment, "<!--", textPosition);
                else {
                    source.back();
                    return new Symbol(Symbol.SymbolType.other, "OtherSymbol", textPosition);
                }
            } else {
                source.back();
                return new Symbol(Symbol.SymbolType.beginDoctype, "<!", textPosition);
            }
        }
        else if(source.getCurrentChar() == '/')
            return new Symbol(Symbol.SymbolType.beginEndTag, "</", textPosition);
        else {
            source.back();
            return new Symbol(Symbol.SymbolType.beginStartTag, "<", textPosition);
        }
    }


}
