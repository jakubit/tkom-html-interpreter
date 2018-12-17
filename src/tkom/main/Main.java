package tkom.main;

import tkom.lexer.Symbol;
import tkom.lexer.Lexer;
import tkom.source.Source;

public class Main {

    public static void main(String[] args) {

        Source s = new Source("resources/config.html");
        s.open();
        Lexer l = new Lexer(s);

        Symbol symbol;

        do {
            symbol = l.nextSymbol();
            System.out.println(symbol);
        } while (symbol.getType() != Symbol.SymbolType.EOF);
    }
}
