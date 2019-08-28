import xmlparsertool.*;
import org.jdom2.Element;

class ParserTest {

    
    public static void main(String[] args) {
    	
    	XMLParser xmlP = new XMLParser();
    	
    	try {
    		xmlP.loadFile("/home/diego/Documents/Universidad/Teoría de la Computación/Project1/example1.jff");
    		
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
        
    }

}