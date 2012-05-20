package theskunk.path.assertions;

import java.util.List;


public interface Assertable {

	public abstract void addAssertion(Assertion assertion);

	public abstract List<Assertion> assertions();

}