package org.kisses.core.dto;

import org.elasticsearch.action.get.GetResponse;

/**
 * @author Charles Fleury
 * @since 10/04/16.
 */
public class ObjectGetResponse<T> {

  private GetResponse response;
  private T object;

  public ObjectGetResponse(GetResponse response, T object) {
    this.response = response;
    this.object = object;
  }

  public GetResponse getResponse() {
    return response;
  }

  public void setResponse(GetResponse response) {
    this.response = response;
  }

  public T getObject() {
    return object;
  }

  public void setObject(T object) {
    this.object = object;
  }
}
