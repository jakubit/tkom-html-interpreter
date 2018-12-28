package tkom.parser;

import tkom.lexer.Lexer;
import tkom.lexer.Symbol;

import java.util.LinkedList;
import java.util.Stack;

public class Parser {
    private Symbol currentSymbol;
    private Lexer lexer;
    private Stack<HtmlElement> htmlElements;
    private boolean strict;

    /* TODO: KNOWN BUGS:
     * # ingoruje atrybuty doubleQuoted z pusta wartoscia
     * # problem parsowaniem komentarza! nie znajduje --> jesli cos przed nim stoi bez spacji
     * # problem z whitespaceami - dodaje je na rympal
     * # nie parsuj zawartosci tagu <script></script>
     */


    // todo: sprawdzac zamkniecia tagow - kolejnosc zamykania ma zanczenie

    public Parser(Lexer lexer, boolean strict) {
        this.lexer = lexer;
        this.strict = strict;
        htmlElements = new Stack<>();
    }

    public void printStack() {
        htmlElements.forEach(System.out::println);
    }

    public void parse() throws Exception {
        nextSymbol();
        while (currentSymbol.getType() != Symbol.SymbolType.EOF) {
            parseElement();
        }
        // Check if every opening tag has its own closing tag
        if (strict) {
            closeTags();
            checkClosings();
        }

    }

    private void parseElement() throws Exception {

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
        return symbol.getType() != Symbol.SymbolType.beginStartTag
                && symbol.getType() != Symbol.SymbolType.beginEndTag
                && symbol.getType() != Symbol.SymbolType.beginComment
                && symbol.getType() != Symbol.SymbolType.beginDoctype;
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

/*    private void checkOpeningTags() throws ClosingTagException {
        for (HtmlElement element : htmlElements) {
            if (element.getType() == HtmlElement.ElementType.tag && ((HtmlTag)element).getTagType() == HtmlTag.TagType.opening && !((HtmlTag)element).isClosed())
                throw new ClosingTagException((HtmlTag)element, null, element.getPosition());
        }
    }*/

    /*private void checkCloseTag(HtmlTag closingTag) throws ClosingTagException {
        // todo lepsza nazwa
        *//* Dostalem juz closingTag, teraz musze znalezc odpowiadajacy mu openingTag.
         * Lece po stosie i szukam HtmlTag ktory:
         * 1. nie jest zamkniety
         * 2. jest typu opening
         *
         * Jak juz go znajde, to sprawdzam jego name.
         * Jesli jest rowny temu z closing tag, to git - oznaczam openingTag jako zamkniety.
         * W p. p. rzucam wyjatek
         * *//*

        HtmlTag openingTag = null;

        // todo bug:
        for (int i = htmlElements.size() - 1; i >= 0; i--) {
            HtmlElement element = htmlElements.get(i);
            if (element.getType() == HtmlElement.ElementType.tag && !((HtmlTag)element).isClosed() && ((HtmlTag)element).getTagType() == HtmlTag.TagType.opening) {
                if (((HtmlTag)element).getName().toLowerCase().equals(closingTag.getName().toLowerCase())) {
                    openingTag = (HtmlTag) element;
                    break;
                } else {
                    System.out.println("zla kolejnosc tagow zamykajacych");
                    //throw new ClosingTagException((HtmlTag) element, null, closingTag.getPosition());
                }
            }
        }

        if (openingTag != null) {
            openingTag.setClosed(true);
            closingTag.setClosed(true);
        } else {
            throw new ClosingTagException(null, closingTag, currentSymbol.getPosition());
        }

    }*/

    private void closeTags() throws ClosingTagException {
        int index = 0;
        for (HtmlElement element : htmlElements) {
            if (element.getType() == HtmlElement.ElementType.tag) {
                HtmlTag tag = (HtmlTag) element;
                if (tag.getTagType() == HtmlTag.TagType.selfClosing) {
                    tag.setClosed(true);
                } else if (tag.getTagType() == HtmlTag.TagType.opening) {
                    closeTag(tag, index);
                }
            }
            index++;
        }
    }

    private void closeTag(HtmlTag openingTag, int index) throws ClosingTagException {

        for (int i = index + 1; i < htmlElements.size(); i++) {
            if (htmlElements.get(i).getType() == HtmlElement.ElementType.tag) {
                HtmlTag tag = (HtmlTag) htmlElements.get(i);
                if (tag.getTagType() == HtmlTag.TagType.closing && tag.getName().toLowerCase().equals(openingTag.getName().toLowerCase())) {
                    // znaleziono domkniecie
                    openingTag.setClosed(true);
                    tag.setClosed(true);
                    break;
                } else if (tag.getTagType() == HtmlTag.TagType.closing && tag.isClosed()) {
                    // error
                    throw new ClosingTagException(openingTag, null, openingTag.getPosition());
                }
            }
        }
    }

    private void checkClosings() throws ClosingTagException {
        for (HtmlElement element : htmlElements) {
            if (element.getType() == HtmlElement.ElementType.tag) {
                HtmlTag tag = (HtmlTag) element;
                if (!tag.isClosed()) {
                    if (tag.getTagType() == HtmlTag.TagType.closing)
                        throw new ClosingTagException(null, tag, tag.getPosition());
                    else
                        throw new ClosingTagException(tag, null, tag.getPosition());
                }

            }
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

    private void skipScript(HtmlTag tag) throws SyntaxErrorException, ClosingTagException {
        // find </script>
        if (tag.getTagType() == HtmlTag.TagType.opening && tag.getName().toLowerCase().equals("script")) {
            // skip content

            System.out.println("Skipping script starting at " + currentSymbol);


            while (currentSymbol.getType() != Symbol.SymbolType.EOF) {
                // keep looking for </script>
                if (currentSymbol.getType() == Symbol.SymbolType.beginEndTag) {
                    //System.out.println("found " + currentSymbol);
                    nextSymbol();
                    if (currentSymbol.getType() == Symbol.SymbolType.data && currentSymbol.getValue().toLowerCase().equals("script")) {
                        //System.out.println("found " + currentSymbol);
                        // </script
                        // na pewno koniec JSa, teraz trzeba zajac sie do konca tagiem
                        nextSymbol();
                        if (currentSymbol.getType() == Symbol.SymbolType.finishTag) {
                            // </script> ok
                            HtmlTag endTag = new HtmlTag("script", HtmlTag.TagType.closing, currentSymbol.getPosition());
                            pushStack(endTag);
                            nextSymbol();
                        } else {
                            LinkedList<String> expected = new LinkedList<>();
                            expected.add(">");
                            throw new SyntaxErrorException(expected, currentSymbol);
                        }
                        break;
                    } else {
                        nextSymbol();
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
