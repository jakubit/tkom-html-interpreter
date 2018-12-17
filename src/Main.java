public class Main {

    public static void main(String[] args) {

        Source s = new Source("src/config.html");
        s.open();
        Lexer l = new Lexer(s);



        Symbol symbol;

        do {
            symbol = l.nextSymbol();
            System.out.println(symbol);
        } while (symbol.getType() != Symbol.SymbolType.EOF);
    }
}
