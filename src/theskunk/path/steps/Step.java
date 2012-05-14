package theskunk.path.steps;

import java.util.LinkedList;
import java.util.List;

import theskunk.path.assertions.Assertable;
import theskunk.path.assertions.Assertion;

public abstract class Step implements Assertable {
	private List<Assertion> _assertions = new LinkedList<Assertion>();
	
	protected void setAssertions(List<Assertion> assertions) {
		this._assertions = assertions;
	}
	@Override
	public void addAssertion(Assertion assertion) {
		this._assertions.add(assertion);
	}

	@Override
	public List<Assertion> assertions() {
		return this._assertions;
	}
	
	@Override
	public String toString() {
		return "Step()";
	}
}
