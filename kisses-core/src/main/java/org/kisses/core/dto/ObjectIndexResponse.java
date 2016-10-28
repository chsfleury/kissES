package org.kisses.core.dto;

import org.elasticsearch.action.index.IndexResponse;

/**
 * @author Charles Fleury
 * @since 30/03/16.
 */
public class ObjectIndexResponse<T> {

  private IndexResponse response;
  private T object;

  public ObjectIndexResponse(IndexResponse response, T object) {
    this.response = response;
    this.object = object;
  }

  public IndexResponse getResponse() {
    return response;
  }

  public void setResponse(IndexResponse response) {
    this.response = response;
  }

  public T getObject() {
    return object;
  }

  public void setObject(T object) {
    this.object = object;
  }
}
