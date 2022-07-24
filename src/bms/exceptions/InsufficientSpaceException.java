package bms.exceptions;

/**
 * Exception thrown when a room is added to a floor where there is insufficient
 * space available to be able to add the room.
 *
 * @ass1
 */
public class InsufficientSpaceException extends Exception {
    /**
     * Constructs a normal InsufficientSpaceException with no error message or
     * cause.
     *
     * @see Exception#Exception()
     * @ass1
     */
    public InsufficientSpaceException() {
        super();
    }

    /**
     * Constructs a InsufficientSpaceException that contains a helpful message
     * detailing why the exception occurred.
     * <p>
     * <b>Note:</b> implementing this constructor is <b>optional</b>.
     * It has only been included in the Javadoc to ensure your code will compile
     * if you give your exception a message when throwing it. This practice
     * can be useful for debugging purposes.
     * <p>
     * <b>Important:</b> do not write JUnit tests that expect a valid
     * implementation of the assignment to have a certain error message, as the
     * official solution will use different messages to those you are expecting,
     * if any at all.
     *
     * @param message detail message
     * @see Exception#Exception(String)
     * @ass1
     */
    public InsufficientSpaceException(String message) {
        super(message);
    }
}

