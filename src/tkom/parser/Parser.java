package tkom.parser;

import tkom.lexer.Lexer;
import tkom.lexer.Symbol;

import java.util.Stack;

public class Parser {
    private Symbol currentSymbol;
    private Lexer lexer;
    private Stack<HtmlElement> htmlElements;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        htmlElements = new Stack<>();
    }

    public void parse() {
        nextSymbol();

        while (currentSymbol.getType() != Symbol.SymbolType.EOF) {
            // parse
            nextSymbol();

            if(currentSymbol.getType() == Symbol.SymbolType.beginStartTag) {
                // <
                // parse opening tag
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
            }
        }
    }

    private void parseDoctype() {
        // todo wartosci
        while (currentSymbol.getType() != Symbol.SymbolType.finishTag)
            nextSymbol();

        htmlElements.push(new HtmlElement(HtmlElement.ElementType.doctype));
    }

    private void parseComment() {
        // todo tekst komentarza

        while (currentSymbol.getType() != Symbol.SymbolType.finishComment)
            nextSymbol();

        htmlElements.push(new HtmlElement(HtmlElement.ElementType.comment));
    }


    private void parseClosingTag() {
        HtmlTag tag = new HtmlTag();
        nextSymbol();
        if (currentSymbol.getType() == Symbol.SymbolType.data) {
            // tag name
            tag.setName(currentSymbol.getValue());
        } else {
            // todo
            System.out.println("ERROR: In parseClosingtag");
            return;
        }

        nextSymbol();
        if(currentSymbol.getType() == Symbol.SymbolType.finishTag) {
            tag.setType(HtmlTag.TagType.closing);
        } else {
            // todo
            System.out.println("ERROR: In parseClosingtag");
            return;
        }

        // Add tag to html elements stack
        htmlElements.push(tag);
    }

    private void parseOpeningTag() {
        nextSymbol();
        HtmlTag tag = new HtmlTag();

        if (currentSymbol.getType() == Symbol.SymbolType.data)
            tag.setName(currentSymbol.getValue());
        else
            //todo  error
            ;

        nextSymbol();
        if (currentSymbol.getType() == Symbol.SymbolType.data)
            parseAttributes(tag);
        else if (currentSymbol.getType() == Symbol.SymbolType.finishTag)
            tag.setType(HtmlTag.TagType.opening);
        else if (currentSymbol.getType() == Symbol.SymbolType.finishSelfClosingTag)
            tag.setType(HtmlTag.TagType.selfClosing);
        else {
            // todo
            System.out.println("ERROR: parseOpeningtag");
            return;
        }

        // Add tag to html elements stack
        htmlElements.push(tag);
    }

    private void parseAttributes(HtmlTag tag) {
        String attrName = currentSymbol.getValue();
        nextSymbol();

        do {
            if(currentSymbol.getType() == Symbol.SymbolType.attrributeAssing) {
                nextSymbol();
                if(currentSymbol.getType() == Symbol.SymbolType.data) {
                    //unquoted attribute value
                    tag.addAtribute(attrName, currentSymbol.getValue());
                } else if(currentSymbol.getType() == Symbol.SymbolType.singleQuote) {
                    // single quoted attribute value
                    StringBuilder value = new StringBuilder("");
                    nextSymbol();
                    while (currentSymbol.getType() != Symbol.SymbolType.singleQuote) {
                        value.append(currentSymbol.getValue());
                        nextSymbol();
                    }
                    tag.addAtribute(attrName, value.toString());
                } else if (currentSymbol.getType() == Symbol.SymbolType.doubleQuote) {
                    // double quoted attribute value
                    StringBuilder value = new StringBuilder("");
                    nextSymbol();
                    while (currentSymbol.getType() != Symbol.SymbolType.doubleQuote) {
                        value.append(currentSymbol.getValue());
                        nextSymbol();
                    }
                    tag.addAtribute(attrName, value.toString());
                } else
                    // todo error
                    ;
            } else {
                // no value attribute
                tag.addAtribute(attrName, "");
            }
            nextSymbol();
        } while (currentSymbol.getType() != Symbol.SymbolType.finishTag && currentSymbol.getType() != Symbol.SymbolType.finishSelfClosingTag);
    }


    private void nextSymbol() {
        currentSymbol = lexer.nextSymbol();
        System.out.println("PARSER:\tDostalem symbol: " + currentSymbol);
    }
}
