package org.kisses.core.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.Client;
import org.kisses.core.mapping.DocumentMapping;
import org.kisses.core.mapping.MappingRegistry;

import java.util.Collection;

/**
 * @author Charles Fleury
 * @since 28/10/16.
 */
public class DeleteRequests extends BulkableRequests {

  private ObjectMapper mapper;

  public DeleteRequests(Client client, MappingRegistry mappingRegistry, ObjectMapper mapper) {
    super(client, mappingRegistry);
    this.mapper = mapper;
  }

  public <T> DeleteResponse delete(T entity) {
    DocumentMapping mapping = mappingRegistry.get(entity.getClass());
    return delete(mapping, entity);
  }

  public <T> DeleteResponse delete(DocumentMapping<T> mapping, T entity) {
    return prepare(entity, mapping).get();
  }

  public <T> void delete(Collection<T> entities) {
    bulk(entities);
  }

  public <T> void delete(Collection<T> entities, DocumentMapping<T> mapping) {
    bulk(entities, mapping);
  }

  public DeleteRequestBuilder delete(String index, String type, String id) {
    return client.prepareDelete(index, type, id);
  }

  public <T> DeleteResponse delete(Class<T> entityClass, String id) {
    DocumentMapping<T> mapping = mappingRegistry.get(entityClass);
    if(mapping == null) {
      throw new IllegalArgumentException("not a managed type : " + entityClass.getName());
    }
    return delete(mapping, id);
  }

  public <T> DeleteResponse delete(DocumentMapping<T> mapping, String id) {
    return client.prepareDelete(mapping.getIndex(), mapping.getType(), id).get();
  }

  @Override
  public <T> DeleteRequestBuilder prepare(T entity, DocumentMapping mapping) {
    try {
      if (mapping == null) {
        throw new IllegalArgumentException("not a managed type : " + entity.getClass().getName());
      }
      Object id = null;
      if (mapping.getIdField() != null) {
        id = mapping.getIdField().get(entity);
      }
      if (id == null) {
        throw new IllegalArgumentException("id not found");
      }
      return client.prepareDelete(mapping.getIndex(), mapping.getType(), id.toString());
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
