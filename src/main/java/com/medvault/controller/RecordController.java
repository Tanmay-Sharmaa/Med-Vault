package com.medvault.controller;

import com.medvault.model.AuditLog;
import com.medvault.model.MedicalRecord;
import com.medvault.model.User;
import com.medvault.repository.MedicalRecordRepository;
import com.medvault.repository.UserRepository;
import com.medvault.repository.DoctorPatientRepository;
import com.medvault.service.AIService;
import com.medvault.service.AuditService;
import com.medvault.service.RecordStorageService;
import com.medvault.service.TikaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class RecordController {

    private final RecordStorageService storage;
    private final UserRepository userRepo;
    private final MedicalRecordRepository recordRepo;
    private final DoctorPatientRepository doctorPatientRepo;
    private final AuditService audit;
    private final MedicalRecordRepository medicalRecordRepository;
    @Autowired
    private TikaService tikaService;
    @Autowired
    private AIService aiService;


    @Value("${app.storage.root:}")
    private String storageRoot;


    // ---- UPLOAD ---------
    @PostMapping("/records/upload")
    public String handleUpload(@RequestParam("file") MultipartFile file,
                               @RequestParam("patientEmail") String patientEmail,
                               @RequestParam(value = "type", defaultValue = "GENERAL") MedicalRecord.RecordType type,
                               Authentication auth,
                               Model model){
        try {
            if (file == null || file.isEmpty()) {
                model.addAttribute("err", "please choose a file.");
                return "records/upload";
            }
            if (!StringUtils.hasText(patientEmail)) {
                model.addAttribute("err", "please enter the email.");
                return "records/upload"; // ✅ fixed typo
            }

            User uploader = userRepo.findByEmail(auth.getName()).orElseThrow();
            User patient = userRepo.findByEmail(patientEmail).orElseThrow();

            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isDoctor = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));

            if (!isAdmin && isDoctor && !doctorPatientRepo.existsByDoctorAndPatient(uploader, patient)) {
                model.addAttribute("err", "you are not assigned to this patient.");
                return "records/upload";
            }

            MedicalRecord saved = storage.saveEncrypted(file, patient, uploader, type);
            audit.log(uploader, saved, AuditLog.Action.UPLOAD);

            model.addAttribute("msg", "upload successfully!");
            return "records/upload";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("err", ex.getMessage());
            return "records/upload";
        } catch (Exception e) {
            model.addAttribute("err","upload failed: "+ e.getMessage());
            return "records/upload";
        }
    }

    // == DOWNLOAD ==
    @GetMapping("/records/{id}/download")
    public void download(@PathVariable Long id,
                         Authentication auth,
                         HttpServletResponse resp) throws Exception {
        Optional<MedicalRecord> opt = recordRepo.findById(id);
        if (opt.isEmpty()) {
            resp.setStatus(404);
            resp.getWriter().write("not found");
            return;
        }
        MedicalRecord rec = opt.get();

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isDoctor = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));
        User me = userRepo.findByEmail(auth.getName()).orElseThrow();

        boolean allow = false;
        if (isAdmin) {
            allow = true;
        } else if (isDoctor) {
            allow = doctorPatientRepo.existsByDoctorAndPatient(me, rec.getPatient());
        } else {
            allow = me.getId().equals(rec.getPatient().getId()); // patient owner
        }

        if (!allow) {
            resp.setStatus(403);
            resp.getWriter().write("Forbidden");
            return;
        }

        audit.log(me, rec, AuditLog.Action.DOWNLOAD);

        resp.setContentType(rec.getContentType() != null ? rec.getContentType() : "application/octet-stream");
        String filename = rec.getOriginalFilename() != null ? rec.getOriginalFilename() : "record";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        resp.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);

        try (OutputStream out = resp.getOutputStream()) {
            storage.streamDecrypted(rec, out);
        }
        resp.flushBuffer();
    }

    // ==== PATIENT: View my records ====
    @GetMapping("/patient/records")
    public String myRecords(Model model, Authentication auth){
        User user = userRepo.findByEmail(auth.getName()).orElseThrow();
        model.addAttribute("records", recordRepo.findByPatientOrderByUploadedAtDesc(user));
        return "records/patients-list";
    }

    // ==== DOCTOR: Upload form ====
    @GetMapping("/doctor/upload")
    public String uploadForm(){
        return "records/upload";
    }

    // ---- DOCTOR: Upload form for specific patient ----
    @GetMapping("/records/upload/{patientId}")
    public String uploadFormForPatient(@PathVariable Long patientId, Model model, Authentication auth) {
        User patient = userRepo.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        model.addAttribute("patientEmail", patient.getEmail());
        model.addAttribute("showGeneralForm", isAdmin);
        return "records/upload";
    }


    @PostMapping("/doctor/upload")
    public String doctorUpload(@RequestParam("patientEmail") String patientEmail,
                               @RequestParam("type") MedicalRecord.RecordType type,
                               @RequestParam("file") MultipartFile file,
                               Authentication auth, Model model) {
        try {
            User patient = userRepo.findByEmail(patientEmail).orElseThrow();
            User uploader = userRepo.findByEmail(auth.getName()).orElseThrow();
            MedicalRecord saved = storage.saveEncrypted(file, patient, uploader, type);
            audit.log(uploader, saved, AuditLog.Action.UPLOAD);
            model.addAttribute("msg", "Uploaded Successfully");
        } catch (Exception e) {
            model.addAttribute("err", "Upload Failed: " + e.getMessage());
        }
        return "records/upload";
    }
    @GetMapping("/records/{email}/bundle")
    public void downloadBundle(@PathVariable String email, HttpServletResponse response) throws Exception {
        List<MedicalRecord> files = recordRepo.findByPatientEmail(email);

        if (files.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No records found for this patient");
            return;
        }
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=records_bundle.zip");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (MedicalRecord rec : files) {
                zos.putNextEntry(new ZipEntry(rec.getOriginalFilename()));
                storage.streamDecrypted(rec, zos);
                zos.closeEntry();
            }
        }
    }
    @GetMapping("/records/{recordId}/summarize")
    @ResponseBody
    public String summarizeRecord(@PathVariable Long recordId) throws Exception {
        Optional<MedicalRecord> recordOpt = recordRepo.findById(recordId);
        if (recordOpt.isEmpty()) return "Record not found.";

        MedicalRecord record = recordOpt.get();

        //  1. Decrypt file to temp
        File decryptedFile = storage.decryptToTempFile(record);

        try {
            //  2. Extract text
            String text = tikaService.extractText(decryptedFile.toPath());

            //  3. Summarize using AI
            return aiService.summarizeText(text);

        } finally {
            //  4. Delete temp file for security
            boolean ignored = decryptedFile.delete();
        }
    }




}
