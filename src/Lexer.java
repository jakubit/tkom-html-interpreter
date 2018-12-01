import java.io.IOException;

public class Lexer implements ILexer {

    private Source source;

    public Lexer(Source source) {
        this.source = source;
    }

    public Symbol nextSymbol() {
        // Pomiń białe znaki
        try {
            char sing = source.nextChar();
            System.out.println(sing);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Symbol.attributeName;
    }
}
