package tkom.model.lexer;

import tkom.model.source.TextPosition;
import tkom.model.source.Source;

public class Lexer implements ILexer {

    private Source source;
    private static final int MAX_LENGTH = 30;

    public Lexer(Source source) {
        this.source = source;

        // Shift source to the first char
        source.nextChar();
    }

    public Symbol nextSymbol() {
        /*
        * TODO
        * 1. Ogarnac max value zeby rzucalo jakims wyjatkiem czy cos
        * 2. Ogarnac token error - chyba trzeba zamienic to na rzucanie wyjatkiem i lapanie w parserze
        * 3. Ogarnac lexer - nie moze rozbijac nazw... Mozna ale potem w parserze trzeba je laczyc
        * */

        /*
         * TODO
         * 3. AUTOMAT DLA --> W KOMENTARZU
         * 4. komentarze
         */

        // 1. Shift source to next char
        //source.nextChar();

        // 2. Ignore whitespaces
        while (Character.isWhitespace(source.getCurrentChar()))
            source.nextChar();

        // 3. Store current position as token beginning point
        TextPosition textPosition = new TextPosition(source.getTextPosition());

        // 4. Lexing

        // <, <!, <!--, </
        if(source.getCurrentChar() == '<') {
            //source.nextChar();
            return openingTags(textPosition);
        }

        // =
        if(source.getCurrentChar() == '=') {
            source.nextChar();
            return new Symbol(Symbol.SymbolType.attrributeAssing, "=", textPosition);
        }

        // >
        if(source.getCurrentChar() == '>') {
            source.nextChar();
            return new Symbol(Symbol.SymbolType.finishTag, ">", textPosition);
        }

        // -, --, -->
        if(source.getCurrentChar() == '-') {
            //source.nextChar();
            return dashOrFinishComment(textPosition);
        }


        // /, />
        if(source.getCurrentChar() == '/')
            return slashOrFinishSelfClosingTag(textPosition);

        // "
        if(source.getCurrentChar() == '\"') {
            source.nextChar();
            return new Symbol(Symbol.SymbolType.doubleQuote, "\"", textPosition);
        }


        // '
        if(source.getCurrentChar() == '\'') {
            source.nextChar();
            return new Symbol(Symbol.SymbolType.singleQuote, "\'", textPosition);
        }


        // Alphanumeric names starting with letter
        if(Character.isLetter(source.getCurrentChar()))
            return lettersAndDigits(textPosition);

        // Numbers
        if(Character.isDigit(source.getCurrentChar()))
            return digits(textPosition);

        // Special Characters
        if(source.getCurrentChar() == '&')
            return specialSymbol(textPosition);

        // EOF
        if(source.getCurrentChar() == '\uFFFF') {
            source.nextChar();
            return new Symbol(Symbol.SymbolType.EOF, "EOF", textPosition);
        }


        // Other, e.g. ;, :, *
        Symbol sym = new Symbol(Symbol.SymbolType.data, String.valueOf(source.getCurrentChar()), textPosition);
        source.nextChar();
        return sym;
    }

    private Symbol openingTags(TextPosition textPosition) {
        // 4 possibilities: <, <!, <!--, </
        //source.mark();
        source.nextChar();
        if(source.getCurrentChar() == '!') {
            //source.mark();
            source.nextChar();
            if(source.getCurrentChar() == '-') {
                //source.mark();
                source.nextChar();
                if(source.getCurrentChar() == '-') {
                    Symbol symbol = new Symbol(Symbol.SymbolType.beginComment, "<!--", textPosition);
                    source.nextChar();
                    return symbol;
                } else {
                    //source.back();
                    Symbol symbol = new Symbol(Symbol.SymbolType.error, "<!-", textPosition);
                    source.nextChar();
                    return symbol;
                }
            } else {
                //source.back();
                Symbol symbol = new Symbol(Symbol.SymbolType.beginDoctype, "<!", textPosition);
                //source.nextChar();
                return symbol;
            }
        }

        if(source.getCurrentChar() == '/') {
            Symbol symbol = new Symbol(Symbol.SymbolType.beginEndTag, "</", textPosition);
            source.nextChar();
            return symbol;
        }

        //source.back();
        return new Symbol(Symbol.SymbolType.beginStartTag, "<", textPosition);
    }

