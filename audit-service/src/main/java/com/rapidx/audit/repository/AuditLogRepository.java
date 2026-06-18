package com.rapidx.audit.repository;

import com.rapidx.audit.entity.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String>, 
                                            PagingAndSortingRepository<AuditLog, String>, 
                                            JpaSpecificationExecutor<AuditLog> {
}
