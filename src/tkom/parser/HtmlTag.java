package tkom.parser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HtmlTag extends HtmlElement {

    public enum TagType {
        opening,        // <tag>
        closing,        // </tag>
        selfClosing,    // <tag/>
    }

    private String name;
    private TagType type;
    private List<Attribute> attributes;


    // todo zrobic teleskopowy konstruktor

    public HtmlTag() {
        super(ElementType.tag);
        attributes = new LinkedList<>();
    }

    public HtmlTag(String name) {
        super(ElementType.tag);
        this.name = name;
    }

    public HtmlTag(TagType type, String name) {
        super(ElementType.tag);
        this.type = type;
        this.name = name;
    }


    public void addAttribute(String name, String value, Attribute.AttributeType type) throws Exception {
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


    public void setName(String name) {
        this.name = name;
    }


    public void setType(TagType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        String string = super.toString();
        if (type == TagType.opening) {
            // <tag attributes>
            string += "<" + name + " " + attributesToString() + ">";
        } else if (type == TagType.closing) {
            // </tag>
            string += "</" + name + ">";
        } else {
            // <tag attributes />
            string += "<" + name + " " + attributesToString() + "/>";
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
