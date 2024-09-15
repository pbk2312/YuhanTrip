package hello.yuhanTrip.exception;

public class KakaoApiException extends RuntimeException {

    // 기본 생성자
    public KakaoApiException() {
        super("카카오 API 호출 중 문제가 발생했습니다.");
    }

    // 메시지를 받는 생성자
    public KakaoApiException(String message) {
        super(message);
    }

    // 메시지와 원인을 받는 생성자
    public KakaoApiException(String message, Throwable cause) {
        super(message, cause);
    }

    // 원인만 받는 생성자
    public KakaoApiException(Throwable cause) {
        super(cause);
    }
}