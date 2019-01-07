package tkom.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tkom.model.lexer.Lexer;
import tkom.model.parser.HtmlElement;
import tkom.model.parser.Parser;
import tkom.model.source.Source;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class Controller {
    @FXML
    private TextField urlInput;

    @FXML
    private Button getButton, parseButton;

    @FXML
    private TextArea rawWebsite, parsedWebsite;

    @FXML
    private CheckBox strictCheckbox;

    private BufferedReader reader;
    private String rawText;
    private boolean strictMode;

    @FXML
    private void getButtonClicked() {
        try {
            URL url = new URL(urlInput.getText());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            BufferedReader in = reader;
            String inputLine;
            StringBuilder website = new StringBuilder();
            StringBuilder websiteRaw = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                website.append(inputLine);
                websiteRaw.append(inputLine);
                website.append("\n");
            }

            rawText = websiteRaw.toString();
            rawWebsite.setText(website.toString());
        } catch (MalformedURLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Wrong url");
            alert.setHeaderText("You entered wrong url.");
            alert.setContentText("Remember to prepend https://!");
            alert.showAndWait();
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void parseButtonClicked() {
        Reader inputString = new StringReader(rawWebsite.getText());
        BufferedReader reader = new BufferedReader(inputString);

        Source source = new Source(reader);
        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer, strictMode);

        try {
            //System.out.println("Parsing...");
            parser.parse();
            //parser.printStack();

            StringBuilder parsed = new StringBuilder();
            for(HtmlElement element : parser.getHtmlElements()) {
                parsed.append(element);
                parsed.append("\n");
            }
            parsedWebsite.setText(parsed.toString());

        } catch (Exception e) {
            parsedWebsite.setText(e.toString());
        }
    }

    @FXML
    private void strictCheckboxClicked() {
        strictMode = strictCheckbox.isSelected();
    }
}
