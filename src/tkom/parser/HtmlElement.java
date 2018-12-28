package tkom.parser;

import tkom.lexer.Symbol;
import tkom.source.TextPosition;

public class HtmlElement {
    public enum ElementType {
        tag,
        text,
        comment,
        doctype
    }

    private final ElementType type;
    private String content;
    private TextPosition position;

    public HtmlElement(ElementType type, TextPosition position) {
        this.type = type;
        this.position = position;
    }

    public HtmlElement(ElementType type, String content, TextPosition position) {
        this.type = type;
        this.content = content;
        this.position = position;
    }

    public TextPosition getPosition() {
        return position;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ElementType getType() {
        return type;
    }

    @Override
    public String toString() {
        if (type == ElementType.comment) {
            return "<!--" + content + "-->";
        } else if (type == ElementType.text) {
            return content;
        } else if (type == ElementType.doctype) {
            return "<!" + content + ">";
        } else {
            return "";
        }
    }

}
