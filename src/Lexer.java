public class Lexer implements ILexer {

    private Source source;

    public Lexer(Source source) {
        this.source = source;
    }

    public Symbol nextSymbol() {
        /*
        * TODO
        * Po konsultacji z Gawkowskim:
        * 1. Pamietac poczatek tokenu w tekscie/pliku DONE
        * 2. Nie rozbijac tokenow: h1 -> name, a nie alphabets numeric DONE
        * 3. Sprobowac ogarnac to: attr = "value 123" zeby "value 123" -> doubleQuoted
        * */

        // 1. Shift source to next char
        source.nextChar();

        // 2. Ignore whitespaces
        while (Character.isWhitespace(source.getCurrentChar()))
            source.nextChar();

        // 3. Store current position as token beginning point
        TextPosition textPosition = new TextPosition(source.getTextPosition());

        // 4. Lexing

        // <, <!, <!--, </
        if(source.getCurrentChar() == '<')
            return openingTags(textPosition);

        // =
        if(source.getCurrentChar() == '=')
            return new Symbol(Symbol.SymbolType.attrributeAssing, "=", textPosition);

        // >
        if(source.getCurrentChar() == '>')
            return new Symbol(Symbol.SymbolType.finishTag, ">", textPosition);

        // -, --, -->
        if(source.getCurrentChar() == '-')
            return dashOrFinishComment(textPosition);

        // /, />
        if(source.getCurrentChar() == '/')
            return slashOrFinishSelfClosingTag(textPosition);

        // "
        if(source.getCurrentChar() == '\"')
            return new Symbol(Symbol.SymbolType.doubleQuote, "\"", textPosition);

        // '
        if(source.getCurrentChar() == '\'')
            return new Symbol(Symbol.SymbolType.singleQuote, "\'", textPosition);

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
        if(source.getCurrentChar() == '\uFFFF')
            return new Symbol(Symbol.SymbolType.EOF, "EOF", textPosition);

        // Other, e.g. ;, :, *
        return new Symbol(Symbol.SymbolType.other, String.valueOf(source.getCurrentChar()), textPosition);
    }

    private Symbol openingTags(TextPosition textPosition) {
        // 4 possibilities: <, <!, <!--, </
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

        if(source.getCurrentChar() == '/')
            return new Symbol(Symbol.SymbolType.beginEndTag, "</", textPosition);

        source.back();
        return new Symbol(Symbol.SymbolType.beginStartTag, "<", textPosition);
    }

    private Symbol specialSymbol(TextPosition textPosition) {
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

    private Symbol slashOrFinishSelfClosingTag(TextPosition textPosition) {
        source.mark();
        source.nextChar();
        if(source.getCurrentChar() == '>')
            return new Symbol(Symbol.SymbolType.finishSelfClosingTag, "/>", textPosition);
        else {
            source.back();
            return new Symbol(Symbol.SymbolType.other, "/", textPosition);
        }
    }

    private Symbol digits(TextPosition textPosition) {
        StringBuilder value = new StringBuilder("");
        while (Character.isDigit(source.getCurrentChar())) {
            source.mark();
            value.append(String.valueOf(source.getCurrentChar()));
            source.nextChar();
        }
        source.back();
        return new Symbol(Symbol.SymbolType.numeric, value.toString(), textPosition);
    }

    private Symbol dashOrFinishComment(TextPosition textPosition) {
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
    }

    private Symbol lettersAndDigits(TextPosition textPosition) {
        StringBuilder value = new StringBuilder("");
        while (Character.isLetterOrDigit(source.getCurrentChar())) {
            source.mark();
            value.append(String.valueOf(source.getCurrentChar()));
            source.nextChar();
        }
        source.back();
        return new Symbol(Symbol.SymbolType.data, value.toString(), textPosition);
    }
}
