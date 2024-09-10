package hello.yuhanTrip.service;

import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.domain.member.MemberRole;
import hello.yuhanTrip.domain.admin.RequestStatus;
import hello.yuhanTrip.domain.admin.RoleChangeRequest;
import hello.yuhanTrip.repository.RoleChangeRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class RoleChangeRequestService {

    private final RoleChangeRequestRepository roleChangeRequestRepository;

    @Value("${upload.dir}")  // application.properties에서 경로를 주입받음
    private String uploadDir;

    @Transactional
    public void requestRoleChange(Member member, MultipartFile file,String accommodationTitle,String accommodationDescription) {

        // 중복 요청 방지 로직 추가
        if (roleChangeRequestRepository.existsByMemberAndRequestedRoleAndStatus(
                member, MemberRole.ROLE_HOST, RequestStatus.PENDING)) {
            throw new IllegalStateException("이미 역할 변경 요청이 대기 중입니다.");
        }

        // 파일 저장 로직
        String filePath = saveFile(file);

        // 역할 변경 요청 생성 및 저장
        RoleChangeRequest roleChangeRequest = RoleChangeRequest.builder()
                .requestedRole(MemberRole.ROLE_HOST)
                .accommodationTitle(accommodationTitle)
                .accommodationDescription(accommodationDescription)
                .requestDate(LocalDateTime.now())
                .status(RequestStatus.PENDING)
                .member(member)
                .attachmentFilePath(filePath)
                .attachmentFileName(file.getOriginalFilename())
                .attachmentFileType(file.getContentType())
                .build();

        roleChangeRequestRepository.save(roleChangeRequest);
    }

    private String saveFile(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("첨부파일이 없습니다.");
            }

            // 파일 경로 생성
            Path directory = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!directory.toFile().exists()) {
                directory.toFile().mkdirs();  // 디렉토리가 없으면 생성
            }

            // 파일 저장
            Path targetPath = directory.resolve(file.getOriginalFilename());
            file.transferTo(targetPath.toFile());
            return targetPath.toString();
        } catch (IOException e) {
            log.error("파일 저장 중 오류 발생", e);
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.");
        }
    }

    public Page<RoleChangeRequest> getPendingRequests(Pageable pageable) {
        return roleChangeRequestRepository.findByStatus(RequestStatus.PENDING, pageable);
    }

    @Transactional
    public void approveRequest(Long requestId) {
        RoleChangeRequest request = roleChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다."));
        request.setStatus(RequestStatus.APPROVED);

        // 승급 로직 추가 (예: MemberRole 변경)
        Member member = request.getMember();
        member.setMemberRole(MemberRole.ROLE_HOST);

        roleChangeRequestRepository.save(request);
    }

    @Transactional
    public void rejectRequest(Long requestId, String rejectionReason) {
        RoleChangeRequest request = roleChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다."));
        request.setStatus(RequestStatus.REJECTED);
        request.setRejectionReason(rejectionReason);
        roleChangeRequestRepository.save(request);
    }

    public RoleChangeRequest getRequestById(Long id) {
        return roleChangeRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다."));
    }

    public  List<RoleChangeRequest> getRequestByMember(Member member) {
        List<RoleChangeRequest> roleChangeRequests = member.getRoleChangeRequests();

        return roleChangeRequests;
    }

}
