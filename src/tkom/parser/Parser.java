package tkom.parser;

import tkom.lexer.Lexer;
import tkom.lexer.Symbol;

import javax.swing.text.html.HTML;
import java.util.LinkedList;
import java.util.Stack;

public class Parser {
    private Symbol currentSymbol;
    private Lexer lexer;
    private Stack<HtmlElement> htmlElements;


    // todo: sprawdzac zamkniecia tagow - kolejnosc zamykania ma zanczenie

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
                try {
                    parseOpeningTag();
                } catch (SyntaxErrorException e) {
                    e.printStackTrace();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            } else if (currentSymbol.getType() == Symbol.SymbolType.beginEndTag) {
                // </
                try {
                    parseClosingTag();
                } catch (SyntaxErrorException e) {
                    e.printStackTrace();
                    break;
                } catch (ClosingTagException e) {
                    e.printStackTrace();
                    break;
                }
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

        // Check if every opening tag has its own closing tag
        try {
            openingTag();
        } catch (ClosingTagException e) {
            e.printStackTrace();
        }
    }

    private void parseText() {
        parseTextStartingWith("");
    }

    private void parseTextStartingWith(String text) {
        // caly tekst pomiedzy znacznikami
        StringBuilder value = new StringBuilder(text);
        while (textTypeSymbol(currentSymbol)) {
            value.append(currentSymbol.getValue());
            value.append(" ");
            nextSymbol();
        }

        pushStack(new HtmlElement(HtmlElement.ElementType.text, value.toString(), currentSymbol.getPosition()));
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

        nextSymbol();
        StringBuilder value = new StringBuilder("");
        while (currentSymbol.getType() != Symbol.SymbolType.finishTag) {
            value.append(currentSymbol.getValue());
            value.append(" ");
            nextSymbol();
        }
        value.deleteCharAt(value.lastIndexOf(" "));
        pushStack(new HtmlElement(HtmlElement.ElementType.doctype, value.toString(), currentSymbol.getPosition()));
        nextSymbol();
    }

    private void parseComment() {
        nextSymbol();
        StringBuilder value = new StringBuilder("");
        while (currentSymbol.getType() != Symbol.SymbolType.finishComment) {
            value.append(currentSymbol.getValue());
            value.append(" ");
            nextSymbol();
        }
        pushStack(new HtmlElement(HtmlElement.ElementType.comment, value.toString(), currentSymbol.getPosition()));
        nextSymbol();
    }


    private void parseClosingTag() throws SyntaxErrorException, ClosingTagException {
        HtmlTag tag = new HtmlTag(currentSymbol.getPosition());
        nextSymbol();
        if (currentSymbol.getType() == Symbol.SymbolType.data) {
            // tag name
            tag.setName(currentSymbol.getValue());
        } else {
            System.out.println("ERROR: In parseClosingTag (1)");
            LinkedList<String> expected = new LinkedList<>();
            expected.add("tagName");
            throw new SyntaxErrorException(expected, currentSymbol);
        }

        nextSymbol();
        if(currentSymbol.getType() == Symbol.SymbolType.finishTag) {
            tag.setType(HtmlTag.TagType.closing);
        } else {
            // niepoprawne zamkniecie closingTagu
            System.out.println("ERROR: In parseClosingTag (2)");
            LinkedList<String> expected = new LinkedList<>();
            expected.add(">");
            throw new SyntaxErrorException(expected, currentSymbol);
        }

        // Add tag to html elements stack
        pushStack(tag);

        // Close corresponding opening tag
        closeTag(tag);
        nextSymbol();
    }

