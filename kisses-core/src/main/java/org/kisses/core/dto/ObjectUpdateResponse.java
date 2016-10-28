package org.kisses.core.dto;

import org.elasticsearch.action.update.UpdateResponse;

/**
 * @author Charles Fleury
 * @since 11/09/16.
 */
public class ObjectUpdateResponse<T> {

  private UpdateResponse response;
  private T object;

  public ObjectUpdateResponse(UpdateResponse response, T object) {
    this.response = response;
    this.object = object;
  }

  public UpdateResponse getResponse() {
    return response;
  }

  public void setResponse(UpdateResponse response) {
    this.response = response;
  }

  public T getObject() {
    return object;
  }

  public void setObject(T object) {
    this.object = object;
  }
}
