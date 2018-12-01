import java.io.IOException;

public interface ISource {
    char nextChar() throws IOException;
    void open();
    void error();
}
