package com.cloudvault.mainservice.storage;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
public interface FileStorageService {
    StoredFile store(UUID memoryId, MultipartFile file);
    void delete(String storageKey);
    record StoredFile(String key, String contentType, long size, String originalName) {}
}
