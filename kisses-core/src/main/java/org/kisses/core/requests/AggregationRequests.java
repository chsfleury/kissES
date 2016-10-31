package org.kisses.core.requests;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.kisses.core.scope.Scope;
import org.kisses.core.source.SourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Charles Fleury
 * @since 28/10/16.
 */
public class AggregationRequests {

  private Client client;
  private SourceBuilder sourceBuilder;

  public AggregationRequests(Client client, SourceBuilder sourceBuilder) {
    this.client = client;
    this.sourceBuilder = sourceBuilder;
  }

  public Aggregation aggregate(QueryBuilder query, Scope scope, String jsonSource) throws IOException {
    return aggregate(prepare(query, scope), sourceBuilder.agg(jsonSource));
  }

  public Aggregation aggregate(QueryBuilder query, Scope scope, AggregationBuilder agg) {
    return aggregate(prepare(query, scope), agg);
  }

  public Aggregation aggregate(SearchRequestBuilder request, AggregationBuilder agg) {
    String aggName = agg.getName();
    return request
            .addAggregation(agg)
            .get()
            .getAggregations()
            .get(aggName);
  }

  public Map<String, Aggregation> aggregate(QueryBuilder query, Scope scope, List<String> aggSources) throws IOException {
    List<AggregationBuilder> aggs = new ArrayList<>();
    for(String source : aggSources) {
      aggs.add(sourceBuilder.agg(source));
    }
    return aggregate(prepare(query, scope), aggs);
  }

  public Map<String, Aggregation> aggregate(QueryBuilder query, Scope scope, Collection<AggregationBuilder> aggs) {
    return aggregate(prepare(query, scope), aggs);
  }

  public Map<String, Aggregation> aggregate(SearchRequestBuilder request, Collection<AggregationBuilder> aggs) {
    aggs.forEach(request::addAggregation);
    return request.get().getAggregations().asMap();
  }

  private SearchRequestBuilder prepare(QueryBuilder query, Scope scope) {
    return client.prepareSearch(scope.getIndices())
            .setTypes(scope.getTypes())
            .setQuery(query);
  }
}
