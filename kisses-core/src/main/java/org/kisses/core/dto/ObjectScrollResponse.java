package org.kisses.core.dto;

import org.elasticsearch.action.search.SearchResponse;
import org.kisses.core.pagination.Page;

/**
 * @author Charles Fleury
 * @since 30/03/16.
 */

public class ObjectScrollResponse<T> extends ObjectSearchResponse<T> {

  protected String scrollId;
  protected long keepContextMillis;
  protected boolean dynamicMapping;

  public ObjectScrollResponse(SearchResponse response, Page<ObjectSearchResult<T>> results, long keepContextMillis, boolean dynamicMapping) {
    super(response, results);
    this.keepContextMillis = keepContextMillis;
    this.dynamicMapping = dynamicMapping;
    scrollId = response.getScrollId();
  }

  public String getScrollId() {
    return scrollId;
  }

  public void setScrollId(String scrollId) {
    this.scrollId = scrollId;
  }

  public long getKeepContextMillis() {
    return keepContextMillis;
  }

  public void setKeepContextMillis(long keepContextMillis) {
    this.keepContextMillis = keepContextMillis;
  }

  public boolean isDynamicMapping() {
    return dynamicMapping;
  }

  public void setDynamicMapping(boolean dynamicMapping) {
    this.dynamicMapping = dynamicMapping;
  }
}
