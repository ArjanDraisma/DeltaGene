package deltagene.output;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import deltagene.input.InputHandler;
import deltagene.input.UserInput;
import deltagene.input.data.HPODataHandler;

public class ComparisonResult extends AbstractResult {
	private static final long serialVersionUID = 1L;
	private HPODataHandler hpoDataHandler;
	private InputHandler inputHandler;

	public ComparisonResult(HPODataHandler hpoDataHandler, InputHandler inputHandler) {
		super(hpoDataHandler, inputHandler);
		this.hpoDataHandler = hpoDataHandler;
		this.inputHandler = inputHandler;
		generate();
	}
	
	private void generate() {
		HashMap<Integer, HashSet<String>> inputMap = new HashMap<Integer, HashSet<String>>();
		for (UserInput input : inputHandler.inputs) {
			int group = input.getInputGroup();
			if (!inputMap.containsKey(group) || inputMap.get(group) == null)
				inputMap.put(group, new HashSet<String>());
			input.getGeneSet(inputMap.get(group));
		}
		
		switch (inputHandler.getOperator()) {
			case InputHandler.DEFAULT:
				
			break;
		}
	}
}