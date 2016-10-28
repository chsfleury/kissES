package org.kisses.core.requests;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.kisses.core.pagination.impl.PageRequest;
import org.kisses.core.scope.Scope;

/**
 * @author Charles Fleury
 * @since 28/10/16.
 */
public class CountRequests {

  private Client client;

  public CountRequests(Client client) {
    this.client = client;
  }

  public long count(QueryBuilder query, Scope scope) {
    return count(
            client.prepareSearch(scope.getIndices())
                    .setTypes(scope.getTypes())
                    .setQuery(query)
    );
  }

  public long count(SearchRequestBuilder request) {
    return request
            .setFrom(PageRequest.ZERO.getOffset())
            .setSize(PageRequest.ZERO.getPageSize())
            .get()
            .getHits()
            .getTotalHits();
  }

}
