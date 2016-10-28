package org.kisses.core.dto;

import org.elasticsearch.search.SearchHit;

/**
 * @author Charles Fleury
 * @since 11/05/16.
 */
public class ObjectSearchResult<T> {
  private SearchHit hit;
  private T object;

  public ObjectSearchResult(SearchHit hit, T object) {
    this.hit = hit;
    this.object = object;
  }

  public SearchHit getHit() {
    return hit;
  }

  public void setHit(SearchHit hit) {
    this.hit = hit;
  }

  public T getObject() {
    return object;
  }

  public void setObject(T object) {
    this.object = object;
  }
}
