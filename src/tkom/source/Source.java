package tkom.source;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Source implements ISource {

    private final String fileName;
    private BufferedReader reader;
    private char currentChar;
    private TextPosition textPosition;


    public Source(String fileName) {
        this.fileName = fileName;
        textPosition = new TextPosition();
    }

    @Override
    public void open() {
        try {
            reader = new BufferedReader(new FileReader(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public char nextChar() {
        try {
            currentChar = (char)reader.read();
            if(currentChar == '\n') {
                // New line
                textPosition.incrementLineIndex();
                textPosition.setCurrentCharIndex(0);
            } else {
                // Same line
                textPosition.incrementCharIndex();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return currentChar;
    }

    @Override
    public char getCurrentChar() {
        return currentChar;
    }

    @Override
    public void error() {

    }

    public void mark() {
        try {
            reader.mark(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void back() {
        try {
            if(currentChar == '\n') {
                // Previous line
                textPosition.decrementLineIndex();
                textPosition.decrementCharIndex();
            } else {
                // Same line
                textPosition.decrementCharIndex();
            }
            reader.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getLineIndex() {
        return textPosition.getCurrentLineIndex();
    }

    public long getCharIndex() {
        return textPosition.getCurrentCharIndex();
    }

    public TextPosition getTextPosition() {
        return textPosition;
    }
}
