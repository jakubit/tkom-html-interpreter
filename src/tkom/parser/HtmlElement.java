package tkom.parser;

public class HtmlElement {
    public enum ElementType {
        tag,
        text,
        comment,
        doctype
    }

    private final ElementType type;

    public HtmlElement(ElementType type) {
        this.type = type;
    }

}
