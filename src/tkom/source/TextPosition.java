package tkom.source;

public class TextPosition {

    private long currentLineIndex;
    private long currentCharIndex;
    private long lastLineIndex;
    private long lastCharIndex;

    public TextPosition() {
        currentLineIndex = 1;
        currentCharIndex = 0;
        lastLineIndex = 0;
        lastCharIndex = -1;
    }

    public TextPosition(TextPosition other) {
        currentLineIndex = other.getCurrentLineIndex();
        currentCharIndex = other.getCurrentCharIndex();
        lastLineIndex = other.getLastLineIndex();
        lastCharIndex = other.getLastCharIndex();
    }

    public long getCurrentLineIndex() {
        return currentLineIndex;
    }

    public long getCurrentCharIndex() {
        return currentCharIndex;
    }

    public long getLastLineIndex() {
        return lastLineIndex;
    }

    public long getLastCharIndex() {
        return lastCharIndex;
    }

    public void incrementLineIndex() {
        lastLineIndex = currentLineIndex;
        currentLineIndex++;
    }

    public void incrementCharIndex() {
        lastCharIndex = currentCharIndex;
        currentCharIndex++;
    }

    public void decrementLineIndex() {
        currentLineIndex = lastLineIndex;
        lastLineIndex--;
    }

    public void decrementCharIndex() {
        currentCharIndex = lastCharIndex;
        lastCharIndex--;
    }

    public void setCurrentLineIndex(long currentLineIndex) {
        this.currentLineIndex = currentLineIndex;
    }

    public void setCurrentCharIndex(long currentCharIndex) {
        this.currentCharIndex = currentCharIndex;
    }

    @Override
    public String toString() {
        return "Line: " + currentLineIndex + " Char: " + currentCharIndex;
    }
}
