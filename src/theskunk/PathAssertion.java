package theskunk;

import apoSkunkman.ai.ApoSkunkmanAILevel;
import apoSkunkman.ai.ApoSkunkmanAIPlayer;

public abstract class PathAssertion {
	public abstract boolean evaulate(ApoSkunkmanAILevel level, ApoSkunkmanAIPlayer player);
}