    private Symbol specialSymbol(TextPosition textPosition) {
        // TODO: PODMIENIAC OD RAZU NA WARTOSCI ASCII
        /*
        jesli jest &#[numer] to value = numer
        jesli jest &name to value = map.get(name)
         */

        StringBuilder value = new StringBuilder("&");
        source.nextChar();

        /*
        int asciiCode;
        if(source.getCurrentChar() == '#') {
            // &#number
            while(Character.isDigit(source.getCurrentChar())) {
                value.append(String.valueOf(source.getCurrentChar()));
            }
            asciiCode = Integer.valueOf(value.toString());
        } else if(Character.isLetter(source.getCurrentChar())) {
            // &name
            while(Character.isLetterOrDigit(source.getCurrentChar())) {
                value.append(String.valueOf(source.getCurrentChar()));
            }

        }*/

        int length = 0;
        while(length < MAX_LENGTH && Character.isLetterOrDigit(source.getCurrentChar()) || source.getCurrentChar() == '#') {
            //source.mark();
            value.append(String.valueOf(source.getCurrentChar()));
            source.nextChar();
            length++;
        }
        //source.back();
        Symbol symbol = new Symbol(Symbol.SymbolType.specialChar, value.toString(), textPosition);
        //source.nextChar();
        return symbol;
    }

    private Symbol slashOrFinishSelfClosingTag(TextPosition textPosition) {
        //source.mark();
        source.nextChar();
        if(source.getCurrentChar() == '>') {
            Symbol symbol = new Symbol(Symbol.SymbolType.finishSelfClosingTag, "/>", textPosition);
            source.nextChar();
            return symbol;
        } else {
            //source.back();
            Symbol symbol = new Symbol(Symbol.SymbolType.other, "/", textPosition);
            //source.nextChar();
            return symbol;
        }
    }

    private Symbol digits(TextPosition textPosition) {
        // TODO: ROBIC OD RAZU INTIGER
        StringBuilder value = new StringBuilder("");
        int length = 0;
        while (length < MAX_LENGTH && Character.isDigit(source.getCurrentChar())) {
            //source.mark();
            value.append(String.valueOf(source.getCurrentChar()));
            source.nextChar();
            length++;
        }
        //source.back();
        return new Symbol(Symbol.SymbolType.numeric, value.toString(), textPosition);
    }

    private Symbol dashOrFinishComment(TextPosition textPosition) {
        // TODO: AUTOMAT DO TEGO
        //source.mark();
        source.nextChar();
        if(source.getCurrentChar() == '-') {
            //source.mark();
            source.nextChar();
            if(source.getCurrentChar() == '>') {
                Symbol symbol = new Symbol(Symbol.SymbolType.finishComment, "-->", textPosition);
                source.nextChar();
                return symbol;
            } else {
                //source.back();
                Symbol symbol = new Symbol(Symbol.SymbolType.data, "--", textPosition);
                //source.nextChar();
                return symbol;
            }
        } else {
            //source.back();
            Symbol symbol = new Symbol(Symbol.SymbolType.data, "-", textPosition);
            //source.nextChar();
            return symbol;
        }
    }


    private Symbol lettersAndDigits(TextPosition textPosition) {


        StringBuilder value = new StringBuilder("");
        int length = 0;
        while (length < MAX_LENGTH && !Character.isWhitespace(source.getCurrentChar()) && characterAllowedInName(source.getCurrentChar())) {
            //source.mark();


                value.append(String.valueOf(source.getCurrentChar()));
                source.nextChar();
                length++;

        }
        //source.back();
        return new Symbol(Symbol.SymbolType.data, value.toString(), textPosition);
    }

    private boolean characterAllowedInName(char c) {
        switch (c) {
            case '=':
            case '/':
            case '>':
            case '\'':
            case '\"':
            case '<':
            case '-':
            case '&': return false;
        }

        return true;
    }
}
