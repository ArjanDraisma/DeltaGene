package deltagene.output;

import deltagene.input.InputHandler;
import deltagene.input.data.HPODataHandler;

public class ListResult extends AbstractResult {
	private static final long serialVersionUID = 1L;

	public ListResult(HPODataHandler hpoDataHandler, InputHandler inputHandler) {
		super(hpoDataHandler, inputHandler);
		generate();
	}
	
	private void generate() {
		
	}
}