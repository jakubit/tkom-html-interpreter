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
                textPosition.setCharIndex(0);
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

    public TextPosition getTextPosition() {
        return textPosition;
    }
}
