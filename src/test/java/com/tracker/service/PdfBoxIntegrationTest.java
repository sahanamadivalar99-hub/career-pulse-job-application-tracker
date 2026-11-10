package com.tracker.service;

import com.tracker.dto.ResumeAnalysisResult;
import com.tracker.util.PdfTextExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PdfBoxIntegrationTest {

    @Autowired
    private PdfTextExtractor pdfTextExtractor;

    @Autowired
    private ResumeAnalyzerService analyzerService;

    @Test
    public void testGenerateAndParsePdfResume() throws Exception {
        // Create an authentic PDF in memory using Apache PDFBox 3.x
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.setLeading(18.0f);
                contentStream.newLineAtOffset(50, 720);

                contentStream.showText("ALEX JOHNSON - SOFTWARE ENGINEER");
                contentStream.newLine();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                contentStream.showText("Email: alex.johnson@example.com | Phone: 415-555-8921");
                contentStream.newLine();
                contentStream.showText("GitHub: github.com/alex-dev | LinkedIn: linkedin.com/in/alex-johnson");
                contentStream.newLine();
                contentStream.newLine();

                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.showText("PROFESSIONAL SUMMARY");
                contentStream.newLine();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.showText("Full Stack Developer with 3 years building scalable Java backend systems.");
                contentStream.newLine();
                contentStream.newLine();

                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.showText("EDUCATION");
                contentStream.newLine();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.showText("Bachelor of Science in Computer Science, University of California, 2023");
                contentStream.newLine();
                contentStream.newLine();

                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.showText("EXPERIENCE");
                contentStream.newLine();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.showText("Software Engineer at CloudTech Inc (2023 - Present)");
                contentStream.newLine();
                contentStream.showText("Engineered microservices using Java and Spring Boot, improving performance by 30%.");
                contentStream.newLine();
                contentStream.newLine();

                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.showText("TECHNICAL SKILLS");
                contentStream.newLine();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.showText("Java, Spring Boot, SQL, MySQL, PostgreSQL, Docker, Git, REST APIs, HTML, CSS");
                contentStream.newLine();
                contentStream.newLine();

                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.showText("PROJECTS");
                contentStream.newLine();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.showText("E-Commerce Microservices: Engineered Java Spring Boot application with MySQL.");
                contentStream.newLine();
                contentStream.showText("Optimized SQL query performance to handle 2000 transactions per second.");
                contentStream.newLine();

                contentStream.endText();
            }
            doc.save(baos);
        }

        byte[] pdfBytes = baos.toByteArray();
        assertTrue(pdfBytes.length > 0, "PDF bytes should be generated");

        // Convert to Spring MockMultipartFile
        MockMultipartFile multipartFile = new MockMultipartFile(
            "resumeFile", "Alex_Johnson_Resume.pdf", "application/pdf", pdfBytes
        );

        // Extract text using our PdfTextExtractor
        String extractedText = pdfTextExtractor.extractText(multipartFile);
        assertNotNull(extractedText);
        assertTrue(extractedText.contains("ALEX JOHNSON"), "Extracted text should contain applicant name");
        assertTrue(extractedText.contains("Spring Boot"), "Extracted text should contain Spring Boot");
        assertTrue(extractedText.contains("Docker"), "Extracted text should contain Docker");

        // Analyze extracted text
        ResumeAnalysisResult analysis = analyzerService.analyzeResumeText(extractedText);
        assertNotNull(analysis);
        assertTrue(analysis.getAtsScore() >= 50, "ATS score should be valid for this generated resume");
        assertTrue(analysis.getTechnicalSkillsIdentified().contains("Java"));
        assertTrue(analysis.getTechnicalSkillsIdentified().contains("Spring Boot"));
        assertTrue(analysis.getTechnicalSkillsIdentified().contains("Docker"));
        assertTrue(analysis.isEmailPresent());
        assertTrue(analysis.isPhonePresent());
        assertTrue(analysis.isLinkedInOrGithubPresent());
    }
}
