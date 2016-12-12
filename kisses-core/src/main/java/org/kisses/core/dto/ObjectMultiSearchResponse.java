package org.kisses.core.dto;

import org.elasticsearch.action.search.MultiSearchResponse;

import java.util.List;

/**
 * @author Charles Fleury
 * @since 12/12/16.
 */
public class ObjectMultiSearchResponse<T> {

  private MultiSearchResponse response;
  private List<ObjectSearchResponse<T>> results;

  public ObjectMultiSearchResponse(MultiSearchResponse response, List<ObjectSearchResponse<T>> results) {
    this.response = response;
    this.results = results;
  }

  public MultiSearchResponse getResponse() {
    return response;
  }

  public void setResponse(MultiSearchResponse response) {
    this.response = response;
  }

  public List<ObjectSearchResponse<T>> getResults() {
    return results;
  }

  public void setResults(List<ObjectSearchResponse<T>> results) {
    this.results = results;
  }
}
