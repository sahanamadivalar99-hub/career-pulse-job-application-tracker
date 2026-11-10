package com.tracker.controller;

import com.tracker.dto.ResumeAnalysisResult;
import com.tracker.model.Resume;
import com.tracker.model.ResumeAnalysis;
import com.tracker.service.ResumeAnalyzerService;
import com.tracker.service.ResumeParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/resume-analyzer")
public class ResumeController {

    @Autowired
    private ResumeParserService resumeParserService;

    @Autowired
    private ResumeAnalyzerService analyzerService;

    @GetMapping
    public String viewAnalyzer(@RequestParam(value = "id", required = false) Long resumeId, Model model) {
        Optional<Resume> resumeOpt;
        if (resumeId != null) {
            resumeOpt = resumeParserService.getResumeById(resumeId);
        } else {
            resumeOpt = resumeParserService.getLatestResume();
        }

        if (resumeOpt.isPresent()) {
            Resume resume = resumeOpt.get();
            model.addAttribute("currentResume", resume);
            if (resume.getAnalysis() != null) {
                model.addAttribute("analysis", analyzerService.toDto(resume.getAnalysis()));
            } else if (resume.getExtractedText() != null) {
                ResumeAnalysisResult freshResult = analyzerService.analyzeResumeText(resume.getExtractedText());
                analyzerService.saveAnalysis(resume, freshResult);
                model.addAttribute("analysis", freshResult);
            }
        }

        List<Resume> allResumes = resumeParserService.getAllResumes();
        model.addAttribute("resumeList", allResumes);
        model.addAttribute("activePage", "resume-analyzer");

        return "resume/analyzer";
    }

    @PostMapping("/upload")
    public String uploadResume(@RequestParam("resumeFile") MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a PDF resume file to upload.");
            return "redirect:/resume-analyzer";
        }

        try {
            Resume resume = resumeParserService.uploadAndAnalyze(file);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Resume uploaded and analyzed successfully! ATS Score: " + resume.getAnalysis().getAtsScore() + "/100");
            return "redirect:/resume-analyzer?id=" + resume.getId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/resume-analyzer";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error parsing PDF: " + e.getMessage());
            return "redirect:/resume-analyzer";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteResume(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        resumeParserService.deleteResume(id);
        redirectAttributes.addFlashAttribute("successMessage", "Resume deleted successfully.");
        return "redirect:/resume-analyzer";
    }
}
