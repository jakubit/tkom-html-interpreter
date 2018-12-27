package tkom.parser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HtmlTag extends HtmlElement {

    public enum TagType {
        opening,        // <tag>
        closing,        // </tag>
        selfClosing,    // <tag/>
        doctype,        // <!tag>

    }

    private String name;
    private TagType type;
    private Map<String, List<String>> attributes;


    // todo zrobic teleskopowy konstruktor

    public HtmlTag() {
        super(ElementType.tag);
        attributes = new HashMap<String, List<String>>();
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

    public void addAtribute(String name, String value) {
        List<String> values = attributes.get(name);
        if (values == null) {
            values = new LinkedList<>();
            attributes.put(name, values);
        }
        values.add(value);
    }


    public void setName(String name) {
        this.name = name;
    }


    public void setType(TagType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return super.toString() + "\tName: " + name + "\tTag-Type: " + type + "\tAttributes: " + attributes.toString();
    }
}
