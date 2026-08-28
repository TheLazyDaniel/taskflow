package com.thelazydaniel.taskflow.security.permission;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DelegatingPermissionEvaluator implements PermissionEvaluator {

    private final Map<String, EntityPermissionEvaluator> evaluators;

    @Autowired
    public DelegatingPermissionEvaluator(List<EntityPermissionEvaluator> evaluatorList) {
        log.info("✅ DelegatingPermissionEvaluator constructor called with {} evaluators",
                evaluatorList != null ? evaluatorList.size() : 0);
        this.evaluators = new HashMap<>();
        for (EntityPermissionEvaluator evaluator : evaluatorList) {
            this.evaluators.put(evaluator.getEntityType(), evaluator);
            log.info("✅ Registered evaluator: {} -> {}",
                    evaluator.getEntityType(),
                    evaluator.getClass().getSimpleName());
        }

        log.info("✅ DelegatingPermissionEvaluator initialized with {} evaluators", this.evaluators.size());
    }

    @Override
    public boolean hasPermission(Authentication authentication,
                                 Object targetDomainObject,
                                 Object permission) {
        log.info("🔍 hasPermission called: auth={}, target={}, permission={}",
                authentication != null ? authentication.getName() : "null",
                targetDomainObject,
                permission);

        // Handle null cases
        if (targetDomainObject == null || permission == null) {
            log.warn("❌ Null target or permission");
            return false;
        }

        // KEY FIX: Check if targetDomainObject is a String (entity type)
        if (targetDomainObject instanceof String) {
            String type = ((String) targetDomainObject).toUpperCase();
            log.info("🔍 Target is String, treating as entity type: {}", type);
            EntityPermissionEvaluator evaluator = evaluators.get(type);
            if (evaluator == null) {
                log.warn("❌ No evaluator found for type: {}", type);
                return false;
            }
            // This calls the CREATE permission method
            return evaluator.hasPermission(authentication, permission.toString());
        }

        // Handle actual entity objects
        String type = targetDomainObject.getClass().getSimpleName().toUpperCase();
        log.info("🔍 Target is object of type: {}", type);
        EntityPermissionEvaluator evaluator = evaluators.get(type);
        if (evaluator == null) {
            log.warn("❌ No evaluator found for type: {}", type);
            return false;
        }
        return evaluator.hasPermission(authentication, targetDomainObject, permission.toString());
    }

    @Override
    public boolean hasPermission(Authentication authentication,
                                 Serializable targetId,
                                 String targetType,
                                 Object permission) {
        log.info("🔍 hasPermission called: auth={}, targetId={}, type={}, permission={}",
                authentication != null ? authentication.getName() : "null",
                targetId,
                targetType,
                permission);

        EntityPermissionEvaluator evaluator = evaluators.get(targetType.toUpperCase());
        if (evaluator == null) {
            log.warn("❌ No evaluator found for type: {}", targetType);
            return false;
        }

        if (targetId == null) {
            return evaluator.hasPermission(authentication, permission.toString());
        }

        if (targetId instanceof Long) {
            return evaluator.hasPermission(authentication, (Long) targetId, permission.toString());
        }

        log.warn("❌ Unsupported targetId type: {}", targetId.getClass().getName());
        return false;
    }
}