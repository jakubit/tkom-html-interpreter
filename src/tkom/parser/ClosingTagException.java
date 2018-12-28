package tkom.parser;

import tkom.source.TextPosition;

public class ClosingTagException extends Exception {
    private HtmlTag openingTag;
    private HtmlTag closingTag;
    private TextPosition position;

    public ClosingTagException(HtmlTag openingTag, HtmlTag closingTag, TextPosition position) {
        this.openingTag = openingTag;
        this.closingTag = closingTag;
        this.position = position;
    }

    public void setOpeningTag(HtmlTag openingTag) {
        this.openingTag = openingTag;
    }

    public void setClosingTag(HtmlTag closingTag) {
        this.closingTag = closingTag;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append("SYNTAX ERROR! ");
        string.append(position);

        if (openingTag != null) {
            // opening tag never closed error
            string.append(" Opening tag: " + openingTag + " is not closed!");
        } else if(closingTag != null) {
            // closing tag never opened error
            string.append(" Closing tag: " + closingTag + " matches nothing!");
        }

        return string.toString();
    }
}
