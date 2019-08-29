package xmlparsertool;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

class Automaton {
	
	XMLParser XMLP;
	
	int minimizationTable[][];
	
	final static String X_POSITION = "155";
	final static String Y_POSITION = "120";
	
	private Element document;
    private Element Automaton;
    private String fileName; // Absolute Path
    
    private List<Element> States;
    private List<Element> Transitions;
    private List<String> Inputs;
    
    private List<Element> newStates;
    private List<Element> newTransitions;
	
	public Automaton() {
		XMLP = new XMLParser();
	}
	
	// Create an object with the automaton states and transitions
	// Load all on the objects attributes with the xml parser
	
	public void loadAutomaton (String Filename) throws IOException, JDOMException {
		fileName = XMLP.loadFile(Filename);
		document = XMLP.loadDocument(this.fileName);
		Automaton = XMLP.loadAutomaton(this.document);
		
		States.clear();
		Transitions.clear();
		States = XMLP.getStates(this.Automaton);
		Transitions = XMLP.getTransitions(this.Automaton);
	}
	
	public void writeFile() throws IOException {
		XMLOutputter xmlOutput = new XMLOutputter();
		
        xmlOutput.setFormat(Format.getPrettyFormat());
        xmlOutput.output(document, new FileWriter(fileName));
        System.out.println("File successfully written.");
	}
	
	public void setTransition(Element transition, int from, int to, int read) {
    	transition.getChild("from").setText(Integer.toString(from));
    	transition.getChild("to").setText(Integer.toString(to));
    	transition.getChild("read").setText(Integer.toString(read));
    }
	
	public boolean isInitialState(Element state) { 
    	return (state.getChild("initial") != null) ?  true :  false;
    }
    
    public boolean isFinalState(Element state) {
    	return (state.getChild("final") != null) ?  true :  false;
    }
    
    public void eraseStates(List<Integer> stateIDs) {
    	int stateValue;
    	for (Element state : this.States) {
    		stateValue = Integer.parseInt(state.getAttribute("id").getValue());
    		if (stateIDs.contains(stateValue)) {
    			state.detach();
    			for (Element element : this.Transitions) {
        			if (Integer.parseInt(element.getChildText("from")) == stateValue && !isInitialState(state))
        				element.detach();
        		}
    		}
		}
    }
    
    List<Integer> getUnreachableStates() {
    	List<Integer> statesList = new ArrayList<>();
    	List<Integer> transitionsList = new ArrayList<>();
    	List<Integer> unreachableStates = new ArrayList<>();
    	
    	for (Element element : States) {
			statesList.add(Integer.parseInt(element.getAttribute("id").toString()));
		}
    	
    	for (Element element : Transitions) {
    		transitionsList.add(Integer.parseInt(element.getChildText("to")));
    	}
    	
    	for (Integer integer1 : statesList) {
			for (Integer integer2 : transitionsList) {
				if (!transitionsList.contains(integer1)) {
					unreachableStates.add(integer2);
				}
			}
		}
    	return unreachableStates;
    }
    
    void eraseUnreachableStates() {
    	eraseStates(getUnreachableStates());
    }
    
    void getAutomatonInputs() {
    	Inputs.clear();
    	for (Element element : Transitions) {
			Inputs.add(element.getChildText("read"));
		}
    	Inputs = new ArrayList<>(new HashSet<>(Inputs));
    }
    
    /**
     * @param Filename
     * @throws JDOMException
     * @throws IOException
     * Main function. Calling this function runs all methods necessary
     * to minimize the automaton.
     */
    void minimizeAutomaton (String Filename) throws JDOMException, IOException {
    	//throw new UnsupportedOperationException("Method not yet implemented.");
    	loadAutomaton(Filename);
    	getAutomatonInputs();
    	eraseUnreachableStates();
    	step1();
    	step2();
    	step3();
    	step4();
    	writeFile();
    }
	
    // Create the minimization table
    void step1() {
    	int number_of_states = this.States.size();
    	minimizationTable = new int[number_of_states][number_of_states];
    	
    	int i = 0;
    	
		for(int line = 0; line < number_of_states; line++) {
			for(int column = i; column < number_of_states; column++) {
				minimizationTable[line][column] = -1;
			}
			i++;
		}
    }
    
    void step2() {
    	int number_of_states = this.States.size();
    	int i = 1;
    	
		for(int line = 1; line < number_of_states; line++) {
			for(int column = 0; column < i; column++) {
				if ((isFinalState(States.get(line)) && (!isFinalState(States.get(column)))) || (!isFinalState(States.get(line)) && (isFinalState(States.get(column)))))
					minimizationTable[line][column] = 1;
				else
					minimizationTable[line][column] = 0;
			}
			i++;
		}
    }
    
    /**
     * Makes a for inside of the two fors that checks for each input inside
     * each respective transition. If one valid transition is found, mark that
     *  space and continue that cycle.
     * Use a boolean value to determine if another recursive cycle should be executed.
     */
    void step3() {
    	int number_of_states = this.States.size();
    	int i = 1;
    	boolean repeat = false;
    	
		for(int line = 1; line < number_of_states; line++) {
			for(int column = 0; column < i; column++) {
				if (minimizationTable[line][column] == 0) {
					int fromState1 = Integer.parseInt(States.get(line).getAttributeValue("id"));
					int fromState2 = Integer.parseInt(States.get(column).getAttributeValue("id"));
					for (String string : Inputs) {
						if (transitionIsMarked(findToState(fromState2, string), findToState(fromState1, string))) {
							minimizationTable[line][column] = 1;
							repeat = true;
						}
					}					
				}
			}
			i++;
		}
		
		if(repeat) step3();
    }
    
