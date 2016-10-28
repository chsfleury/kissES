package org.kisses.core.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequestBuilder;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.client.Client;
import org.kisses.core.dto.ObjectGetResponse;
import org.kisses.core.dto.ObjectMultiGetResponse;
import org.kisses.core.mapping.DocumentMapping;
import org.kisses.core.mapping.MappingRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Charles Fleury
 * @since 28/10/16.
 */
public class GetRequests {

  private Client client;
  private MappingRegistry mappingRegistry;
  private ObjectMapper mapper;

  public GetRequests(Client client, MappingRegistry mappingRegistry, ObjectMapper mapper) {
    this.client = client;
    this.mappingRegistry = mappingRegistry;
    this.mapper = mapper;
  }

  public ObjectGetResponse<?> get(String index, String type, String id) {
    Class<?> target = mappingRegistry.get(type);
    if(target == null) {
      throw new IllegalArgumentException("not a managed type: " + type);
    }
    return get(client.prepareGet(index, type, id), target);
  }

  public <T> ObjectGetResponse<T> get(Class<T> entityClass, String id) {
    DocumentMapping mapping = mappingRegistry.get(entityClass);
    if(mapping == null) {
      throw new IllegalArgumentException("not a managed type: " + entityClass.getName());
    }
    return get(client.prepareGet(mapping.getIndex(), mapping.getType(), id), entityClass);
  }

  public <T> ObjectGetResponse<T> get(DocumentMapping<T> mapping, String id) {
    return get(client.prepareGet(mapping.getIndex(), mapping.getType(), id), mapping.getDocumentClass());
  }

  public <T> ObjectGetResponse<T> get(GetRequestBuilder request, Class<T> entityClass) {
    GetResponse response = request.get();
    try {
      T object = null;
      if(response.isExists()) {
        object = mapper.readValue(response.getSourceAsBytes(), entityClass);
      }
      return new ObjectGetResponse<>(response, object);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /*
   * MULTIGET
   */

  public <T> ObjectMultiGetResponse<T> get(Class<T> entityClass, Set<String> ids) {
    DocumentMapping mapping = mappingRegistry.get(entityClass);
    if(mapping == null) {
      throw new IllegalArgumentException("not a managed type: " + entityClass.getName());
    }
    return get(client.prepareMultiGet().add(mapping.getIndex(), mapping.getType(), ids), entityClass);
  }

  public <T> ObjectMultiGetResponse<T> get(DocumentMapping<T> mapping, Set<String> ids) {
    return get(client.prepareMultiGet().add(mapping.getIndex(), mapping.getType(), ids), mapping.getDocumentClass());
  }

  public <T> ObjectMultiGetResponse<T> get(MultiGetRequestBuilder request, Class<T> entityClass) {
    MultiGetResponse response = request.get();
    List<T> results = new ArrayList<>(response.getResponses().length);
    for(MultiGetItemResponse itemResponse : response.getResponses()) {
      if(!itemResponse.isFailed()) {
        GetResponse getResponse = itemResponse.getResponse();
        try {
          T object = mapper.readValue(getResponse.getSourceAsBytes(), entityClass);
          results.add(object);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return new ObjectMultiGetResponse<>(response, results);
  }
}
