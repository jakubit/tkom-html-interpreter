import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Source implements ISource{

    private final String fileName;
    private BufferedReader reader;


    public Source(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public int open() {
        try {
            reader = new BufferedReader(new FileReader(fileName));
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public char nextChar() throws IOException {
        return (char) reader.read();
    }

    @Override
    public void error() {

    }


}
