package com.rapidx.audit.repository;

import com.rapidx.audit.entity.AuditLog;
import com.rapidx.audit.specification.AuditLogSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditLogRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private AuditLogRepositoryImpl repository;

    private Specification<AuditLog> spec;

    @BeforeEach
    void setUp() {
        spec = AuditLogSpecification.filterLogs("USER_LOGIN", "john_doe", "Logged");
    }

    @Test
    void testFindAll_WithSpec() {
        when(mongoTemplate.find(any(Query.class), eq(AuditLog.class)))
                .thenReturn(Collections.emptyList());

        List<AuditLog> results = repository.findAll(spec);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(AuditLog.class));
        Query capturedQuery = queryCaptor.getValue();

        assertNotNull(capturedQuery.getQueryObject());
        // Verify key structure rather than string format
        assertTrue(capturedQuery.getQueryObject().toString().contains("action"));
        assertTrue(capturedQuery.getQueryObject().toString().contains("username"));
        assertTrue(capturedQuery.getQueryObject().toString().contains("details"));
    }

    @Test
    void testFindAll_WithSpecAndPageable() {
        when(mongoTemplate.find(any(Query.class), eq(AuditLog.class)))
                .thenReturn(Collections.emptyList());

        Pageable pageable = PageRequest.of(0, 10);
        repository.findAll(spec, pageable);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(AuditLog.class));
        Query capturedQuery = queryCaptor.getValue();

        assertEquals(10, capturedQuery.getLimit());
        assertEquals(0, capturedQuery.getSkip());
    }
}
