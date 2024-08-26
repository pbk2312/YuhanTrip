package hello.yuhanTrip.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class ImageService {

    @Value("${upload.dir}")
    private String uploadDir;

    // 단일 이미지 저장
    public String saveImage(MultipartFile imageFile) throws IOException {
        if (imageFile.isEmpty()) {
            throw new IllegalArgumentException("Empty image file provided");
        }

        Path uploadPath = Paths.get(uploadDir, imageFile.getOriginalFilename());
        Files.copy(imageFile.getInputStream(), uploadPath);

        log.info("Image saved to {}", uploadPath.toString());
        return uploadPath.toString();
    }

    // 다중 이미지 저장
    public List<String> saveImages(List<MultipartFile> imageFiles) throws IOException {
        List<String> imagePaths = new ArrayList<>();

        for (MultipartFile imageFile : imageFiles) {
            if (!imageFile.isEmpty()) {
                String imagePath = saveImage(imageFile);
                imagePaths.add(imagePath);
            }
        }

        return imagePaths;
    }
}
