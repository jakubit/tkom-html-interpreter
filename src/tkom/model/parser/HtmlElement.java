package tkom.model.parser;

import tkom.model.source.TextPosition;

public class HtmlElement {
    public enum ElementType {
        tag,
        text,
        comment,
        doctype,
        scriptBody,
        styleBody
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
            if (content.equals(""))
                return "<!-->";
            else
                return "<!--" + content + "-->";
        } else if (type == ElementType.text || type == ElementType.scriptBody || type == ElementType.styleBody) {
            return content;
        } else if (type == ElementType.doctype) {
            return "<!" + content + ">";
        } else {
            return "";
        }
    }

}
