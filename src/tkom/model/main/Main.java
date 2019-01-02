package tkom.model.main;

import tkom.model.lexer.Lexer;
import tkom.model.parser.Parser;
import tkom.model.source.Source;

public class Main {

    public static void main(String[] args) {

        boolean strict = false;

        Source s = new Source("resources/config.html");
        s.open();
        Lexer l = new Lexer(s);
        Parser p = new Parser(l, strict);


        try {
            p.parse();

        } catch (Exception e) {
            e.printStackTrace();
        }
        p.printStack();


        /*Symbol symbol;

        do {
            symbol = l.nextSymbol();
            System.out.println(symbol);
        } while (symbol.getType() != Symbol.SymbolType.EOF);*/

    }
}
