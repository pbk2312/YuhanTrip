package hello.yuhanTrip.domain.admin;

import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.domain.member.MemberRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private MemberRole requestedRole;

    private LocalDateTime requestDate;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    // 서버에 저장된 파일의 경로를 저장할 필드 추가
    private String attachmentFilePath;

    private String attachmentFileName;  // 파일 이름 저장
    private String attachmentFileType;  // 파일 타입(MIME 타입) 저장

    @ManyToOne
    @JoinColumn(name = "approver_id")
    private Member approver;  // 요청을 승인한 관리자

    private LocalDateTime approvalDate;  // 요청 승인 일자

    private String rejectionReason;  // 요청 거절 사유

    private String accommodationDescription;  // 숙소 설명 (수정된 필드명)

    private String accommodationTitle; // 숙소 이름
}
