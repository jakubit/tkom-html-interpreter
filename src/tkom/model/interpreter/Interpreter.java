package tkom.model.interpreter;
import org.w3c.dom.Attr;
import tkom.model.parser.Attribute;
import tkom.model.parser.HtmlElement;
import tkom.model.parser.HtmlTag;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static tkom.model.lexer.Symbol.SymbolType.doubleQuote;

public class Interpreter {
    private List<HtmlElement> toClean;
    private List<HtmlElement> rules;
    private List<HtmlElement> cleaned;
    private boolean clearEveryTagFromAttributes = false;
    private boolean clearStyleTag = false;
    private boolean clearScriptTag = false;

    private HashMap<String, List<String>> tagNameToAttributes;
    private List<String> globalAttributes;

    public Interpreter(List<HtmlElement> toClean, List<HtmlElement> rules) {
        this.toClean = toClean;
        this.rules = rules.stream()
                            .filter((element) -> element.getType() == HtmlElement.ElementType.tag)
                            .collect(Collectors.toList())
                            .stream()
                            .filter((element -> ((HtmlTag)element).getTagType() == HtmlTag.TagType.selfClosing))
                            .collect(Collectors.toList());

        cleaned = new LinkedList<>();
        globalAttributes = new LinkedList<>();
        tagNameToAttributes = new HashMap<>();
    }

    public void interpretRules() {
        interpretAttributeRules();
        interpretStyleRules();
        interpretScriptRules();
    }

    private void interpretStyleRules() {
        // Filter html tags with "style" name
        List<HtmlElement> styleRules = rules
                .stream()
                .filter((element -> ((HtmlTag)element).getName().toLowerCase().equals("style")))
                .collect(Collectors.toList());

        if (styleRules.size() > 0)
            clearStyleTag = true;
    }

    private void interpretScriptRules() {
        // Filter html tags with "script" name
        List<HtmlElement> scriptRules = rules
                .stream()
                .filter((element -> ((HtmlTag)element).getName().toLowerCase().equals("script")))
                .collect(Collectors.toList());

        if (scriptRules.size() > 0)
            clearScriptTag = true;
    }

    private void interpretAttributeRules() {
        // Filter html tags with "attr" name
        List<HtmlElement> attributeRules = rules
                .stream()
                .filter((element -> ((HtmlTag)element).getName().toLowerCase().equals("attr")))
                .collect(Collectors.toList());

        // Filter "attr" tags without attributes
        List<HtmlElement> attributeGlobal = attributeRules
                .stream()
                .filter((element -> (((HtmlTag)element).getAttributes().size() == 0)))
                .collect(Collectors.toList());


        if (attributeGlobal.size() > 0) {
            clearEveryTagFromAttributes = true;
        } else {
            // Create map with rules
            for (HtmlElement element : attributeRules) {
                List<Attribute> attributes = ((HtmlTag)element).getAttributes();
                List<String> tagNames = null;
                List<String> attrNames = null;

                for (Attribute attribute : attributes) {
                    if (attribute.getName().toLowerCase().equals("tag")) {
                        // Get all tag attributes
                        if (tagNames == null)
                            tagNames = attribute.getValues();
                        else
                            tagNames.addAll(attribute.getValues());
                    } else if (attribute.getName().toLowerCase().equals("name")) {
                        // Get all name attributes
                        if (attrNames == null)
                            attrNames = attribute.getValues();
                        else
                            attrNames.addAll(attribute.getValues());
                    }
                }

                if (tagNames == null) {
                    globalAttributes.addAll(attrNames);
                } else if (attrNames == null) {
                    for (String tagName : tagNames) {
                        if (tagNameToAttributes.containsKey(tagName)) {
                            if (tagNameToAttributes.get(tagName).contains("style")) {
                                tagNameToAttributes.get(tagName).clear();
                                tagNameToAttributes.get(tagName).add("style");
                            } else {
                                tagNameToAttributes.get(tagName).clear();
                            }
                        } else {
                            tagNameToAttributes.put(tagName, new LinkedList<>());
                        }
                    }
                } else {
                    // tagNames != null && attrNames != null
                    for (String tagName : tagNames) {
                        if (tagNameToAttributes.containsKey(tagName) && tagNameToAttributes.get(tagName).size() != 0) {
                            // Concatenate attributes
                            tagNameToAttributes.get(tagName).addAll(new LinkedList<>(attrNames));
                        } else {
                            // Put all attributes
                            tagNameToAttributes.put(tagName, new LinkedList<>(attrNames));
                        }
                    }
                }
            }
        }
    }

    public void clean() {
        if (clearEveryTagFromAttributes)
            System.out.println("Every html tag will be cleared from attributes!");

        if (clearScriptTag)
            System.out.println("Script tags will be cleared from content!");

        if (clearStyleTag)
            System.out.println("Style tags will be cleared from content!");

        if (globalAttributes.size() != 0)
            System.out.println("Every html tag will be cleared from these attributes: \n" + globalAttributes);

        if (tagNameToAttributes.size() != 0)
            System.out.println("Given tag will be cleared from attributes: \n" + tagNameToAttributes);

        for (HtmlElement element : toClean) {
            if (element.getType() == HtmlElement.ElementType.tag) {
                HtmlTag tag = (HtmlTag)element;
                HtmlTag newTag = new HtmlTag(tag);
                List<Attribute> newTagAttributes = newTag.getAttributes();

                if (clearEveryTagFromAttributes) {
                    for (Iterator<Attribute> it = newTagAttributes.iterator(); it.hasNext(); ) {
                        Attribute attr = it.next();
                        if (!attr.getName().toLowerCase().equals("style"))
                            it.remove();
                    }
                    cleaned.add(newTag);
                } else {
                    // global attributes
                    for (Iterator<String> it = globalAttributes.iterator(); it.hasNext(); ) {
                        String attr = it.next();
                        for (Iterator<Attribute> it2 = newTagAttributes.iterator(); it2.hasNext(); ) {
                            Attribute attr2 = it2.next();
                            if (attr2.getName().toLowerCase().equals(attr.toLowerCase()))
                                it2.remove();
                        }
                    }

                    // specific rule - from map
                    if (tagNameToAttributes.containsKey(newTag.getName())) {
                        List<String> attibutes = tagNameToAttributes.get(newTag.getName());
                        if (attibutes.size() == 0) {
                            newTag.getAttributes().clear();
                        } else {
                            for (String attr : attibutes) {
                                for (Iterator<Attribute> it = newTagAttributes.iterator(); it.hasNext();) {
                                    Attribute attr2 = it.next();
                                    if (attr2.getName().toLowerCase().equals(attr.toLowerCase()))
                                        it.remove();
                                }
                            }
                        }
                    }
                    cleaned.add(newTag);
                }
            } else if (element.getType() == HtmlElement.ElementType.scriptBody && !clearScriptTag) {
                cleaned.add(element);
            } else if (element.getType() == HtmlElement.ElementType.styleBody && !clearStyleTag) {
                cleaned.add(element);
            } else if (element.getType() != HtmlElement.ElementType.scriptBody && element.getType() != HtmlElement.ElementType.styleBody) {
                cleaned.add(element);
            }
        }

    }

    public void printRules() {
        rules.forEach(System.out::println);
    }

    public List<HtmlElement> getCleaned() {
        return cleaned;
    }

    public void printToClean() {
        toClean.forEach(System.out::println);
    }

    public void printCleaned() { cleaned.forEach(System.out::println);}
}
