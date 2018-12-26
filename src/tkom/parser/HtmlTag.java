package tkom.parser;

import java.util.HashMap;
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
    private Map<String, String> attributes;


    // todo zrobic teleskopowy konstruktor

    public HtmlTag() {
        super(ElementType.tag);
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
        if(attributes == null)
            attributes = new HashMap<String, String>();

        attributes.put(name, value);
    }


    public void setName(String name) {
        this.name = name;
    }


    public void setType(TagType type) {
        this.type = type;
    }
}
