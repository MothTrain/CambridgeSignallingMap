package aradnezami.cambridgesignallingmap;

/**
 * Thrown when there is a fault with a diagram json file that means it cannot be loaded.
 * Note that the absence of this exception being thrown does not necessarily mean that
 * the diagram was entirely correct
 */
public class DiagramFormatException extends RuntimeException {
    public DiagramFormatException(String message) {
        super(message);
    }

    public DiagramFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
