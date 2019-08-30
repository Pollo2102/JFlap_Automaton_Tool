import xmlparsertool.*;
import org.jdom2.Element;

class ParserTest {

    
    public static void main(String[] args) {
    	
    	XMLParser xmlP = new XMLParser();
    	Automaton auto = new Automaton();
    	
    	try {
    		auto.minimizeAutomaton("/home/diego/Downloads/DFA_0.1.jff");    		
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
        
    }

}