package com.tracker.controller;

import com.tracker.model.Company;
import com.tracker.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/companies")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping
    public String viewCompanies(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        model.addAttribute("companies", companyService.searchCompanies(keyword));
        model.addAttribute("keyword", keyword);
        model.addAttribute("newCompany", new Company());
        model.addAttribute("activePage", "companies");
        return "companies/list";
    }

    @PostMapping("/save")
    public String saveCompany(@Valid @ModelAttribute("newCompany") Company company,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Company name is required.");
            return "redirect:/companies";
        }
        companyService.saveCompany(company);
        redirectAttributes.addFlashAttribute("successMessage", "Company saved successfully!");
        return "redirect:/companies";
    }

    @GetMapping("/delete/{id}")
    public String deleteCompany(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        companyService.deleteCompany(id);
        redirectAttributes.addFlashAttribute("successMessage", "Company removed.");
        return "redirect:/companies";
    }
}
