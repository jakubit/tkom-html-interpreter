package tkom.model.main;

import tkom.model.interpreter.Interpreter;
import tkom.model.lexer.Lexer;
import tkom.model.parser.HtmlElement;
import tkom.model.parser.Parser;
import tkom.model.source.Source;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Main2 {
    public static void main(String[] args) {
        boolean strict = false;

        Source s = new Source("resources/config.html");
        s.open();
        Lexer l = new Lexer(s);
        Parser p = new Parser(l, strict);

        System.out.println("Config file loaded successfully.\n");


        try {
            // Get rules
            p.parse();
            System.out.println("Website parsed successfully.\n");
            List<HtmlElement> rules = new LinkedList<>(p.getHtmlElements());

            // Get html to clean
            s = new Source("resources/in.html");
            s.open();
            l.setSource(s);
            p.reset();
            p.parse();
            List<HtmlElement> toClean = p.getHtmlElements();

            // Interpret rules and apply them
            Interpreter i = new Interpreter(toClean, rules);
            i.interpretRules();
            i.clean();
            System.out.println("Website cleaned successfully.\n");

            BufferedWriter writer = new BufferedWriter(new FileWriter("resources/out.html"));
            List<HtmlElement> cleaned = i.getCleaned();
            for (HtmlElement element : cleaned) {
                writer.write(element.toString() + "\n");
            }

            writer.close();

            System.out.println("Cleaned website saved as out.html.\n");


        } catch (MalformedURLException e) {
            System.out.println("Wrong url! Remember to prepend https://!\n");
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
