import java.io.IOException;

public interface ISource {
    char nextChar();
    char getCurrentChar();
    void open();
    void error();
}
