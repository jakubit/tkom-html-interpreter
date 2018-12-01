import java.io.IOException;

public interface ISource {
    char nextChar() throws IOException;
    int open();
    void error();
}
