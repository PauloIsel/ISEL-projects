package pt.isel.mpd;

import org.junit.jupiter.api.Test;
import pt.isel.mpd.spreadsheet0.parser.Lex;
import pt.isel.mpd.spreadsheet0.parser.Lex.TokType;
import pt.isel.mpd.spreadsheet0.parser.Lex.Token;

public class ParserTests {
    
    @Test
    public void testLexWithExpressions() {
        String exprText = "23 + 3 * (40+80)";
        
        Lex lex = new Lex();
        
        lex.start(exprText);
        
        Token tok;
        do {
            tok = lex.next();
            tok.show();
        }
        while(tok.getType() != TokType.END);
        
    }
    
   
}
