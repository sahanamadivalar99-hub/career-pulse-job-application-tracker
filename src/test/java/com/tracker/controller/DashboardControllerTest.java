package com.tracker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testUnauthenticatedUserRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "sahana.bengaluru@example.com")
    public void testDashboardLoadsSuccessfullyForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("stats"))
                .andExpect(model().attributeExists("recentApplications"))
                .andExpect(model().attributeExists("upcomingInterviews"));
    }

    @Test
    @WithMockUser(username = "sahana.bengaluru@example.com")
    public void testApplicationsListLoadsSuccessfully() throws Exception {
        mockMvc.perform(get("/applications"))
                .andExpect(status().isOk())
                .andExpect(view().name("applications/list"))
                .andExpect(model().attributeExists("applications"));
    }

    @Test
    @WithMockUser(username = "sahana.bengaluru@example.com")
    public void testResumeAnalyzerLoadsSuccessfully() throws Exception {
        mockMvc.perform(get("/resume-analyzer"))
                .andExpect(status().isOk())
                .andExpect(view().name("resume/analyzer"))
                .andExpect(model().attributeExists("resumeList"));
    }

    @Test
    public void testLoginPageLoads() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    public void testRegisterPageLoads() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("user"));
    }
}
