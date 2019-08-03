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

    public void setSource(Source source) {
        this.source = source;
        source.nextChar();
    }

    public Symbol nextSymbol() {

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
            return new Symbol(Symbol.SymbolType.attributiveAssign, "=", textPosition);
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
        source.nextChar();
        if(source.getCurrentChar() == '!') {
            source.nextChar();
            if(source.getCurrentChar() == '-') {
                source.nextChar();
                if(source.getCurrentChar() == '-') {
                    Symbol symbol = new Symbol(Symbol.SymbolType.beginComment, "<!--", textPosition);
                    source.nextChar();
                    return symbol;
                } else {
                    Symbol symbol = new Symbol(Symbol.SymbolType.error, "<!-", textPosition);
                    source.nextChar();
                    return symbol;
                }
            } else {
                Symbol symbol = new Symbol(Symbol.SymbolType.beginDoctype, "<!", textPosition);
                return symbol;
            }
        }

        if(source.getCurrentChar() == '/') {
            Symbol symbol = new Symbol(Symbol.SymbolType.beginEndTag, "</", textPosition);
            source.nextChar();
            return symbol;
        }

        return new Symbol(Symbol.SymbolType.beginStartTag, "<", textPosition);
    }

    private Symbol specialSymbol(TextPosition textPosition) {
        StringBuilder value = new StringBuilder("&");
        source.nextChar();

        int length = 0;
        while(length < MAX_LENGTH && Character.isLetterOrDigit(source.getCurrentChar()) || source.getCurrentChar() == '#') {
            value.append(String.valueOf(source.getCurrentChar()));
            source.nextChar();
            length++;
        }
        Symbol symbol = new Symbol(Symbol.SymbolType.specialChar, value.toString(), textPosition);
        return symbol;
    }

    private Symbol slashOrFinishSelfClosingTag(TextPosition textPosition) {
        source.nextChar();
        if(source.getCurrentChar() == '>') {
            Symbol symbol = new Symbol(Symbol.SymbolType.finishSelfClosingTag, "/>", textPosition);
            source.nextChar();
            return symbol;
        } else {
            Symbol symbol = new Symbol(Symbol.SymbolType.other, "/", textPosition);
            return symbol;
        }
    }

    private Symbol digits(TextPosition textPosition) {
        StringBuilder value = new StringBuilder("");
        int length = 0;
        while (length < MAX_LENGTH && Character.isDigit(source.getCurrentChar())) {
            value.append(String.valueOf(source.getCurrentChar()));
            source.nextChar();
            length++;
        }
        return new Symbol(Symbol.SymbolType.numeric, value.toString(), textPosition);
    }

    private Symbol dashOrFinishComment(TextPosition textPosition) {
        source.nextChar();
        if(source.getCurrentChar() == '-') {
            source.nextChar();
            if(source.getCurrentChar() == '>') {
                Symbol symbol = new Symbol(Symbol.SymbolType.finishComment, "-->", textPosition);
                source.nextChar();
                return symbol;
            } else {
                Symbol symbol = new Symbol(Symbol.SymbolType.data, "--", textPosition);
                return symbol;
            }
        } else {
            Symbol symbol = new Symbol(Symbol.SymbolType.data, "-", textPosition);
            return symbol;
        }
    }


    private Symbol lettersAndDigits(TextPosition textPosition) {


        StringBuilder value = new StringBuilder("");
        int length = 0;
        while (length < MAX_LENGTH && !Character.isWhitespace(source.getCurrentChar()) && characterAllowedInName(source.getCurrentChar())) {
            value.append(String.valueOf(source.getCurrentChar()));
            source.nextChar();
            length++;
        }
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
