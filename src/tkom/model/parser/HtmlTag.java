package tkom.model.parser;

import tkom.model.source.TextPosition;

import java.util.LinkedList;
import java.util.List;

public class HtmlTag extends HtmlElement {

    public enum TagType {
        opening,        // <tag>
        closing,        // </tag>
        selfClosing,    // <tag/>
    }

    private String name;
    private TagType type;
    private List<Attribute> attributes;
    private boolean closed;


    // todo zrobic teleskopowy konstruktor

    public HtmlTag(TextPosition position) {
        super(ElementType.tag, position);
        attributes = new LinkedList<>();
        closed = false;
    }

    public HtmlTag(HtmlTag other) {
        super(ElementType.tag, other.getPosition());
        attributes = new LinkedList<>(other.getAttributes());
        name = other.getName();
        type = other.getTagType();
        closed = other.isClosed();
    }

    public HtmlTag(String name, TagType type, TextPosition position) {
        super(ElementType.tag, position);
        attributes = new LinkedList<>();
        closed = false;

        this.name = name;
        this.type = type;
    }

    public void addAttribute(String name, String value, Attribute.AttributeType type) {
        Attribute attribute = null;
        for(Attribute a : attributes) {
            if (a.getName().equals(name) && a.getType() == type) {
                attribute = a;
                break;
            }
        }

        if (attribute == null) {
            attribute = new Attribute(name, type);
            attributes.add(attribute);
        }

        attribute.addValue(value);
    }

    public List<Attribute> getAttributes() {return attributes;}

    public String getName() {
        return name;
    }

    public TagType getTagType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public void setType(TagType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        String string = super.toString();
        if (type == TagType.opening) {
            // <tag attributes>
            string += "<" + name;
            if (attributes.size() > 0) {
                string += " " + attributesToString();
            }
            string += ">";
        } else if (type == TagType.closing) {
            // </tag>
            string += "</" + name + ">";
        } else {
            // <tag attributes />
            string += "<" + name;
            if (attributes.size() > 0) {
                string += " " + attributesToString();
            }
            string += "/>";
        }

        return string;
    }

    private String attributesToString() {

        StringBuilder string = new StringBuilder("");
        for(Attribute a : attributes) {
            string.append(a);
            string.append(" ");
        }

        int index = string.lastIndexOf(" ");
        if (index > 0)
            string.deleteCharAt(index);

        return string.toString();
    }
}