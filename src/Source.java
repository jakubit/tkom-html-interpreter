import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

public class Source implements ISource{

    private final String fileName;
    private BufferedReader reader;


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
    public char nextChar() throws IOException {
        return (char) reader.read();
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
