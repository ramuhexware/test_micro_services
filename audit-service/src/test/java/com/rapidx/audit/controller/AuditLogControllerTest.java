package com.rapidx.audit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rapidx.audit.entity.AuditLog;
import com.rapidx.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditLogController.class)
public class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuditLogRepository auditLogRepository;

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog("1", "USER_LOGIN", "john_doe", "Logged in", LocalDateTime.of(2026, 6, 18, 10, 15, 30));
    }

    @Test
    void testCreateAuditLog() throws Exception {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);

        mockMvc.perform(post("/api/audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(auditLog)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.action").value("USER_LOGIN"))
                .andExpect(jsonPath("$.username").value("john_doe"));

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testGetAllAuditLogs() throws Exception {
        Page<AuditLog> page = new PageImpl<>(Arrays.asList(auditLog));
        when(auditLogRepository.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/audit")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("1"))
                .andExpect(jsonPath("$.content[0].action").value("USER_LOGIN"));
    }

    @Test
    void testGetAuditLogById_Success() throws Exception {
        when(auditLogRepository.findById("1")).thenReturn(Optional.of(auditLog));

        mockMvc.perform(get("/api/audit/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.action").value("USER_LOGIN"));
    }

    @Test
    void testGetAuditLogById_NotFound() throws Exception {
        when(auditLogRepository.findById("2")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/audit/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFilterAuditLogs() throws Exception {
        Page<AuditLog> page = new PageImpl<>(Arrays.asList(auditLog));
        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/audit/filter")
                        .param("action", "USER_LOGIN")
                        .param("username", "john_doe")
                        .param("details", "Logged")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("1"));

        ArgumentCaptor<Specification<AuditLog>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(auditLogRepository, times(1)).findAll(specCaptor.capture(), any(Pageable.class));
        assertNotNull(specCaptor.getValue());
    }

    @Test
    void testUpdateAuditLog_Success() throws Exception {
        AuditLog updateDetails = new AuditLog(null, "USER_LOGOUT", "john_doe", "Logged out", null);
        AuditLog updatedLog = new AuditLog("1", "USER_LOGOUT", "john_doe", "Logged out", auditLog.getTimestamp());

        when(auditLogRepository.findById("1")).thenReturn(Optional.of(auditLog));
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(updatedLog);

        mockMvc.perform(put("/api/audit/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.action").value("USER_LOGOUT"))
                .andExpect(jsonPath("$.details").value("Logged out"));
    }

    @Test
    void testDeleteAuditLog_Success() throws Exception {
        when(auditLogRepository.findById("1")).thenReturn(Optional.of(auditLog));
        doNothing().when(auditLogRepository).delete(auditLog);

        mockMvc.perform(delete("/api/audit/1"))
                .andExpect(status().isNoContent());

        verify(auditLogRepository, times(1)).delete(auditLog);
    }
}
