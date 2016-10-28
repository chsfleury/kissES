package org.kisses.core.dto;

import org.elasticsearch.action.search.SearchResponse;

/**
 * @author Charles Fleury
 * @since 22/04/16.
 */
public class ObjectCountResponse {

  private SearchResponse response;
  private long count;

  public ObjectCountResponse(SearchResponse response, long count) {
    this.response = response;
    this.count = count;
  }

  public SearchResponse getResponse() {
    return response;
  }

  public void setResponse(SearchResponse response) {
    this.response = response;
  }

  public long getCount() {
    return count;
  }

  public void setCount(long count) {
    this.count = count;
  }
}
