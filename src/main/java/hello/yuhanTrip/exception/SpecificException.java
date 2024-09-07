package hello.yuhanTrip.exception;

public class SpecificException extends RuntimeException{


    public SpecificException(String message) {
        super(message);
    }

    public SpecificException(String message, Throwable cause) {
        super(message, cause);
    }
}
