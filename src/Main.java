import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");


        try (FileReader reader = new FileReader("/Users/JakubPawlak/OneDrive/STUDNIA/Semestr 6/TKOM/Projekt/Etap 1/index.html")) {
            do {
                char c = (char) reader.read();
                System.out.println(c);
            } while (System.in.read() != 0 );


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
