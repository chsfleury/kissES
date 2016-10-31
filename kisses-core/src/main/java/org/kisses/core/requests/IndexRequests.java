package org.kisses.core.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.kisses.core.dto.ObjectIndexResponse;
import org.kisses.core.mapping.DocumentMapping;
import org.kisses.core.mapping.MappingRegistry;

import java.util.Collection;

/**
 * @author Charles Fleury
 * @since 28/10/16.
 */
public class IndexRequests extends BulkableRequests {

  private ObjectMapper mapper;

  public IndexRequests(Client client, MappingRegistry mappingRegistry, ObjectMapper mapper) {
    super(client, mappingRegistry);
    this.mapper = mapper;
  }

  public <T> ObjectIndexResponse<T> index(T entity) {
      DocumentMapping mapping = mappingRegistry.get(entity.getClass());
      return index(entity, mapping);
  }

  public <T> void index(T entity, BulkProcessor bulk) {
    DocumentMapping mapping = mappingRegistry.get(entity.getClass());
    index(entity, mapping, bulk);
  }

  public <T> ObjectIndexResponse<T> index(T entity, DocumentMapping<T> mapping) {
    try {
      IndexResponse indexResponse = prepare(entity, mapping).get();
      if(mapping.getIdField() != null) {
        mapping.getIdField().set(entity, indexResponse.getId());
      }
      return new ObjectIndexResponse<>(indexResponse, entity);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> void index(T entity, DocumentMapping<T> mapping, BulkProcessor bulk) {
    bulk.add(prepare(entity, mapping).request());
  }

  public <T> void index(Collection<T> entities) {
    bulk(entities);
  }

  public <T> void index(Collection<T> entities, DocumentMapping<T> mapping) {
    bulk(entities, mapping);
  }

  public <T> IndexRequestBuilder prepare(T entity, DocumentMapping mapping) {
    try {
      if (mapping == null) {
        throw new IllegalArgumentException("not a managed type : " + entity.getClass().getName());
      }
      IndexRequestBuilder request = client.prepareIndex(mapping.getIndex(), mapping.getType());

      Object idValue = null;
      if (mapping.getIdField() != null) {
        idValue = mapping.getIdField().get(entity);
      }

      if (idValue != null) {
        request.setId(idValue.toString());
      }

      request.setSource(mapper.writeValueAsString(entity));
      return request;
    } catch (IllegalAccessException | JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }


}
