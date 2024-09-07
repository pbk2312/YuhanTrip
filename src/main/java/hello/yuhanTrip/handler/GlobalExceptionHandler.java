package hello.yuhanTrip.handler;


import hello.yuhanTrip.exception.InvalidHostException;
import hello.yuhanTrip.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public String handleUnauthorizedException(UnauthorizedException e) {
        // 로그를 남기거나 추가적인 처리
        return "redirect:/member/login?message=return";
    }

    @ExceptionHandler(InvalidHostException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN) // 403 Forbidden 상태 코드
    public String handleInvalidHostException(InvalidHostException e, HttpServletRequest request) {
        request.setAttribute("errorMessage", "호스트 권한이 필요합니다.");
        return "forward:/error"; // forward를 사용하여 속성 전달
    }
}
