package org.kisses.core.dto;

import org.elasticsearch.action.get.MultiGetResponse;

import java.util.List;

/**
 * @author Charles Fleury
 * @since 10/04/16.
 */
public class ObjectMultiGetResponse<T> {

  private MultiGetResponse response;
  private List<T> objects;

  public ObjectMultiGetResponse(MultiGetResponse response, List<T> objects) {
    this.response = response;
    this.objects = objects;
  }

  public MultiGetResponse getResponse() {
    return response;
  }

  public void setResponse(MultiGetResponse response) {
    this.response = response;
  }

  public List<T> getObjects() {
    return objects;
  }

  public void setObjects(List<T> objects) {
    this.objects = objects;
  }

}
