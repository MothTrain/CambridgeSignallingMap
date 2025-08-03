package aradnezami.cambridgesignallingmap.NRFeed;

/**
 * Thrown when a map cannot be loaded because of invalid formatting.
 * @see SClassDecoder
 */
public class IllegalMapFormatException extends RuntimeException {
    
    public IllegalMapFormatException() {super();}
    public IllegalMapFormatException(String s) {super(s);}
}
