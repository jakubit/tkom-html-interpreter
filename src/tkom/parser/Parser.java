package tkom.parser;

import tkom.lexer.Lexer;
import tkom.lexer.Symbol;

import javax.swing.text.html.HTML;
import java.io.EOFException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Parser {
    private Symbol currentSymbol;
    private Lexer lexer;
    private Stack<HtmlElement> htmlElements;

    /* TODO: KNOWN BUGS:
     * # ingoruje atrybuty doubleQuoted z pusta wartoscia
     * # problem z whitespaceami - dodaje je na rympal
     * # nie parsuj zawartosci tagu <script></script>
     */


    // todo: sprawdzac zamkniecia tagow - kolejnosc zamykania ma zanczenie

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        htmlElements = new Stack<>();
    }

    public void printStack() {
        htmlElements.stream().forEach(System.out::println);
    }

    public void parse() throws Exception {
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
                // <!
                parseDoctype();
            } else {
                parseText();
            }
        }

        // Check if every opening tag has its own closing tag
        openingTag();
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

    private void parseDoctype() throws SyntaxErrorException, UnexpectedEOFException {
        nextSymbol();
        StringBuilder value = new StringBuilder();

        if (!currentSymbol.getValue().toLowerCase().equals("doctype")) {
           // traktuj jako tekst
            parseTextStartingWith("<!" + currentSymbol.getValue());
        } else {
            // <!doctype
            value.append(currentSymbol.getValue());
            nextSymbol();
            if (!currentSymbol.getValue().toLowerCase().equals("html")) {
                LinkedList<String> expected = new LinkedList<>();
                expected.add("html");
                throw new SyntaxErrorException(expected, currentSymbol);
            } else {
                // <!doctype html
                value.append(" ");
                value.append(currentSymbol.getValue());
                nextSymbol();
                if (currentSymbol.getType() == Symbol.SymbolType.finishTag) {
                    // <!doctype html> HTML5 ok
                    //value.append(currentSymbol.getValue());
                    pushStack(new HtmlElement(HtmlElement.ElementType.doctype, value.toString(), currentSymbol.getPosition()));
                } else if (currentSymbol.getValue().toLowerCase().equals("public")) {
                    // <!doctype html public
                    value.append(" ");
                    value.append(currentSymbol.getValue());
                    nextSymbol();
                    if (currentSymbol.getType() == Symbol.SymbolType.doubleQuote) {
                        value.append(" ");
                        value.append(parseDoubleQuoted());
                        nextSymbol();
                        if (currentSymbol.getType() == Symbol.SymbolType.doubleQuote) {
                            value.append(" ");
                            value.append(parseDoubleQuoted());
                            nextSymbol();
                            if (currentSymbol.getType() == Symbol.SymbolType.finishTag) {
                                // <!doctype html public "link1" "link2"> HTML 4 and lower ok
                                //value.append(currentSymbol.getValue());
                                pushStack(new HtmlElement(HtmlElement.ElementType.doctype, value.toString(), currentSymbol.getPosition()));
                            } else {
                                LinkedList<String> expected = new LinkedList<>();
                                expected.add(">");
                                throw new SyntaxErrorException(expected, currentSymbol);
                            }
                        } else {
                            LinkedList<String> expected = new LinkedList<>();
                            expected.add("\"");
                            throw new SyntaxErrorException(expected, currentSymbol);
                        }
                    } else {
                        LinkedList<String> expected = new LinkedList<>();
                        expected.add("\"");
                        throw new SyntaxErrorException(expected, currentSymbol);
                    }
                } else {
                    LinkedList<String> expected = new LinkedList<>();
                    expected.add("public");
                    throw new SyntaxErrorException(expected, currentSymbol);
                }
            }
        }
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

        // Next symbol
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

        // If <script> then skip content
        skipScript(tag);
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

        // todo bug:
        for (int i = htmlElements.size() - 1; i >= 0; i--) {
            HtmlElement element = htmlElements.get(i);
            if (element.getType() == HtmlElement.ElementType.tag && !((HtmlTag)element).isClosed() && ((HtmlTag)element).getTagType() == HtmlTag.TagType.opening) {
                if (((HtmlTag)element).getName().equals(closingTag.getName())) {
                    openingTag = (HtmlTag) element;
                    break;
                } else {
                    // zla kolejnosc tagow zamykajacych
                    throw new ClosingTagException((HtmlTag) element, null, closingTag.getPosition());
                }
            }
        }

        if (openingTag != null) {
            openingTag.setClosed(true);
            closingTag.setClosed(true);
        } else {
            throw new ClosingTagException(null, closingTag, currentSymbol.getPosition());
        }

    }

    private String parseDoubleQuoted() throws UnexpectedEOFException {
        StringBuilder value = new StringBuilder("\"");

        nextSymbol();
        while (currentSymbol.getType() != Symbol.SymbolType.doubleQuote && currentSymbol.getType() != Symbol.SymbolType.EOF) {
            value.append(currentSymbol.getValue());
            nextSymbol();
        }

        // EOF error handling
        if (currentSymbol.getType() == Symbol.SymbolType.EOF) {
            throw new UnexpectedEOFException("\"", currentSymbol.getPosition());
        }

        value.append(currentSymbol.getValue());

        return value.toString();
    }

    private void skipScript(HtmlTag tag){
        if (tag.getTagType() == HtmlTag.TagType.opening && tag.getName().toLowerCase().equals("script")) {
            // skip content
            System.out.println("Skipping script starting at " + currentSymbol.getPosition());

            while (currentSymbol.getType() != Symbol.SymbolType.EOF) {
                //System.out.println("skipped");
                if (currentSymbol.getType() == Symbol.SymbolType.beginEndTag) {
                    nextSymbol();
                    if (currentSymbol.getValue().toLowerCase().equals("script")) {
                        nextSymbol();
                        if (currentSymbol.getType() == Symbol.SymbolType.finishTag) {
                            nextSymbol();
                            break;
                        }
                    }
                } else {
                    nextSymbol();
                }
            }
            System.out.println("Skipped to " + currentSymbol.getPosition());
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
