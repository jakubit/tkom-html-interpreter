public class Main {

    public static void main(String[] args) {

        Source s = new Source("src/config.html");
        Lexer l = new Lexer(s);

        s.open();

        Symbol symbol;

        do {
            symbol = l.nextSymbol();
            System.out.println(symbol);
        } while (symbol.getType() != Symbol.SymbolType.EOF);
    }
}
