package tkom.model.source;

public interface ISource {
    char nextChar();
    char getCurrentChar();
    void open();
    void error();
}
