import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

public class Source implements ISource{

    private final String fileName;
    private BufferedReader reader;
    private char currentChar;


    public Source(String fileName) {
        this.fileName = fileName;
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
            reader.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
