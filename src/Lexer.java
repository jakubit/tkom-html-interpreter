import java.io.IOException;

public class Lexer implements ILexer {

    private Source source;
    private Symbol currentSymbol;
    private char currentChar;

    public Lexer(Source source) {
        this.source = source;
        currentSymbol = Symbol.EOI;
    }

    public Symbol nextSymbol() {
        nextChar();

        // 1. Pomin białe znaki
        while (Character.isWhitespace(currentChar))
            nextChar();

        // 2. Sprawdzaj
        if(currentChar == '<') {
            // 2.1 Otwarcie
            // 4 mozliwosci: <, <!, <!--, </
            nextChar();
            if(currentChar == '!') {
                nextChar();
                if(currentChar == '-') {
                    nextChar();
                    if(currentChar == '-')
                        currentSymbol = Symbol.beginComment;
                    else
                        currentSymbol = Symbol.other;
                } else
                    currentSymbol = Symbol.beginDoctype;
            } else if(currentChar == '/')
                currentSymbol = Symbol.beginEndTag;
            else
                currentSymbol = Symbol.beginStartTag;
        } else if(currentChar == '=')
            currentSymbol = Symbol.attrributeAssing;
        else if(currentChar == '>')
            currentSymbol = Symbol.finishTag;
        else if(currentChar == '-') {
            nextChar();
            if(currentChar == '-') {
                nextChar();
                if(currentChar == '>')
                    currentSymbol = Symbol.finishComment;
            }
        } else if(Character.isAlphabetic(currentChar)) {
            // nazwa atrybutu, wartosc atrybutu, komentarz, doctype, tekst
            if(currentSymbol == Symbol.beginStartTag || currentSymbol == Symbol.beginEndTag) {
                // nazwa tagu
                while(Character.isAlphabetic(currentChar) || Character.isDigit(currentChar)) {
                    source.mark();
                    nextChar();
                }
                source.back();
                currentSymbol = Symbol.tagName;
            } else if(currentSymbol == Symbol.beginComment) {
                // komentarz
                while(currentChar != '-') {
                    source.mark();
                    nextChar();
                }
                source.back();
                currentSymbol = Symbol.data;
            } else if(currentSymbol == Symbol.beginDoctype) {
                // doctype
                while(currentChar != '>') {
                    source.mark();
                    nextChar();
                }
                source.back();
                currentSymbol = Symbol.data;
            } else if(currentSymbol == Symbol.tagName || currentSymbol == Symbol.attributeValue) {
                // nazwa atrybutu
                while(!Character.isWhitespace(currentChar) && currentChar != '=') {
                    source.mark();
                    nextChar();
                }
                source.back();
                currentSymbol = Symbol.attributeName;
            } else if(currentSymbol == Symbol.attrributeAssing) {
                while (!Character.isWhitespace(currentChar)) {
                    source.mark();
                    nextChar();
                }
                source.back();
                currentSymbol = Symbol.attributeValue;
            }
        } else if(Character.isDigit(currentChar)) {
            while (!Character.isWhitespace(currentChar)) {
                source.mark();
                nextChar();
            }
            source.back();
            currentSymbol = Symbol.data;
        }

        return currentSymbol;
    }

    private void nextChar() {
        try {
            currentChar = source.nextChar();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
