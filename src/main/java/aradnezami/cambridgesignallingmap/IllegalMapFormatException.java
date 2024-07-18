package aradnezami.cambridgesignallingmap;

/**
 * Thrown when a map cannot be loaded because of invalid formatting.
 * @see SClassHandler
 */
public class IllegalMapFormatException extends RuntimeException {
    
    public IllegalMapFormatException() {super();}
    public IllegalMapFormatException(String s) {super(s);}
}
