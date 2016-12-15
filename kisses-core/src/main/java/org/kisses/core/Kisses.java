package org.kisses.core;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.kisses.core.dto.ObjectGetResponse;
import org.kisses.core.dto.ObjectIndexResponse;
import org.kisses.core.dto.ObjectMultiGetResponse;
import org.kisses.core.dto.ObjectSearchResponse;
import org.kisses.core.dto.ObjectUpdateResponse;
import org.kisses.core.mapping.MappingRegistry;
import org.kisses.core.pagination.Pageable;
import org.kisses.core.requests.AggregationRequests;
import org.kisses.core.requests.AnalyzeRequests;
import org.kisses.core.requests.BulkableRequests;
import org.kisses.core.requests.CountRequests;
import org.kisses.core.requests.DeleteRequests;
import org.kisses.core.requests.GetRequests;
import org.kisses.core.requests.IndexRequests;
import org.kisses.core.requests.MappingRequests;
import org.kisses.core.requests.SearchRequests;
import org.kisses.core.requests.SuggestRequests;
import org.kisses.core.requests.UpdateRequests;
import org.kisses.core.scope.Scope;
import org.kisses.core.source.SourceBuilder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Charles Fleury
 * @since 27/10/16.
 */
public class Kisses {

  public static final int DEFAULT_PORT = 9300;

  private Client client;
  private MappingRegistry mappingRegistry;

  private MappingRequests mappingRequests;
  private IndexRequests indexRequests;
  private UpdateRequests updateRequests;
  private GetRequests getRequests;
  private DeleteRequests deleteRequests;
  private SearchRequests searchRequests;
  private CountRequests countRequests;
  private AggregationRequests aggregationRequests;
  private SuggestRequests suggestRequests;
  private AnalyzeRequests analyzeRequests;
  private SourceBuilder sourceBuilder;

  public Kisses(Client client, String mappingPackage) {
    this.client = client;
    ObjectMapper mapper = mapper();

    this.sourceBuilder = new SourceBuilder();
    this.mappingRequests = new MappingRequests(client);
    this.mappingRegistry = new MappingRegistry(mappingRequests, mappingPackage);
    this.mappingRequests.setRegistry(mappingRegistry);
    this.indexRequests = new IndexRequests(client, mappingRegistry, mapper);
    this.updateRequests = new UpdateRequests(client, mappingRegistry, mapper);
    this.getRequests = new GetRequests(client, mappingRegistry, mapper);
    this.deleteRequests = new DeleteRequests(client, mappingRegistry, mapper);
    this.searchRequests = new SearchRequests(client, mappingRegistry, mapper);
    this.aggregationRequests = new AggregationRequests(client, sourceBuilder);
    this.suggestRequests = new SuggestRequests(searchRequests);
    this.countRequests = new CountRequests(client);
    this.analyzeRequests = new AnalyzeRequests(client);
  }

  public static Kisses embedded(String mappingPackage, @Nullable String pathHome) throws NodeValidationException {
    Settings settings = Settings.builder()
            .put("client.type", "node")
            .put("cluster.name", "elasticsearch")
            .put("http.enabled", false)
            .put("node.name", "localNode")
            .put("transport.type", "local")
            .put("path.home", pathHome != null ? pathHome : ".")
            .build();

      Node node = new Node(settings);
      Client client = node.start().client();
      return new Kisses(client, mappingPackage);
  }

  public static Kisses transport(String mappingPackage, String clusterName, String...clusterNodes) throws UnknownHostException {
    return transport(mappingPackage, clusterName, Arrays.asList(clusterNodes));
  }

  public static Kisses transport(String mappingPackage, String clusterName, Collection<String> clusterNodes) throws UnknownHostException {
    Settings settings = Settings.builder().put("cluster.name", clusterName).build();
    TransportClient client = new PreBuiltTransportClient(settings);

    for (String node : clusterNodes) {
      String[] nodeParts = node.trim().split(":");
      String nodeAddress = nodeParts[0];
      int port;
      if (nodeParts.length == 1) {
        port = DEFAULT_PORT;
      } else if (nodeParts.length == 2) {
        port = Integer.valueOf(nodeParts[1]);
      } else {
        throw new IllegalArgumentException("invalid node " + node);
      }
      client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(nodeAddress), port));
    }

    return new Kisses(client, mappingPackage);
  }

  public static ObjectMapper mapper() {
    return new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .registerModule(new JodaModule())
            .disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
  }

  public MappingRequests mapping() {
    return mappingRequests;
  }

  public IndexRequests index() {
    return indexRequests;
  }

  public <T> ObjectIndexResponse<T> index(T entity) {
    return indexRequests.index(entity);
  }

  public UpdateRequests update() {
    return updateRequests;
  }

  public <T> ObjectUpdateResponse<T> update(T entity) {
    return updateRequests.update(entity);
  }

  public GetRequests get() {
    return getRequests;
  }

  public <T> ObjectGetResponse<T> get(Class<T> entityClass, String id) {
    return getRequests.get(entityClass, id);
  }

  public <T> ObjectMultiGetResponse<T> get(Class<T> entityClass, Set<String> ids) {
    return getRequests.get(entityClass, ids);
  }

  public DeleteRequests delete() {
    return deleteRequests;
  }

  public <T> DeleteResponse delete(Class<T> entityClass, String id) {
    return deleteRequests.delete(entityClass, id);
  }

  public SearchRequests search() {
    return searchRequests;
  }

  public <T> ObjectSearchResponse<T> search(QueryBuilder query, Class<T> target, Scope scope, Pageable pageable) {
    return searchRequests.search(query, target, scope, pageable);
  }

  public <T> void forEach(QueryBuilder query, Class<T> target, Scope scope, Consumer<T> consumer) {
    searchRequests.forEach(query, target, scope, consumer);
  }

  public AggregationRequests aggregate() {
    return aggregationRequests;
  }

  public Aggregation aggregate(QueryBuilder query, Scope scope, AggregationBuilder agg) {
    return aggregationRequests.aggregate(query, scope, agg);
  }

  public Map<String, Aggregation> aggregate(QueryBuilder query, Scope scope, Collection<AggregationBuilder> aggs) {
    return aggregationRequests.aggregate(query, scope, aggs);
  }

  public AnalyzeRequests analyze() {
    return analyzeRequests;
  }

  public AnalyzeResponse analyze(String index, String analyzer, String text) {
    return analyzeRequests.analyze(index, analyzer, text);
  }

  public SuggestRequests suggest() {
    return suggestRequests;
  }

  public List<String> suggest(Scope scope, SuggestionBuilder<?> suggestion, String text) {
    return suggestRequests.suggest(scope, suggestion, text);
  }

  public SourceBuilder source() {
    return sourceBuilder;
  }

  public CountRequests count() {
    return countRequests;
  }

  public long count(QueryBuilder query, Scope scope) {
    return countRequests.count(query, scope);
  }

  public BulkProcessor bulk(int size) {
    return BulkableRequests.bulk(client, null).setBulkActions(size).build();
  }

  public Client getClient() {
    return client;
  }

  public ClusterHealthResponse health() {
    return client.admin().cluster().prepareHealth().get();
  }

  public MappingRegistry getMappingRegistry() {
    return mappingRegistry;
  }

  public void close() {
    client.close();
  }

}
