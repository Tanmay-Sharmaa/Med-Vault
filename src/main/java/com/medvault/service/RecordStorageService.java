package com.medvault.service;

import com.medvault.model.MedicalRecord;
import com.medvault.model.User;
import com.medvault.repository.MedicalRecordRepository;
import com.medvault.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordStorageService {

    private final MedicalRecordRepository repo;
    private final CryptoUtil crypto;

    @Value("${medvault.storage.dir}")
    private String storageDir;
    // return saved medical record
    public MedicalRecord saveEncrypted(MultipartFile file, User patient, User uploader, MedicalRecord.RecordType type) throws Exception {

        // file size 10MB hard limit
        if(file.getSize() > 10*1024*1024){
            throw new IllegalArgumentException("File is too large(max limit: 10MB)");
        }

        // give permissions for only known file types like PDF, JPEG for safety
        String ct = file.getContentType() == null ? "application/octet-stream" : file.getContentType();// ternary operator is used and the file type is stored in the variable ct
        java.util.Set<String> allowed = java.util.Set.of(
                "application/pdf", "image/png", "image/jpeg","text/plain", "image/jpg", "application/octet-stream"
        );
        if(!allowed.contains(ct)){
            throw new IllegalArgumentException("Unsupported file type" + ct);
        }

        // correct the file name remove any slashes and backslashes to resolve conflict with the os
        String original = (file.getOriginalFilename() == null ? "file": file.getOriginalFilename()).replaceAll("[\\\\/]+", "_");

        Files.createDirectories(Path.of(storageDir));
        String stored = UUID.randomUUID().toString() + ".bin";
        File target = Path.of(storageDir, stored).toFile();

        try (InputStream in = file.getInputStream();
             OutputStream out = new BufferedOutputStream(new FileOutputStream(target))) {
            crypto.encrypt(in, out);
        }

        MedicalRecord rec = MedicalRecord.builder()
                .patient(patient)
                .uploadedBy(uploader)
                .originalFilename(original)
                .storedFilename(stored)
                .contentType(ct)
                .sizeByte(file.getSize())
                .type(type)
                .build();

        return repo.save(rec);
    }

    public void streamDecrypted(MedicalRecord rec, OutputStream out) throws Exception {
        File f = Path.of(storageDir, rec.getStoredFilename()).toFile();
        try (InputStream in = new BufferedInputStream(new FileInputStream(f))) {
            crypto.decrypt(in, out);
        }
    }
    //  Helper: decrypts the encrypted .bin file to a temporary readable file
    public File decryptToTempFile(MedicalRecord record) throws Exception {
        // create a temporary PDF or TXT file (depending on your needs)
        Path temp = Files.createTempFile("decrypted-", ".pdf");

        // use your existing decrypt method to write decrypted bytes
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(temp.toFile()))) {
            streamDecrypted(record, out);
        }

        return temp.toFile();
    }

    public String getFilePath(String storedFilename) {
        return Path.of(storageDir, storedFilename).toAbsolutePath().toString();
    }

}
