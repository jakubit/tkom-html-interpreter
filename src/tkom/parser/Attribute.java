package tkom.parser;

import java.util.LinkedList;
import java.util.List;

public class Attribute {
    public enum AttributeType {
        unquoted,
        singleQuoted,
        doubleQuoted,
        noValue
    }

    private String name;
    private AttributeType type;
    private List<String> values;

    public Attribute(String name, AttributeType type) {
        this.name = name;
        this.type = type;
    }

    public void addValue(String value) {
        if (values == null)
            values = new LinkedList<>();

        if (type != AttributeType.unquoted || values.size() == 0)
            values.add(value);

        /*// todo popraw to na jakis ldany customowy exception, ale to nie jest chyba blad
        if (type == AttributeType.unquoted && values.size() > 0) {
            System.out.println("ERROR: In Attribute.addValue!");
            //throw new Exception("Attribute error! You can't use multiple unquoted values as attribute value!");
        } else {
            values.add(value);
        }*/
    }

    public String getName() {
        return name;
    }

    public AttributeType getType() {
        return type;
    }

    public List<String> getValues() {
        return values;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder(name);
        if (type == AttributeType.singleQuoted) {
            string.append("='");
            for (String v : values) {
                string.append(v);
                string.append(" ");
            }
            string.deleteCharAt(string.lastIndexOf(" "));
            string.append("'");
        } else if (type == AttributeType.doubleQuoted) {
            string.append("=\"");
            for (String v : values) {
                string.append(v);
                string.append(" ");
            }
            string.deleteCharAt(string.lastIndexOf(" "));
            string.append("\"");
        } else if (type == AttributeType.unquoted) {
            string.append("=" + values.get(0));
        }

        return string.toString();
    }
}
