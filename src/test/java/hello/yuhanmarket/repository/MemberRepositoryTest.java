//package hello.yuhanmarket.repository;
//
//import hello.yuhanmarket.domain.Member;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//@SpringBootTest
//class MemberRepositoryTest {
//
//    private final MemberRepository memberRepository;
//
//    @Autowired
//    public MemberRepositoryTest(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }
//
//    @Test
//    @Transactional
//    @Rollback
//    void findByEmail() {
//        // Given
//        String email = "padasda@gmail.com";
//        Member member = new Member(email, "2312");
//        memberRepository.save(member);
//
//        // When
//        Optional<Member> findByEmail = memberRepository.findByEmail(email);
//
//        // Then
//        assertTrue(findByEmail.isPresent());
//        assertThat(findByEmail.get().getEmail()).isEqualTo(member.getEmail());
//    }
//
//    @Test
//    @Transactional
//    @Rollback
//    void existsByEmail() {
//        // Given
//        String email = "padasda@gmail.com";
//        Member member = new Member(email, "2312");
//        memberRepository.save(member);
//
//        // When
//        boolean exists = memberRepository.existsByEmail(email);
//
//        // Then
//        assertTrue(exists);
//    }
//}
