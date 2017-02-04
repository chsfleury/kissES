package org.kisses.core.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.kisses.core.dto.ObjectUpdateResponse;
import org.kisses.core.mapping.DocumentMapping;
import org.kisses.core.mapping.MappingRegistry;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Charles Fleury
 * @since 28/10/16.
 */
public class UpdateRequests extends BulkableRequests {

  private ObjectMapper mapper;

  public UpdateRequests(Client client, MappingRegistry mappingRegistry, ObjectMapper mapper) {
    super(client, mappingRegistry);
    this.mapper = mapper;
  }

  public <T> ObjectUpdateResponse<T> update(T entity) {
    return update(prepare(entity), entity);
  }

  public <T> ObjectUpdateResponse<T> update(Object id, T entity) {
    return update(prepare(id, entity), entity);
  }

  public <T> void update(Object id, T entity, BulkProcessor bulk) {
    bulk.add(prepare(id, entity).request());
  }

  public <T> ObjectUpdateResponse<T> update(T entity, Map<String, Object> newFieldMap) {
    return update(prepare(entity, newFieldMap), entity);
  }

  public <T> void update(T entity, Map<String, Object> newFieldMap, BulkProcessor bulk) {
    bulk.add(prepare(entity, newFieldMap).request());
  }

  public <T> ObjectUpdateResponse<T> update(T entity, DocumentMapping<T> mapping) {
    return update(prepare(entity, mapping), entity);
  }

  public <T> ObjectUpdateResponse<T> update(T entity, DocumentMapping<T> mapping, Map<String, Object> newFieldMap) {
    return update(prepare(entity, mapping, newFieldMap), entity);
  }

  public <T> void update(T entity, DocumentMapping<T> mapping, Map<String, Object> newFieldMap, BulkProcessor bulk) {
    bulk.add(prepare(entity, mapping, newFieldMap).request());
  }


  public <T> void update(Collection<T> entities) {
    bulk(entities);
  }

  public <T> void update(Collection<T> entities, DocumentMapping<T> mapping) {
    bulk(entities, mapping);
  }

  private <T> ObjectUpdateResponse<T> update(UpdateRequestBuilder updateRequest, T entity) {
    try {
      UpdateResponse updateResponse = updateRequest.get();
      T object = null;
      if (updateResponse.getGetResult() != null) {
        object = (T) mapper.readValue(updateResponse.getGetResult().source(), entity.getClass());
      }
      return new ObjectUpdateResponse<>(updateResponse, object);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> UpdateRequestBuilder prepare(T entity) {
    DocumentMapping mapping = mappingRegistry.get(entity.getClass());
    return prepare(entity, mapping);
  }

  public <T> UpdateRequestBuilder prepare(Object id, T entity) {
    DocumentMapping mapping = mappingRegistry.get(entity.getClass());
    return prepare(id, entity, mapping);
  }

  public <T> UpdateRequestBuilder prepare(T entity, DocumentMapping mapping) {
    Map newFieldMap = mapper.convertValue(entity, Map.class);
    return prepare(entity, mapping, newFieldMap);
  }

  public <T> UpdateRequestBuilder prepare(Object id, T entity, DocumentMapping mapping) {
    Map newFieldMap = mapper.convertValue(entity, Map.class);
    return prepare(id, entity, mapping, newFieldMap);
  }

  public <T> UpdateRequestBuilder prepare(T entity, Map newFieldMap) {
    DocumentMapping mapping = mappingRegistry.get(entity.getClass());
    return prepare(entity, mapping, newFieldMap);
  }

  public <T> UpdateRequestBuilder prepare(T entity, DocumentMapping mapping, Map newFieldMap) {
    return prepare(null, entity, mapping, newFieldMap);
  }

  public <T> UpdateRequestBuilder prepare(Object id, T entity, DocumentMapping mapping, Map newFieldMap) {
    try {
      if (mapping == null) {
        throw new IllegalArgumentException("not a managed type : " + entity.getClass().getName());
      }

      String idFieldName = null;
      if (mapping.getIdField() != null) {
        idFieldName = mapping.getIdField().getName();
        id = mapping.getIdField().get(entity);
      }

      if (id == null) {
        throw new IllegalArgumentException("id not found");
      }

      if (idFieldName != null && !idFieldName.isEmpty()) {
        newFieldMap.remove(idFieldName);
      }

      return client.prepareUpdate(mapping.getIndex(), mapping.getType(), id.toString()).setDoc(unflat(newFieldMap));
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private Map unflat(Map<?, ?> map) {
    if(map == null) {
      return null;
    } else if (map.keySet().stream().anyMatch(key -> key.toString().contains("."))) {
      Map<String, Object> newMap = new HashMap<>();
      map.forEach((keyObject, value) -> {
        String key = keyObject.toString();
        if(!key.contains(".")) {
          newMap.put(key, value);
        } else {
          Iterator<String> keyParts = Arrays.asList(key.split("\\.")).iterator();
          if(keyParts.hasNext()) {
            put(keyParts, value, newMap);
          }
        }
      });
      return newMap;
    } else {
      return map;
    }
  }

  private void put(Iterator<String> keyParts, Object value, Map<String, Object> map) {
    String nextKeypart = keyParts.next();
    Object val = map.get(nextKeypart);
    if(keyParts.hasNext()) {
      Map<String, Object> nestedMap;
      if(val != null) {
        nestedMap = (Map<String, Object>) val;
      } else {
        nestedMap = new HashMap<>();
        map.put(nextKeypart, nestedMap);
      }
      put(keyParts, value, nestedMap);
    } else {
      map.put(nextKeypart, value);
    }
  }
}
