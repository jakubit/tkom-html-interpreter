package tkom.parser;

public class HtmlElement {
    public enum ElementType {
        tag,
        text,
        comment,
        doctype
    }

    private final ElementType type;
    private String content;

    public HtmlElement(ElementType type) {
        this.type = type;
    }

    public HtmlElement(ElementType type, String content) {
        this.type = type;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        String toReturn = "Type: " + type;
        if (type != ElementType.tag) {
            toReturn += "\tContent: " + content;
        }
        return toReturn;
    }

}
