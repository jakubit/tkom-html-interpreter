package tkom.main;

import tkom.lexer.Symbol;
import tkom.lexer.Lexer;
import tkom.parser.Parser;
import tkom.source.Source;

public class Main {

    public static void main(String[] args) {

        Source s = new Source("resources/config.html");
        s.open();
        Lexer l = new Lexer(s);
        Parser p = new Parser(l);


        try {
            p.parse();

        } catch (Exception e) {
            e.printStackTrace();
        }
        p.printStack();

        /*
        Symbol symbol;

        do {
            symbol = l.nextSymbol();
            System.out.println(symbol);
        } while (symbol.getType() != Symbol.SymbolType.EOF);
        */
    }
}
