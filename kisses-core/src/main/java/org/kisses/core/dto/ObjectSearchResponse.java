package org.kisses.core.dto;

import org.elasticsearch.action.search.SearchResponse;
import org.kisses.core.pagination.Page;

/**
 * @author Charles Fleury
 * @since 30/03/16.
 */

public class ObjectSearchResponse<T> {

  protected SearchResponse response;

  protected Page<ObjectSearchResult<T>> results;

  public ObjectSearchResponse(SearchResponse response, Page<ObjectSearchResult<T>> results) {
    this.response = response;
    this.results = results;
  }

  public SearchResponse getResponse() {
    return response;
  }

  public void setResponse(SearchResponse response) {
    this.response = response;
  }

  public Page<ObjectSearchResult<T>> getResults() {
    return results;
  }

  public void setResults(Page<ObjectSearchResult<T>> results) {
    this.results = results;
  }

  public T getFirst() {
    if(results != null && results.hasContent()) {
      return results.getContent().get(0).getObject();
    } else {
      return null;
    }
  }

}
