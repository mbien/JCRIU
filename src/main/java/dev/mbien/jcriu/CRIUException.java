package dev.mbien.jcriu;

/**
 * Represents errors returned from criu actions.
 * 
 * @author mbien
 */
public class CRIUException extends RuntimeException {

    public CRIUException(int error, String message) {
        super("criu error code: "+error+"; "+message);
    }
    
}
