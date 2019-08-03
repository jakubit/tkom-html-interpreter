package tkom.lexer;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import tkom.model.lexer.Lexer;
import tkom.model.lexer.Symbol;
import tkom.model.source.Source;

class LexerTest {

    @Test
    void nextSymbol() {
        Source source = new Source("tests/config.html");
        source.open();
        Lexer lexer = new Lexer(source);

        Symbol symbol = lexer.nextSymbol();
        Assert.assertEquals(Symbol.SymbolType.beginStartTag, symbol.getType());
        Assert.assertEquals("<", symbol.getValue());
        Assert.assertEquals(1, symbol.getPosition().getLineIndex());
        Assert.assertEquals(1, symbol.getPosition().getCharIndex());

        symbol = lexer.nextSymbol();
        Assert.assertEquals(Symbol.SymbolType.data, symbol.getType());
        Assert.assertEquals("cleaner", symbol.getValue());
        Assert.assertEquals(1, symbol.getPosition().getLineIndex());
        Assert.assertEquals(2, symbol.getPosition().getCharIndex());

        symbol = lexer.nextSymbol();
        Assert.assertEquals(Symbol.SymbolType.finishTag, symbol.getType());
        Assert.assertEquals(">", symbol.getValue());
        Assert.assertEquals(1, symbol.getPosition().getLineIndex());
        Assert.assertEquals(9, symbol.getPosition().getCharIndex());

        symbol = lexer.nextSymbol();
        Assert.assertEquals(Symbol.SymbolType.beginComment, symbol.getType());
        Assert.assertEquals("<!--", symbol.getValue());
        Assert.assertEquals(2, symbol.getPosition().getLineIndex());
        Assert.assertEquals(5, symbol.getPosition().getCharIndex());

        /*
        symbol = lexer.nextSymbol();
        Assert.assertEquals(Symbol.SymbolType.data, symbol.getType());

        symbol = lexer.nextSymbol();
        Assert.assertEquals(Symbol.SymbolType.data, symbol.getType());

        symbol = lexer.nextSymbol();
        Assert.assertEquals(Symbol.SymbolType.finishComment, symbol.getType());

        symbol = lexer.nextSymbol();
        Assert.assertEquals(Symbol.SymbolType.beginStartTag, symbol.getType());

        symbol = lexer.nextSymbol();
        Assert.assertEquals(Symbol.SymbolType.data, symbol.getType());

        symbol = lexer.nextSymbol();
        Assert.assertEquals(Symbol.SymbolType.finishSelfClosingTag, symbol.getType());

        symbol = lexer.nextSymbol();
        Assert.assertEquals(Symbol.SymbolType.beginStartTag, symbol.getType());

        symbol = lexer.nextSymbol();
        Assert.assertEquals(Symbol.SymbolType.data, symbol.getType());

        symbol = lexer.nextSymbol();
        Assert.assertEquals(Symbol.SymbolType.data, symbol.getType());

        symbol = lexer.nextSymbol();
        Assert.assertEquals(Symbol.SymbolType.attributiveAssign, symbol.getType());

        symbol = lexer.nextSymbol();
        Assert.assertEquals(Symbol.SymbolType.singleQuote, symbol.getType());

        symbol = lexer.nextSymbol();
        Assert.assertEquals(Symbol.SymbolType.data, symbol.getType());

        symbol = lexer.nextSymbol();
        Assert.assertEquals(Symbol.SymbolType.other, symbol.getType());

        symbol = lexer.nextSymbol();
        Assert.assertEquals(Symbol.SymbolType.data, symbol.getType());

        symbol = lexer.nextSymbol();
        Assert.assertEquals(Symbol.SymbolType.data, symbol.getType());

        symbol = lexer.nextSymbol();
        Assert.assertEquals(Symbol.SymbolType.singleQuote, symbol.getType());

        symbol = lexer.nextSymbol();
        Assert.assertEquals(Symbol.SymbolType.finishSelfClosingTag, symbol.getType());
        */
    }
}