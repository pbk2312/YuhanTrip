package hello.yuhanmarket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class FileServiceImpl implements FileService {


    @Override
    public String uploadFile(String uploadPath, String originalFileName, byte[] fileData) {
        try {
            UUID uuid = UUID.randomUUID();
            String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String savedFileName = uuid.toString() + extension;
            String fileUploadFullUrl = uploadPath + "/" + savedFileName;

            try (FileOutputStream fos = new FileOutputStream(fileUploadFullUrl)) {
                fos.write(fileData);
                log.info("파일을 업로드하였습니다. 저장된 경로: {}", fileUploadFullUrl);
            }

            return savedFileName;
        } catch (IOException e) {
            log.error("파일 업로드 중 오류가 발생하였습니다.", e);
            throw new RuntimeException("파일 업로드 중 오류가 발생하였습니다.", e);
        }
    }
    @Override
    public void deleteFile(String filePath) {
        File deleteFile = new File(filePath);

        if (deleteFile.exists()) {
            if (deleteFile.delete()) {
                log.info("파일을 삭제하였습니다. 삭제된 파일 경로: {}", filePath);
            } else {
                log.warn("파일을 삭제하지 못했습니다. 삭제 대상 파일 경로: {}", filePath);
            }
        } else {
            log.warn("삭제할 파일이 존재하지 않습니다. 파일 경로: {}", filePath);
        }
    }
}
