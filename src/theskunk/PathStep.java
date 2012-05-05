package theskunk;

import java.util.LinkedList;
import java.util.List;

public abstract class PathStep {
	private List<PathAssertion> _assertions = new LinkedList<PathAssertion>();
	
	protected void setAssertions(List<PathAssertion> assertions) {
		this._assertions = assertions;
	}
	
	protected void addAssertion(PathAssertion assertion) {
		this._assertions.add(assertion);
	}
	
	public List<PathAssertion> assertions() {
		return this._assertions;
	}
}
