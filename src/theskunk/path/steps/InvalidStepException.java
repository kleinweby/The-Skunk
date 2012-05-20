package theskunk.path.steps;

public class InvalidStepException extends RuntimeException {
	private static final long serialVersionUID = -4855806365913167754L;

	public InvalidStepException(Step step, String reason) {
		super("Invalid step " + step + ": " + reason);
	}
}
