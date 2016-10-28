package org.kisses.core.requests;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.kisses.core.scope.Scope;

import java.util.Collection;
import java.util.Map;

/**
 * @author Charles Fleury
 * @since 28/10/16.
 */
public class AggregationRequests {

  private Client client;

  public AggregationRequests(Client client) {
    this.client = client;
  }

  public Aggregation aggregate(QueryBuilder query, Scope scope, AggregationBuilder agg) {
    return aggregate(
            client.prepareSearch(scope.getIndices())
                    .setTypes(scope.getTypes())
                    .setQuery(query),
            agg
    );
  }

  public Aggregation aggregate(SearchRequestBuilder request, AggregationBuilder agg) {
    String aggName = agg.getName();
    return request
            .addAggregation(agg)
            .get()
            .getAggregations()
            .get(aggName);
  }

  public Map<String, Aggregation> aggregate(QueryBuilder query, Scope scope, Collection<AggregationBuilder> aggs) {
    return aggregate(
            client.prepareSearch(scope.getIndices())
                    .setTypes(scope.getTypes())
                    .setQuery(query),
            aggs
    );
  }

  public Map<String, Aggregation> aggregate(SearchRequestBuilder request, Collection<AggregationBuilder> aggs) {
    aggs.forEach(request::addAggregation);
    return request.get().getAggregations().asMap();
  }
}
