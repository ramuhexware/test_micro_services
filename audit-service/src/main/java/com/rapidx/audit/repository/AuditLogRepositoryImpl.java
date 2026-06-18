package com.rapidx.audit.repository;

import com.rapidx.audit.entity.AuditLog;
import com.rapidx.audit.specification.MongoSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.repository.query.FluentQuery;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class AuditLogRepositoryImpl implements JpaSpecificationExecutor<AuditLog> {

    private final MongoTemplate mongoTemplate;

    public AuditLogRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    private Query getQuery(Specification<AuditLog> spec) {
        Query query = new Query();
        if (spec instanceof MongoSpecification) {
            query.addCriteria(((MongoSpecification<AuditLog>) spec).toMongoCriteria());
        }
        return query;
    }

    @Override
    public Optional<AuditLog> findOne(Specification<AuditLog> spec) {
        AuditLog result = mongoTemplate.findOne(getQuery(spec), AuditLog.class);
        return Optional.ofNullable(result);
    }

    @Override
    public List<AuditLog> findAll(Specification<AuditLog> spec) {
        return mongoTemplate.find(getQuery(spec), AuditLog.class);
    }

    @Override
    public Page<AuditLog> findAll(Specification<AuditLog> spec, Pageable pageable) {
        Query query = getQuery(spec).with(pageable);
        List<AuditLog> list = mongoTemplate.find(query, AuditLog.class);
        return PageableExecutionUtils.getPage(list, pageable, 
            () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), AuditLog.class));
    }

    @Override
    public List<AuditLog> findAll(Specification<AuditLog> spec, Sort sort) {
        Query query = getQuery(spec).with(sort);
        return mongoTemplate.find(query, AuditLog.class);
    }

    @Override
    public long count(Specification<AuditLog> spec) {
        return mongoTemplate.count(getQuery(spec), AuditLog.class);
    }

    @Override
    public boolean exists(Specification<AuditLog> spec) {
        return mongoTemplate.exists(getQuery(spec), AuditLog.class);
    }

    @Override
    public long delete(Specification<AuditLog> spec) {
        return mongoTemplate.remove(getQuery(spec), AuditLog.class).getDeletedCount();
    }

    @Override
    public <S extends AuditLog, R> R findBy(Specification<AuditLog> spec, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("findBy method is not supported in MongoDB custom specification executor");
    }
}
