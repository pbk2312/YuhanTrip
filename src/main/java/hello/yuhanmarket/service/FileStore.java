package hello.yuhanmarket.service;


import hello.yuhanmarket.domain.shopping.AttachImage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class FileStore {

    @Value("${file.dir}/")
    private String fileDirPath;

    public List<AttachImage> storeFiles(List<MultipartFile> multipartFiles) throws IOException {
        List<AttachImage> attachments = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            if (!multipartFile.isEmpty()) {
                attachments.add(storeFile(multipartFile));
            }
        }

        return attachments;
    }

    public AttachImage storeFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String storeFilename = createStoreFilename(originalFilename);
        multipartFile.transferTo(new File(createImagePath(storeFilename)));

        return AttachImage.builder()
                .originFilename(originalFilename)
                .storeFilename(storeFilename)
                .build();

    }

    public String createImagePath(String storeFilename) {
        return fileDirPath + "images/" + storeFilename;
    }


    private String createStoreFilename(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String ext = extractExt(originalFilename);
        String storeFilename = uuid + ext;

        return storeFilename;
    }

    private String extractExt(String originalFilename) {
        int idx = originalFilename.lastIndexOf(".");
        String ext = originalFilename.substring(idx);
        return ext;
    }
}
