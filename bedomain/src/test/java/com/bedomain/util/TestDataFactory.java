package com.bedomain.util;

import com.bedomain.domain.dto.entityinstance.CreateEntityInstanceRequest;
import com.bedomain.domain.dto.entityinstance.UpdateEntityInstanceRequest;
import com.bedomain.domain.dto.entitytype.CreateEntityTypeRequest;
import com.bedomain.domain.dto.entitytype.UpdateEntityTypeRequest;
import com.bedomain.domain.dto.property.CreatePropertyRequest;
import com.bedomain.domain.dto.property.UpdatePropertyRequest;
import com.bedomain.domain.entity.EntityInstance;
import com.bedomain.domain.entity.EntityType;
import com.bedomain.domain.entity.Property;
import com.bedomain.domain.enums.DataType;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestDataFactory {

    private TestDataFactory() {
    }

    // EntityType methods
    public static EntityType createEntityType(UUID id, String name, String description) {
        EntityType entityType = EntityType.builder()
            .id(id != null ? id : UUID.randomUUID())
            .name(name)
            .description(description)
            .deleted(false)
            .createdAt(Instant.now())
            .createdBy("test-user")
            .updatedAt(Instant.now())
            .updatedBy("test-user")
            .build();
        return entityType;
    }

    public static EntityType createEntityType(String name) {
        return createEntityType(UUID.randomUUID(), name, "Test description");
    }

    public static CreateEntityTypeRequest createCreateEntityTypeRequest(String name) {
        CreateEntityTypeRequest request = new CreateEntityTypeRequest();
        request.setName(name);
        request.setDescription("Test description");
        return request;
    }

    public static UpdateEntityTypeRequest createUpdateEntityTypeRequest(String name, String description) {
        UpdateEntityTypeRequest request = new UpdateEntityTypeRequest();
        request.setName(java.util.Optional.ofNullable(name));
        request.setDescription(java.util.Optional.ofNullable(description));
        return request;
    }

    // Property methods
    public static Property createProperty(UUID id, String name, DataType dataType, EntityType entityType) {
        Property property = Property.builder()
            .id(id != null ? id : UUID.randomUUID())
            .name(name)
            .description("Test property description")
            .dataType(dataType)
            .entityType(entityType)
            .createdAt(Instant.now())
            .createdBy("test-user")
            .updatedAt(Instant.now())
            .updatedBy("test-user")
            .build();
        return property;
    }

    public static Property createProperty(String name, DataType dataType, EntityType entityType) {
        return createProperty(UUID.randomUUID(), name, dataType, entityType);
    }

    public static CreatePropertyRequest createCreatePropertyRequest(String name, DataType dataType) {
        CreatePropertyRequest request = new CreatePropertyRequest();
        request.setName(name);
        request.setDescription("Test property description");
        request.setDataType(dataType);
        return request;
    }

    public static UpdatePropertyRequest createUpdatePropertyRequest(String name, String description, DataType dataType) {
        UpdatePropertyRequest request = new UpdatePropertyRequest();
        request.setName(java.util.Optional.ofNullable(name));
        request.setDescription(java.util.Optional.ofNullable(description));
        request.setDataType(java.util.Optional.ofNullable(dataType));
        return request;
    }

    // EntityInstance methods
    public static EntityInstance createEntityInstance(UUID id, EntityType entityType, Map<String, Object> attributes) {
        EntityInstance instance = EntityInstance.builder()
            .id(id != null ? id : UUID.randomUUID())
            .entityType(entityType)
            .attributes(attributes != null ? attributes : new HashMap<>())
            .deleted(false)
            .createdAt(Instant.now())
            .createdBy("test-user")
            .updatedAt(Instant.now())
            .updatedBy("test-user")
            .build();
        return instance;
    }

    public static EntityInstance createEntityInstance(EntityType entityType) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("testField", "testValue");
        return createEntityInstance(UUID.randomUUID(), entityType, attributes);
    }

    public static CreateEntityInstanceRequest createCreateEntityInstanceRequest(UUID entityTypeId) {
        CreateEntityInstanceRequest request = new CreateEntityInstanceRequest();
        request.setEntityTypeId(entityTypeId);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("testField", "testValue");
        request.setAttributes(attributes);
        return request;
    }

    public static UpdateEntityInstanceRequest createUpdateEntityInstanceRequest() {
        UpdateEntityInstanceRequest request = new UpdateEntityInstanceRequest();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("updatedField", "updatedValue");
        request.setAttributes(attributes);
        return request;
    }

    // Sample data
    public static final DataType STRING_TYPE = DataType.STRING;
    public static final DataType NUMBER_TYPE = DataType.NUMBER;
    public static final DataType BOOLEAN_TYPE = DataType.BOOLEAN;
    public static final DataType DATE_TYPE = DataType.DATE;
    public static final DataType TEXT_TYPE = DataType.TEXT;
}
