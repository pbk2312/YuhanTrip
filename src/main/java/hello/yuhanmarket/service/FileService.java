package hello.yuhanmarket.service;

public interface FileService {


    public String uploadFile(String uploadPath, String originalFileName, byte[] fileData);


    public void deleteFile(String filePath);


}
