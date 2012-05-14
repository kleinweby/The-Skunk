package theskunk.path.assertions;

import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

public interface Assertion {

	public abstract boolean evaulate(ApoSkunkmanAILevel level,
			ApoSkunkmanAIPlayer player);

}