package hello.yuhanTrip.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileTest {
    public static void main(String[] args) {
        Path path = Paths.get("/Users/park/upload/testfile.txt");
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, "Test content".getBytes());
            System.out.println("파일이 성공적으로 생성되었습니다.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}