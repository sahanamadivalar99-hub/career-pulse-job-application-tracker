package com.tracker.controller;

import com.tracker.dto.JobMatchResult;
import com.tracker.model.Resume;
import com.tracker.service.JobMatcherService;
import com.tracker.service.ResumeParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/job-matcher")
public class JobMatcherController {

    @Autowired
    private JobMatcherService jobMatcherService;

    @Autowired
    private ResumeParserService resumeParserService;

    @GetMapping
    public String viewMatcher(Model model) {
        List<Resume> resumes = resumeParserService.getAllResumes();
        Optional<Resume> latestResume = resumeParserService.getLatestResume();

        model.addAttribute("resumes", resumes);
        model.addAttribute("selectedResumeId", latestResume.map(Resume::getId).orElse(null));
        model.addAttribute("recentMatches", jobMatcherService.getRecentMatches());
        model.addAttribute("activePage", "job-matcher");

        return "matcher/index";
    }

    @PostMapping("/match")
    public String performMatch(@RequestParam(value = "resumeId", required = false) Long resumeId,
                               @RequestParam(value = "jobTitle", defaultValue = "Software Engineer") String jobTitle,
                               @RequestParam(value = "companyName", defaultValue = "Target Company") String companyName,
                               @RequestParam("jobDescription") String jobDescription,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (jobDescription == null || jobDescription.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please paste a job description to analyze.");
            return "redirect:/job-matcher";
        }

        Optional<Resume> resumeOpt;
        if (resumeId != null) {
            resumeOpt = resumeParserService.getResumeById(resumeId);
        } else {
            resumeOpt = resumeParserService.getLatestResume();
        }

        if (resumeOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "No resume available. Please upload a PDF resume in the Resume Analyzer first.");
            return "redirect:/resume-analyzer";
        }

        Resume resume = resumeOpt.get();
        JobMatchResult matchResult = jobMatcherService.matchJobDescription(
            jobTitle, companyName, jobDescription, resume.getExtractedText()
        );

        // Save match history
        jobMatcherService.saveJobMatch(matchResult, jobDescription, resume);

        model.addAttribute("matchResult", matchResult);
        model.addAttribute("jobTitle", jobTitle);
        model.addAttribute("companyName", companyName);
        model.addAttribute("jobDescription", jobDescription);
        model.addAttribute("currentResume", resume);
        model.addAttribute("resumes", resumeParserService.getAllResumes());
        model.addAttribute("selectedResumeId", resume.getId());
        model.addAttribute("recentMatches", jobMatcherService.getRecentMatches());
        model.addAttribute("activePage", "job-matcher");

        return "matcher/index";
    }
}
