package tkom.model.lexer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Util {
    private static final Map<String, Integer> htmlSpecialChars;

    static {
        htmlSpecialChars = new HashMap<String, Integer>();
        htmlSpecialChars.put("quot", 34);
        htmlSpecialChars.put("amp", 38);
        htmlSpecialChars.put("lt", 60);
        htmlSpecialChars.put("gt", 62);
        //htmlSpecialChars.put("", );
    }



}
