package tkom.model.parser;

import tkom.model.lexer.Lexer;
import tkom.model.lexer.Symbol;
import tkom.model.source.TextPosition;

import javax.swing.text.html.HTML;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Parser {
    private Symbol currentSymbol;
    private Lexer lexer;
    private List<HtmlElement> htmlElements;
    private boolean strict;

    /* TODO: KNOWN BUGS:
     */

    // todo: sprawdzic jak sie zachowa gdy trafi na unexpected EOF w kazdym przypadku parsowania

    public Parser(Lexer lexer, boolean strict) {
        this.lexer = lexer;
        this.strict = strict;
        htmlElements = new LinkedList<>();
    }

    public void reset() {
        htmlElements.clear();
    }

    public void setStrict (boolean strict) {
        this.strict = strict;
    }

    /**
     * Starts parsing process. Calls {@link #parseElement()} until EOF token occurs.
     * @throws Exception
     */
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

    /**
     * Parses one HTML Element
     * @throws Exception
     */
    private void parseElement() throws Exception {
        // todo przerzucic do metod parse...

            if(currentSymbol.getType() == Symbol.SymbolType.beginStartTag) {
                // <  >  | <   />
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

    /**
     * Parses plain text
     * @throws UnexpectedEOFException when finds EOF token
     */
    private void parseText() throws UnexpectedEOFException {
        parseTextStartingWith("");
    }

    /**
     * Parses plain text starting with given string
     * @param text starting string
     * @throws UnexpectedEOFException when finds EOF token
     */
    private void parseTextStartingWith(String text) throws UnexpectedEOFException {
        // caly tekst pomiedzy znacznikami
        StringBuilder value = new StringBuilder(text);
        while (textTypeSymbol(currentSymbol)) {
            if (currentSymbol.getType() == Symbol.SymbolType.EOF)
                throw new UnexpectedEOFException("", currentSymbol.getPosition());

            value.append(currentSymbol.getValue());
            value.append(" ");
            nextSymbol();
        }

        pushStack(new HtmlElement(HtmlElement.ElementType.text, value.toString(), currentSymbol.getPosition()));
    }

    /**
     * Checks if given symbol is allowed to be in plain text
     * @param symbol lexical symbol
     * @return true if given symbol is allowed to be in plain text, otherwise false
     */
    private boolean textTypeSymbol(Symbol symbol) {
        Symbol.SymbolType type = symbol.getType();
        return type != Symbol.SymbolType.beginStartTag
                && type != Symbol.SymbolType.beginEndTag
                && type != Symbol.SymbolType.beginComment
                && type != Symbol.SymbolType.beginDoctype;
    }

    /**
     * Parses doctype tag
     * @throws SyntaxErrorException when syntax error occurs
     * @throws UnexpectedEOFException when finds EOF token
     */
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
                    expected.add("> (in HTML 5)");
                    expected.add("public (in HTML 4 and lower)");
                    throw new SyntaxErrorException(expected, currentSymbol);
                }
            }
        }
        nextSymbol();
    }

    /**
     * Parses comment tag
     * @throws UnexpectedEOFException when when finds EOF token
     */
    private void parseComment() throws UnexpectedEOFException {
        StringBuilder value = new StringBuilder("");
        Symbol lastSymbol = currentSymbol;
        nextSymbol();

        if (currentSymbol.getType() != Symbol.SymbolType.finishTag || !areConcatenated(lastSymbol, currentSymbol)) {
            // <!--content -->

            // read content
            while (currentSymbol.getType() != Symbol.SymbolType.finishComment && currentSymbol.getType() != Symbol.SymbolType.EOF) {
                if (currentSymbol.getType() == Symbol.SymbolType.beginComment) {
                    lastSymbol = currentSymbol;
                    nextSymbol();
                    if (currentSymbol.getType() == Symbol.SymbolType.finishTag && areConcatenated(lastSymbol, currentSymbol)) {
                        // trafiono na <!--> koniec czytania komentarza
                        value.append(lastSymbol.getValue());
                        break;
                    } else if (currentSymbol.getType() ==  Symbol.SymbolType.EOF)
                        throw new UnexpectedEOFException(">", currentSymbol.getPosition());
                }

                value.append(currentSymbol.getValue());
                value.append(" ");
                nextSymbol();
            }
            if (currentSymbol.getType() == Symbol.SymbolType.EOF)
                throw new UnexpectedEOFException("-->", currentSymbol.getPosition());
        }

        pushStack(new HtmlElement(HtmlElement.ElementType.comment, value.toString(), currentSymbol.getPosition()));
        nextSymbol();
    }

    /**
     * Parses closing tag
     * @throws SyntaxErrorException when tag name is missing or tag is closed improperly
     */
    private void parseClosingTag() throws SyntaxErrorException {
        HtmlTag tag = new HtmlTag(currentSymbol.getPosition());
        nextSymbol();
        if (currentSymbol.getType() == Symbol.SymbolType.data) {
            // tag name
            tag.setName(parseName());
        } else {
            LinkedList<String> expected = new LinkedList<>();
            expected.add("tagName");
            throw new SyntaxErrorException(expected, currentSymbol);
        }

        //nextSymbol();
        if(currentSymbol.getType() == Symbol.SymbolType.finishTag) {
            tag.setType(HtmlTag.TagType.closing);
        } else {
            // niepoprawne zamkniecie closingTagu
            LinkedList<String> expected = new LinkedList<>();
            expected.add(">");
            throw new SyntaxErrorException(expected, currentSymbol);
        }

        // Add tag to html elements stack
        pushStack(tag);

        // Next symbol
        nextSymbol();
    }

    /**
     * Parses opening tag
     * @throws SyntaxErrorException when tag is closed improperly
     * @throws UnexpectedEOFException when finds EOF token
     */
    private void parseOpeningTag() throws SyntaxErrorException, UnexpectedEOFException {
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
        if (tag.getTagType() == HtmlTag.TagType.opening && tag.getName().toLowerCase().equals("script"))
            skipScript(tag);

        // If <style> then skip content
        if (tag.getTagType() == HtmlTag.TagType.opening && tag.getName().toLowerCase().equals("style"))
            skipStyle(tag);
    }

    /**
     * Parses tag's attributes. Calls {@link #parseAttribute(HtmlTag)} until there are attributes to be parsed.
     * @param tag tag which attributes will be parsed
     * @throws SyntaxErrorException
     * @throws UnexpectedEOFException
     */
    private void parseAttributes(HtmlTag tag) throws SyntaxErrorException, UnexpectedEOFException {
        // todo przeparsowac attr i dodac do taga
        while (currentSymbol.getType() == Symbol.SymbolType.data) {
            parseAttribute(tag);
            //nextSymbol();
        }
    }

    /**
     * Parses one attribute.
     * @param tag tag which attributes will be parsed
     * @throws SyntaxErrorException
     * @throws UnexpectedEOFException
     */
    private void parseAttribute(HtmlTag tag) throws SyntaxErrorException, UnexpectedEOFException {
        // parse one attribute

        // name, mam na pewno <tagName attrName
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
                for (String v : values)
                    tag.addAttribute(attrName, v, Attribute.AttributeType.singleQuoted);

                nextSymbol();
            } else if (currentSymbol.getType() == Symbol.SymbolType.doubleQuote) {
                // double quoted attribute value
                //nextSymbol();
                List<String> values = parseQuoted(Symbol.SymbolType.doubleQuote);
                if (values.size() == 0)
                    tag.addAttribute(attrName, "", Attribute.AttributeType.doubleQuoted);
                for (String v : values)
                    tag.addAttribute(attrName, v, Attribute.AttributeType.doubleQuoted);

                nextSymbol();
            } else {
                LinkedList<String> expected = new LinkedList<>();
                expected.add("value");
                expected.add("'");
                expected.add("\"");
                throw new SyntaxErrorException(expected, currentSymbol);
            }

        } else {
            // no value attribute
            tag.addAttribute(attrName.toString(), "", Attribute.AttributeType.noValue);
        }
    }

    /**
     * Marks matching tags as closed. Self closing tags are being marked as closed by default. Opening tags are being marked as closed with {@link #closeTag(HtmlTag, int)} method.
     * @throws ClosingTagException
     */
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

    /**
     * Looks up for closing tag for given tag starting at given index.
     * @param openingTag
     * @param index
     * @throws ClosingTagException when matching closing tag wasn't found
     */
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

    /**
     * Check if every tag is closed properly.
     * @throws ClosingTagException
     */
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

    /**
     * Parses consecutive tokens as name, until =, />, >, EOF token occurs.
     * @return parsed name
     */
    private String parseName() {
        StringBuilder name = new StringBuilder(currentSymbol.getValue());

        // dopoki kolejne symbole sa skonkatenowane i rozne od [=, />, >, EOF] to wczytuj nazwe atrybutu
        Symbol lastSymbol = currentSymbol;
        nextSymbol();
        while (areConcatenated(lastSymbol, currentSymbol)
                && currentSymbol.getType() != Symbol.SymbolType.attrributeAssing
                && currentSymbol.getType() != Symbol.SymbolType.finishSelfClosingTag
                && currentSymbol.getType() != Symbol.SymbolType.finishTag
                && currentSymbol.getType() != Symbol.SymbolType.EOF) {
            name.append(currentSymbol.getValue());
            lastSymbol = currentSymbol;
            nextSymbol();
        }

        return name.toString();
    }

    /**
     * Parses consecutive tokens between double or single quote.
     * @param quoted type of quote: single or double
     * @return parsed value
     * @throws UnexpectedEOFException
     */
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

    /**
     * Checks if two given tokens are concatenated.
     * @param first
     * @param second
     * @return true if given tokens are concatenated, otherwise false
     */
    private boolean areConcatenated(Symbol first, Symbol second) {
        if (first.getPosition().getCharIndex() + first.getValue().length() == second.getPosition().getCharIndex())
            return true;

        return false;
    }

    /**
     * Skips tokens until </script> token
     * @param tag
     * @throws SyntaxErrorException
     */
    private void skipScript(HtmlTag tag) throws SyntaxErrorException {
        StringBuilder scriptBody = new StringBuilder();
        TextPosition scriptPosition = currentSymbol.getPosition();
        while (currentSymbol.getType() != Symbol.SymbolType.EOF) {
            // keep looking for </script>
            if (currentSymbol.getType() == Symbol.SymbolType.beginEndTag) {
                TextPosition position = currentSymbol.getPosition();
                nextSymbol();
                if (currentSymbol.getType() == Symbol.SymbolType.data && currentSymbol.getValue().toLowerCase().equals("script")) {
                    // </script
                    // na pewno koniec JSa, teraz trzeba zajac sie do konca tagiem
                    nextSymbol();
                    if (currentSymbol.getType() == Symbol.SymbolType.finishTag) {
                        // </script> ok
                        pushStack(new HtmlElement(HtmlElement.ElementType.scriptBody, scriptBody.toString(), scriptPosition));
                        pushStack(new HtmlTag("script", HtmlTag.TagType.closing, position));
                        nextSymbol();
                    } else {
                        LinkedList<String> expected = new LinkedList<>();
                        expected.add(">");
                        throw new SyntaxErrorException(expected, currentSymbol);
                    }
                    break;
                } else {
                    scriptBody.append(currentSymbol.getValue());
                    nextSymbol();
                }
            } else {
                scriptBody.append(currentSymbol.getValue());
                nextSymbol();
            }
        }
    }

    private void skipStyle(HtmlTag tag) throws SyntaxErrorException {
        StringBuilder styleBody = new StringBuilder();
        TextPosition stylePosition = currentSymbol.getPosition();
        while (currentSymbol.getType() != Symbol.SymbolType.EOF) {
            // keep looking for </style>
            if (currentSymbol.getType() == Symbol.SymbolType.beginEndTag) {
                TextPosition position = currentSymbol.getPosition();
                nextSymbol();
                if (currentSymbol.getType() == Symbol.SymbolType.data && currentSymbol.getValue().toLowerCase().equals("style")) {
                    // </style
                    // na pewno koniec stylu, teraz trzeba zajac sie do konca tagiem
                    nextSymbol();
                    if (currentSymbol.getType() == Symbol.SymbolType.finishTag) {
                        // </style> ok
                        pushStack(new HtmlElement(HtmlElement.ElementType.styleBody, styleBody.toString(), stylePosition));
                        pushStack(new HtmlTag("style", HtmlTag.TagType.closing, position));
                        nextSymbol();
                    } else {
                        LinkedList<String> expected = new LinkedList<>();
                        expected.add(">");
                        throw new SyntaxErrorException(expected, currentSymbol);
                    }
                    break;
                } else {
                    styleBody.append(currentSymbol.getValue());
                    nextSymbol();
                }
            } else {
                styleBody.append(currentSymbol.getValue());
                nextSymbol();
            }
        }
    }

    /**
     * Pushes given element to the stack of HTML Elements
     * @param element
     */
    private void pushStack(HtmlElement element) {
        htmlElements.add(element);
    }

    /**
     * Prints out stack of HTML Elements
     */
    public void printStack() {
        htmlElements.forEach(System.out::println);
    }

    /**
     * Gets next lexical symbol from lexer
     */
    private void nextSymbol() {
        currentSymbol = lexer.nextSymbol();
    }

    public List<HtmlElement> getHtmlElements() {
        return htmlElements;
    }
}