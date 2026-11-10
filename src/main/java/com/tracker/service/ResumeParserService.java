package com.tracker.service;

import com.tracker.dto.ResumeAnalysisResult;
import com.tracker.model.Resume;
import com.tracker.model.ResumeAnalysis;
import com.tracker.model.User;
import com.tracker.repository.ResumeRepository;
import com.tracker.util.PdfTextExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ResumeParserService {

    private static final Logger logger = LoggerFactory.getLogger(ResumeParserService.class);
    private static final String UPLOAD_DIR = "uploads/resumes";

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private ResumeAnalyzerService analyzerService;

    @Autowired
    private PdfTextExtractor pdfTextExtractor;

    @Autowired
    private UserService userService;

    @Transactional
    public Resume uploadAndAnalyze(MultipartFile file) throws IOException {
        return uploadAndAnalyze(file, userService.getCurrentUser());
    }

    @Transactional
    public Resume uploadAndAnalyze(MultipartFile file, User user) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty. Please select a valid PDF resume.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF resumes are supported. Please upload a .pdf file.");
        }

        // Create upload directory if it does not exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate safe unique filename
        String storedFileName = UUID.randomUUID() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
        Path filePath = uploadPath.resolve(storedFileName);

        // Copy file content
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Extract text with Apache PDFBox
        String extractedText = pdfTextExtractor.extractText(file);

        if (extractedText.isEmpty()) {
            logger.warn("Extracted text from PDF was empty for file: {}", originalFilename);
        }

        // Create and save Resume entity associated with the authenticated user
        Resume resume = new Resume(
            storedFileName,
            originalFilename,
            file.getContentType(),
            file.getSize(),
            filePath.toString(),
            extractedText
        );
        resume.setUser(user);
        resume = resumeRepository.save(resume);

        // Analyze and save analysis
        ResumeAnalysisResult analysisResult = analyzerService.analyzeResumeText(extractedText);
        ResumeAnalysis analysisEntity = analyzerService.saveAnalysis(resume, analysisResult);
        resume.setAnalysis(analysisEntity);

        return resume;
    }

    public Optional<Resume> getLatestResume() {
        return getLatestResume(userService.getCurrentUser());
    }

    public Optional<Resume> getLatestResume(User user) {
        return resumeRepository.findTopByUserOrderByUploadDateDesc(user);
    }

    public Optional<Resume> getResumeById(Long id) {
        return getResumeById(id, userService.getCurrentUser());
    }

    public Optional<Resume> getResumeById(Long id, User user) {
        return resumeRepository.findByIdAndUser(id, user);
    }

    public List<Resume> getAllResumes() {
        return getAllResumes(userService.getCurrentUser());
    }

    public List<Resume> getAllResumes(User user) {
        return resumeRepository.findAllByUserOrderByUploadDateDesc(user);
    }

    @Transactional
    public void deleteResume(Long id) {
        deleteResume(id, userService.getCurrentUser());
    }

    @Transactional
    public void deleteResume(Long id, User user) {
        Resume resume = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found or unauthorized for id: " + id));

        // Delete physical file if present
        try {
            if (resume.getFilePath() != null) {
                Path path = Paths.get(resume.getFilePath());
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            logger.error("Failed to delete physical file for resume: {}", resume.getFileName(), e);
        }

        resumeRepository.delete(resume);
    }
}
