package xmlparsertool;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;


public class XMLParser {

    public XMLParser() {}
    
    
    
    public String loadFile(String Filename) throws IOException {
    	if (!Filename.endsWith(".jff")) throw new 
    		IllegalArgumentException("Wrong file extension"); 

        return Filename;
    }
    
    public Element loadDocument(String inputFile) throws JDOMException, IOException {
    	File inFile = new File(inputFile);
    	SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(inFile);
        return document.getRootElement();
    }
    
    Element loadAutomaton(Element document) { 
    	return document.getChild("automaton");
    }
    
    List<Element> getStates(Element automaton) {
    	return automaton.getChildren("state");
    }
    
    List<Element> getTransitions(Element automaton) {
    	return automaton.getChildren("transition");
    }
    
    void eraseElement(Element element) {
    	element.detach();
    }
    
    
    
    


}