package tkom.model.source;

public class TextPosition {

    private long lineIndex;
    private long charIndex;


    public TextPosition() {
        lineIndex = 1;
        charIndex = 0;
    }

    public TextPosition(TextPosition other) {
        lineIndex = other.getLineIndex();
        charIndex = other.getCharIndex();
    }

    public long getLineIndex() {
        return lineIndex;
    }

    public long getCharIndex() {
        return charIndex;
    }

    public void incrementLineIndex() {
        lineIndex++;
    }

    public void incrementCharIndex() {
        charIndex++;
    }

    public void setCharIndex(long charIndex) {
        this.charIndex = charIndex;
    }

    @Override
    public String toString() {
        return "Line: " + lineIndex + " Char: " + charIndex;
    }
}