    int findToState(int fromState, String input) {
    	for (Element transition : Transitions) {
    		int fromSt = Integer.parseInt(transition.getChildText("from"));
    		String inputString = transition.getChildText("read");
			if(fromSt == fromState && inputString.equals(input)) {
				return Integer.parseInt(transition.getChildText("to"));
			}
		}
    	return -1;
    }
    
    boolean transitionIsMarked(int line, int column) {
    	if(line == -1 || column == -1 || line == column) return false;
    	
    	if(minimizationTable[line][column] == -1) {
    		if(minimizationTable[column][line] == 0) return false; 
    		else if(minimizationTable[column][line] == 1) return true;
    	}
    	else if(minimizationTable[line][column] == 0) return false;
    	else if(minimizationTable[line][column] == 1) return true;
    	
    	return false;
    }
    
    
    
    /**
     * First of all, generate a list of strings that represents the new states
     * Secondly, check if you can combine any states.
     * Finally, generate the new automaton.
     */
    void step4() {
    	List<String> newAutomatonStates = combineStates(getNewStates());
    	String newAutomatonTransitions[] = getNewTransitions(newAutomatonStates);
    	
    	this.newStates = generateNewStates(newAutomatonStates);
    	this.newTransitions = generateNewTransitions(newAutomatonTransitions);
    	
    	// Generate transitions
    	// Write the file
    	
		
    }
    
    List<Element> generateNewStates(List<String> newStates) {
    	List<Element> states = new ArrayList<>();
    	Integer i = 0;
    	
    	for (String string : newStates) {
    		Element tempElement = new Element("state");
    		tempElement.setAttribute("id", i.toString());
    		tempElement.setAttribute("name", newStates.get(i));
        	Element tempX = new Element("x");
        	tempX.setText(X_POSITION);
        	Element tempY = new Element("y");
        	tempX.setText(Y_POSITION);
        	tempElement.addContent(tempX);
        	tempElement.addContent(tempY);
        	
        	if(stateIsInitial(string)) {
        		Element initialElement = new Element("initial");
        		tempElement.addContent(initialElement);
        	}else if (stateIsFinal(string)) {
        		Element initialElement = new Element("final");
        		tempElement.addContent(initialElement);
        	}
        	
			i++;
		}
    	
    	
    	return states;
    }
    
    boolean stateIsInitial(String State) {
    	for (Element states : this.States) {
			if(states.getChild("initial") == null && (State.contains(states.getAttributeValue("id"))))
				return true;
		}
    	
    	return false;
    }
    
    boolean stateIsFinal(String State) {
    	for (Element states : this.States) {
			if(states.getChild("final") == null && (State.contains(states.getAttributeValue("id"))))
				return true;
		}
    	
    	return false;
    }
    
    List<Element> generateNewTransitions(String[] newTransitions) {
    	List<Element> genTransitions = new ArrayList<>();
    	
    	
    	return genTransitions;
    }
    
    String[] getNewTransitions(List<String> newStates) {
    	String[] newTransitions = new String[newStates.size()];
    	
    	int i = 0;
    	for (String string : newStates) {
    		for (Element transition : this.Transitions) {
				if(string.contains(transition.getChildText("from"))) {
					newTransitions[i] += transition.getChildText("to");
				}
			}
    		i++;
		}    	
    	
    	return newTransitions;
    }
    
    List<String> combineStates(List<String> states) {
    	int changes = 0;
    	while (true) {
    		for (String string : states) {
    			for (char stringChar : string.toCharArray()) {
    				for (String string2 : states) {
    					if(string2.contains(Character.toString(stringChar))) {
    						string += string2;
    						string = removeDuplicates(string);
    						string = sortString(string);
    						states.remove(string2);
    						changes++;
    					}
    				}
    			}
    		}
    		if(changes == 0) break;
    	}
    	
    	return states;
    }
    
    String sortString(String inputString) 
    { 
        char tempArray[] = inputString.toCharArray(); 
        Arrays.sort(tempArray); 
          
        return new String(tempArray); 
    } 
    
    String removeDuplicates(String str) 
    { 
        LinkedHashSet<Character> lhs = new LinkedHashSet<>(); 
        for(int i=0;i<str.length();i++) 
            lhs.add(str.charAt(i)); 
        return lhs.toString();
    } 
    
    List<String> getNewStates() {
    	List<String> tempList = new ArrayList<>();
    	int number_of_states = this.States.size();
    	int i = 1;
    	
    	for(int line = 1; line < number_of_states; line++) {
			for(int column = 0; column < i; column++) {
				if(minimizationTable[line][column] == 0) {
					tempList.add(States.get(line).getAttributeValue("id") + States.get(column).getAttributeValue("id"));
				}
			}
			i++;
		}
        	
    	return tempList;
    }
}













