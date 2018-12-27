package tkom.parser;

import tkom.lexer.Lexer;
import tkom.lexer.Symbol;

import java.util.Stack;
import java.util.stream.Stream;

public class Parser {
    private Symbol currentSymbol;
    private Lexer lexer;
    private Stack<HtmlElement> htmlElements;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        htmlElements = new Stack<>();
    }

    public void printStack() {
        htmlElements.stream().forEach(System.out::println);
    }

    public void parse() {
        nextSymbol();

        while (currentSymbol.getType() != Symbol.SymbolType.EOF) {
            // parse

            if(currentSymbol.getType() == Symbol.SymbolType.beginStartTag) {
                // <
                parseOpeningTag();
            } else if (currentSymbol.getType() == Symbol.SymbolType.beginEndTag) {
                // </
                parseClosingTag();
            } else if (currentSymbol.getType() == Symbol.SymbolType.beginComment) {
                // <!--
                parseComment();
            } else  if (currentSymbol.getType() == Symbol.SymbolType.beginDoctype) {
                // <!DOCTYPE
                parseDoctype();
            } else {
                parseText();
            }
        }
    }

    private void parseText() {
        // caly tekst pomiedzy znacznikami
        StringBuilder value = new StringBuilder("");
        while (textTypeSymbol(currentSymbol)) {
            value.append(currentSymbol.getValue());
            value.append(" ");
            nextSymbol();
        }

        pushStack(new HtmlElement(HtmlElement.ElementType.text, value.toString()));
    }

    private boolean textTypeSymbol(Symbol symbol) {
        if (symbol.getType() == Symbol.SymbolType.beginStartTag
            || symbol.getType() == Symbol.SymbolType.beginEndTag
            || symbol.getType() == Symbol.SymbolType.beginComment
            || symbol.getType() == Symbol.SymbolType.beginDoctype)
            return false;

        return true;
    }

    private void parseDoctype() {

        StringBuilder value = new StringBuilder("");
        while (currentSymbol.getType() != Symbol.SymbolType.finishTag) {
            value.append(currentSymbol.getValue());
            nextSymbol();
        }

        pushStack(new HtmlElement(HtmlElement.ElementType.doctype, value.toString()));
    }

    private void parseComment() {
        // todo tekst komentarza

        nextSymbol();
        StringBuilder value = new StringBuilder("");
        while (currentSymbol.getType() != Symbol.SymbolType.finishComment) {
            value.append(currentSymbol.getValue());
            value.append(" ");
            nextSymbol();
        }
        pushStack(new HtmlElement(HtmlElement.ElementType.comment, value.toString()));
        nextSymbol();
    }


    private void parseClosingTag() {
        HtmlTag tag = new HtmlTag();
        nextSymbol();
        if (currentSymbol.getType() == Symbol.SymbolType.data) {
            // tag name
            tag.setName(currentSymbol.getValue());
        } else {
            // todo
            System.out.println("ERROR: In parseClosingTag (1)");
            return;
        }

        nextSymbol();
        if(currentSymbol.getType() == Symbol.SymbolType.finishTag) {
            tag.setType(HtmlTag.TagType.closing);
        } else {
            // todo
            System.out.println("ERROR: In parseClosingTag (2)");
            return;
        }

        // Add tag to html elements stack
        pushStack(tag);
        nextSymbol();
    }

    private void parseOpeningTag() {
        // teraz ma tylko <

        nextSymbol();
        HtmlTag tag = new HtmlTag();

        if (currentSymbol.getType() == Symbol.SymbolType.data) {
            // znaleziono nazwe tagu, teraz mam <tagName
            tag.setName(currentSymbol.getValue());
        } else {
            // todo
            System.out.println("ERROR: In parseOpeningTag (1)");
            return;
        }

        nextSymbol();

        // jesli jest data, to na pewno jest to atrybut, przeparsuj je
        if (currentSymbol.getType() == Symbol.SymbolType.data)
            parseAttributes(tag);

        // koniec parsowania atrybutow, teraz szukamy zamkniecia
        if (currentSymbol.getType() == Symbol.SymbolType.finishTag)
            tag.setType(HtmlTag.TagType.opening);
        else if (currentSymbol.getType() == Symbol.SymbolType.finishSelfClosingTag)
            tag.setType(HtmlTag.TagType.selfClosing);
        else {
            // todo
            System.out.println("ERROR: parseOpeningtag (2)");
            return;
        }

        // Add tag to html elements stack
        pushStack(tag);
        nextSymbol();
    }

    private void parseAttributes(HtmlTag tag) {
        while (currentSymbol.getType() == Symbol.SymbolType.data) {
            parseAttribute(tag);
            nextSymbol();
        }
    }

    private void parseAttribute(HtmlTag tag) {
        // parse one attribute

        // name, mam na pewno <tagName attrName
        String attrName = currentSymbol.getValue();

        nextSymbol();
        if (currentSymbol.getType() == Symbol.SymbolType.attrributeAssing) {
            // <tagName attrName=
            nextSymbol();
            if (currentSymbol.getType() == Symbol.SymbolType.data) {
                // unquoted attr value
                tag.addAtribute(attrName, currentSymbol.getValue());
            } else if (currentSymbol.getType() == Symbol.SymbolType.singleQuote) {
                // single quoted attr value
                nextSymbol();
                while (currentSymbol.getType() != Symbol.SymbolType.singleQuote) {
                    tag.addAtribute(attrName, currentSymbol.getValue());
                    nextSymbol();
                }
            } else if (currentSymbol.getType() == Symbol.SymbolType.doubleQuote) {
                // double quoted attribute value
                nextSymbol();
                while (currentSymbol.getType() != Symbol.SymbolType.doubleQuote) {
                    tag.addAtribute(attrName, currentSymbol.getValue());
                    nextSymbol();
                }
            } else {
                // todo
                System.out.println("ERROR: In parse attribute!");
            }
        } else {
            // no value attribute
            tag.addAtribute(attrName, "");
        }
    }

    private void pushStack(HtmlElement element) {
        System.out.println(element);
        htmlElements.push(element);
    }


    private void nextSymbol() {
        currentSymbol = lexer.nextSymbol();
        //System.out.println("PARSER:\tDostalem symbol: " + currentSymbol);
    }
}
