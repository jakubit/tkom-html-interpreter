package tkom.source;

public interface ISource {
    char nextChar();
    char getCurrentChar();
    void open();
    void error();
}
