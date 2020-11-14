package dev.mbien.jcriu;

/**
 * Represents errors returned from CRIU actions.
 * 
 * @author mbien
 */
public class CRIUException extends RuntimeException {

    public CRIUException(int error, String message) {
        super(error2String(error) + "; " + message);
    }
    
    // https://github.com/torvalds/linux/blob/master/include/uapi/asm-generic/errno.h
    // https://criu.org/C_API
    private static String error2String(int error) {
        return switch (error) {
            case  -22 -> "Unsupported request";                  // EINVAL Invalid argument
            case  -52 -> "RPC error";                            // EBADE Invalid exchange
            case  -70 -> "Unable to send/recv msg to/from CRIU"; // ECOMM Communication error on send
            case  -74 -> "Unexpected response from CRIU";        // EBADMSG Not a data message
            case -111 -> "Unable to connect to CRIU";            // ECONNREFUSED Connection refused
            default   -> "CRIU error code: "+error;
        };
    }
}
