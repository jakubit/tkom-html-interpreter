package tkom.parser;

import tkom.lexer.Lexer;
import tkom.lexer.Symbol;
import tkom.source.TextPosition;

import java.util.LinkedList;
import java.util.List;
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

    // todo: sprawdzic jak sie zachowa gdy trafi na unexpected EOF w kazdym przypadku parsowania

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
                        value.append(" \"");

                        List<String> link = parseQuoted(Symbol.SymbolType.doubleQuote);
                        for (String s : link) {
                            value.append(s);
                            value.append(" ");
                        }
                        if (link.size() > 0) {
                            int index = value.lastIndexOf(" ");
                            //if (index > 0)
                                value.deleteCharAt(index);
                        }
                        value.append("\"");

                        nextSymbol();
                        if (currentSymbol.getType() == Symbol.SymbolType.doubleQuote) {
                            value.append(" \"");

                            for (String s : parseQuoted(Symbol.SymbolType.doubleQuote)) {
                                value.append(s);
                                value.append(" ");
                            }
                            if (link.size() > 0) {
                                int index = value.lastIndexOf(" ");
                                //if (index > 0)
                                    value.deleteCharAt(index);
                            }
                            value.append("\"");
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
            tag.setName(parseName());
        } else {
            System.out.println("ERROR: In parseClosingTag (1)");
            LinkedList<String> expected = new LinkedList<>();
            expected.add("tagName");
            throw new SyntaxErrorException(expected, currentSymbol);
        }

        //nextSymbol();
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
            tag.setName(parseName());
        } else {
            parseTextStartingWith("<");
            return;
        }

        //nextSymbol();

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
        // todo parsowac nazwe az do
        String attrName = parseName();

        //nextSymbol();
        if (currentSymbol.getType() == Symbol.SymbolType.attrributeAssing) {
            // <tagName attrName=
            nextSymbol();
            if (currentSymbol.getType() == Symbol.SymbolType.data || currentSymbol.getType() == Symbol.SymbolType.numeric) {
                // unquoted attr value
                tag.addAttribute(attrName, parseName(), Attribute.AttributeType.unquoted);
            } else if (currentSymbol.getType() == Symbol.SymbolType.singleQuote) {
                // single quoted attr value
                List<String> values = parseQuoted(Symbol.SymbolType.singleQuote);
                if (values.size() == 0)
                    tag.addAttribute(attrName, "", Attribute.AttributeType.singleQuoted);
                for (String v : values) {
                    tag.addAttribute(attrName, v, Attribute.AttributeType.singleQuoted);
                }

                nextSymbol();

                /*nextSymbol();
                while (currentSymbol.getType() != Symbol.SymbolType.singleQuote) {
                    tag.addAttribute(attrName, currentSymbol.getValue(), Attribute.AttributeType.singleQuoted);
                    nextSymbol();
                }*/
            } else if (currentSymbol.getType() == Symbol.SymbolType.doubleQuote) {
                // double quoted attribute value
                //nextSymbol();
                List<String> values = parseQuoted(Symbol.SymbolType.doubleQuote);
                if (values.size() == 0)
                    tag.addAttribute(attrName, "", Attribute.AttributeType.doubleQuoted);
                for (String v : values) {
                    tag.addAttribute(attrName, v, Attribute.AttributeType.doubleQuoted);
                }

                nextSymbol();

                /*while (currentSymbol.getType() != Symbol.SymbolType.doubleQuote) {
                    tag.addAttribute(attrName, currentSymbol.getValue(), Attribute.AttributeType.doubleQuoted);
                    nextSymbol();
                }*/
            } else {
                System.out.println("ERROR: In parseAttribute");

                LinkedList<String> expected = new LinkedList<>();
                expected.add("value");
                expected.add("'");
                expected.add("\"");

                throw new SyntaxErrorException(expected, currentSymbol);
            }

            //
        } else {
            // no value attribute
            //System.out.println("No value attr\n");
            tag.addAttribute(attrName.toString(), "", Attribute.AttributeType.noValue);
        }
    }

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
        int nested = 0;
        for (int i = index + 1; i < htmlElements.size(); i++) {
            if (htmlElements.get(i).getType() == HtmlElement.ElementType.tag) {
                HtmlTag tag = (HtmlTag) htmlElements.get(i);
                if (tag.getName().toLowerCase().equals(openingTag.getName().toLowerCase())) {
                    if (tag.getTagType() == HtmlTag.TagType.opening)
                        nested++;
                    else if (tag.getTagType() == HtmlTag.TagType.closing) {
                        if (nested == 0) {
                            // znaleziono domkniecie
                            openingTag.setClosed(true);
                            tag.setClosed(true);
                            break;
                        } else {
                            nested--;
                        }
                    }
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

    private String parseName() {
        StringBuilder name = new StringBuilder(currentSymbol.getValue());

        // dopoki kolejne symbole sa skonkatenowane i rozne od [=, />, >, EOF] to wczytuj nazwe atrybutu
        Symbol lastSymbol = currentSymbol;
        nextSymbol();
        while (areConcatenated(lastSymbol, currentSymbol) && currentSymbol.getType() != Symbol.SymbolType.attrributeAssing && currentSymbol.getType() != Symbol.SymbolType.finishSelfClosingTag && currentSymbol.getType() != Symbol.SymbolType.finishTag && currentSymbol.getType() != Symbol.SymbolType.EOF) {
            name.append(currentSymbol.getValue());
            lastSymbol = currentSymbol;
            nextSymbol();
        }

        return name.toString();
    }

    private List<String> parseQuoted(Symbol.SymbolType quoted) throws UnexpectedEOFException {
        List<String> toReturn = new LinkedList<>();

        nextSymbol();
        List<Symbol> symbols = new LinkedList<>();
        while (currentSymbol.getType() != quoted && currentSymbol.getType() != Symbol.SymbolType.EOF) {
            symbols.add(currentSymbol);
            nextSymbol();
        }

        // EOF error handling
        if (currentSymbol.getType() == Symbol.SymbolType.EOF) {
            throw new UnexpectedEOFException("\"", currentSymbol.getPosition());
        }

        // Bulid value from List
        if (symbols.size() > 0) {
            for (int i = 1; i < symbols.size(); i++) {
                StringBuilder stringBuilder = new StringBuilder(symbols.get(i-1).getValue());
                while (i < symbols.size() && areConcatenated(symbols.get(i - 1), symbols.get(i))) {
                    stringBuilder.append(symbols.get(i).getValue());
                    i++;
                }
                toReturn.add(stringBuilder.toString());
            }

            if (symbols.size() > 1){
                if (areConcatenated(symbols.get(symbols.size() - 2), symbols.get(symbols.size() - 1)))
                    symbols.get(symbols.size() - 2).getValue().concat(symbols.get(symbols.size() - 1).getValue());
                else
                    toReturn.add(symbols.get(symbols.size() - 1).getValue());
            } else {
                toReturn.add(symbols.get(0).getValue());
            }

        }

        return toReturn;
    }

    private boolean areConcatenated(Symbol first, Symbol second) {
        if (first.getPosition().getCharIndex() + first.getValue().length() == second.getPosition().getCharIndex())
            return true;

        return false;
    }

    private void skipScript(HtmlTag tag) throws SyntaxErrorException, ClosingTagException {
        // find </script>
        if (tag.getTagType() == HtmlTag.TagType.opening && tag.getName().toLowerCase().equals("script")) {
            // skip content

            //System.out.println("Skipping script starting at " + currentSymbol);


            while (currentSymbol.getType() != Symbol.SymbolType.EOF) {
                // keep looking for </script>
                if (currentSymbol.getType() == Symbol.SymbolType.beginEndTag) {
                    //System.out.println("found " + currentSymbol);
                    TextPosition position = currentSymbol.getPosition();
                    nextSymbol();
                    if (currentSymbol.getType() == Symbol.SymbolType.data && currentSymbol.getValue().toLowerCase().equals("script")) {
                        //System.out.println("found " + currentSymbol);
                        // </script
                        // na pewno koniec JSa, teraz trzeba zajac sie do konca tagiem
                        nextSymbol();
                        if (currentSymbol.getType() == Symbol.SymbolType.finishTag) {
                            // </script> ok
                            HtmlTag endTag = new HtmlTag("script", HtmlTag.TagType.closing, position);
                            //System.out.println("Skipped to " + endTag.getPosition());
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