    private void parseOpeningTag() throws SyntaxErrorException, Exception {
        // teraz mam tylko <

        nextSymbol();
        HtmlTag tag = new HtmlTag(currentSymbol.getPosition());

        if (currentSymbol.getType() == Symbol.SymbolType.data) {
            // znaleziono nazwe tagu, teraz mam <tagName
            tag.setName(currentSymbol.getValue());
        } else {
            parseTextStartingWith("<");
            return;
        }

        nextSymbol();

        // jesli jest data, to na pewno jest to atrybut, przeparsuj je
        if (currentSymbol.getType() == Symbol.SymbolType.data)
            parseAttributes(tag);

        // koniec parsowania atrybutow, teraz szukamy zamkniecia
        if (currentSymbol.getType() == Symbol.SymbolType.finishTag)
            tag.setType(HtmlTag.TagType.opening);
        else if (currentSymbol.getType() == Symbol.SymbolType.finishSelfClosingTag) {
            tag.setType(HtmlTag.TagType.selfClosing);
            tag.setClosed(true);
        } else {
            System.out.println("ERROR: In parseOpeningTag (1)");
            LinkedList<String> expected = new LinkedList<>();
            expected.add(">");
            expected.add("/>");
            expected.add("attributeName");
            throw new SyntaxErrorException(expected, currentSymbol);
        }

        // Add tag to html elements stack
        pushStack(tag);
        nextSymbol();
    }

    private void parseAttributes(HtmlTag tag) throws SyntaxErrorException, Exception {
        while (currentSymbol.getType() == Symbol.SymbolType.data) {
            parseAttribute(tag);
            //nextSymbol();
        }
    }

    private void parseAttribute(HtmlTag tag) throws SyntaxErrorException, Exception {
        // parse one attribute

        // name, mam na pewno <tagName attrName
        String attrName = currentSymbol.getValue();

        nextSymbol();
        if (currentSymbol.getType() == Symbol.SymbolType.attrributeAssing) {
            // <tagName attrName=
            nextSymbol();
            if (currentSymbol.getType() == Symbol.SymbolType.data || currentSymbol.getType() == Symbol.SymbolType.numeric) {
                // unquoted attr value
                tag.addAttribute(attrName, currentSymbol.getValue(), Attribute.AttributeType.unquoted);
            } else if (currentSymbol.getType() == Symbol.SymbolType.singleQuote) {
                // single quoted attr value
                nextSymbol();
                while (currentSymbol.getType() != Symbol.SymbolType.singleQuote) {
                    tag.addAttribute(attrName, currentSymbol.getValue(), Attribute.AttributeType.singleQuoted);
                    nextSymbol();
                }
            } else if (currentSymbol.getType() == Symbol.SymbolType.doubleQuote) {
                // double quoted attribute value
                nextSymbol();
                while (currentSymbol.getType() != Symbol.SymbolType.doubleQuote) {
                    tag.addAttribute(attrName, currentSymbol.getValue(), Attribute.AttributeType.doubleQuoted);
                    nextSymbol();
                }
            } else {
                System.out.println("ERROR: In parseAttribute");

                LinkedList<String> expected = new LinkedList<>();
                expected.add("value");
                expected.add("'");
                expected.add("\"");

                throw new SyntaxErrorException(expected, currentSymbol);
            }

            nextSymbol();
        } else {
            // no value attribute
            //System.out.println("No value attr\n");
            tag.addAttribute(attrName, "", Attribute.AttributeType.noValue);
        }
    }

    private void openingTag() throws ClosingTagException {
        for (HtmlElement element : htmlElements) {
            if (element.getType() == HtmlElement.ElementType.tag && ((HtmlTag)element).getTagType() == HtmlTag.TagType.opening && !((HtmlTag)element).isClosed())
                throw new ClosingTagException((HtmlTag)element, null, element.getPosition());
        }
    }

    private void closeTag(HtmlTag closingTag) throws ClosingTagException {
        // todo lepsza nazwa
        // znajdz tag odpowiadajacy
        HtmlTag openingTag = null;

        // todo zrob na stremach
        for(HtmlElement element : htmlElements) {
            if (element.getType() == HtmlElement.ElementType.tag && ((HtmlTag)element).getName().equals(closingTag.getName()) && !((HtmlTag)element).isClosed() && ((HtmlTag)element).getTagType() == HtmlTag.TagType.opening) {
                openingTag = (HtmlTag) element;
                break;
            }
        }

        if (openingTag != null) {
            openingTag.setClosed(true);
            closingTag.setClosed(true);
        } else {
            throw new ClosingTagException(null, closingTag, currentSymbol.getPosition());
        }

    }


    private void pushStack(HtmlElement element) {
        //System.out.println(element);
        htmlElements.push(element);
    }


    private void nextSymbol() {
        currentSymbol = lexer.nextSymbol();
        //System.out.println("PARSER:\tDostalem symbol: " + currentSymbol);
    }
}
