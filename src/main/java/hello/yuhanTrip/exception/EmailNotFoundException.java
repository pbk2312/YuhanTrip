package hello.yuhanTrip.exception;

public class EmailNotFoundException extends RuntimeException {

    public EmailNotFoundException() {
        super("이메일을 찾을 수 없습니다.");
    }

    public EmailNotFoundException(String message) {
        super(message);
    }

    public EmailNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailNotFoundException(Throwable cause) {
        super(cause);
    }
}