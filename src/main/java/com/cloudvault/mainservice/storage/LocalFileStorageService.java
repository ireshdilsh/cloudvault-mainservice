package com.cloudvault.mainservice.storage;

import com.cloudvault.mainservice.exception.StorageException;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalFileStorageService implements FileStorageService {
    private final Path root;
    private final long maxBytes;

    public LocalFileStorageService(@Value("${app.storage.path}") String path,
            @Value("${app.storage.max-image-bytes}") long maxBytes) {
        this.root = Paths.get(path).toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
    }

    public StoredFile store(UUID memoryId, MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new StorageException("Image file must not be empty");
        if (file.getSize() > maxBytes)
            throw new StorageException("Image exceeds configured size limit");
        String detected = detect(file);
        String key = memoryId + "/" + UUID.randomUUID() + (detected.equals("image/png") ? ".png" : ".jpg");
        try {
            Path destination = root.resolve(key).normalize();
            if (!destination.startsWith(root))
                throw new StorageException("Invalid storage path");
            Files.createDirectories(destination.getParent());
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            return new StoredFile(key, detected, file.getSize(), safeName(file.getOriginalFilename()));
        } catch (IOException e) {
            throw new StorageException("Could not store image");
        }
    }

    public void delete(String key) {
        try {
            Path file = root.resolve(key).normalize();
            if (!file.startsWith(root))
                throw new StorageException("Invalid storage path");
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new StorageException("Could not delete image");
        }
    }

    private String detect(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            byte[] h = in.readNBytes(12);
            if (h.length >= 3 && (h[0] & 255) == 255 && (h[1] & 255) == 216 && (h[2] & 255) == 255)
                return "image/jpeg";
            if (h.length >= 8 && h[0] == (byte) 137 && h[1] == 80 && h[2] == 78 && h[3] == 71 && h[4] == 13
                    && h[5] == 10 && h[6] == 26 && h[7] == 10)
                return "image/png";
        } catch (IOException e) {
            throw new StorageException("Could not inspect image");
        }
        throw new StorageException("Only JPEG and PNG image files are allowed");
    }

    private String safeName(String name) {
        if (name == null)
            return "upload";
        return Paths.get(name).getFileName().toString().replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
