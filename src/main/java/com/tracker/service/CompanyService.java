package com.tracker.service;

import com.tracker.model.Company;
import com.tracker.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    public List<Company> getAllCompanies() {
        return companyRepository.findAllByOrderByNameAsc();
    }

    public List<Company> searchCompanies(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCompanies();
        }
        return companyRepository.findByNameContainingIgnoreCaseOrIndustryContainingIgnoreCase(keyword.trim(), keyword.trim());
    }

    public Optional<Company> getCompanyById(Long id) {
        return companyRepository.findById(id);
    }

    @Transactional
    public Company saveCompany(Company company) {
        return companyRepository.save(company);
    }

    @Transactional
    public Company getOrCreateCompany(String name, String location) {
        return companyRepository.findByNameIgnoreCase(name.trim())
                .orElseGet(() -> {
                    Company c = new Company();
                    c.setName(name.trim());
                    c.setLocation(location);
                    c.setIndustry("Technology & Software");
                    c.setRating(4.0);
                    return companyRepository.save(c);
                });
    }

    @Transactional
    public void deleteCompany(Long id) {
        companyRepository.deleteById(id);
    }
}
