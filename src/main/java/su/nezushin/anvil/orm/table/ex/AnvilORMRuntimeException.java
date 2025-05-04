package su.nezushin.anvil.orm.table.ex;

public class AnvilORMRuntimeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 281549005514206064L;

	public AnvilORMRuntimeException() {
		super();
	}

	public AnvilORMRuntimeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AnvilORMRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public AnvilORMRuntimeException(String message) {
		super(message);
	}

	public AnvilORMRuntimeException(Throwable cause) {
		super(cause);
	}

}
