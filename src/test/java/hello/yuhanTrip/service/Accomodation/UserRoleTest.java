package hello.yuhanTrip.service.Accomodation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@SpringBootTest
public class UserRoleTest {

    @Autowired
    private UserDetailsService userDetailsService;

    @Test
    public void testCheckUserRole() {
        String email = "pbk2312@naver.com"; // 테스트할 이메일 주소
        checkUserRole(email);
    }

    public void checkUserRole(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        userDetails.getAuthorities().forEach(auth -> {
            System.out.println(auth.getAuthority());
        });
    }
}
