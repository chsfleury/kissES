package org.kisses.core.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.kisses.core.dto.ObjectMultiSearchResponse;
import org.kisses.core.dto.ObjectScrollResponse;
import org.kisses.core.dto.ObjectSearchResponse;
import org.kisses.core.dto.ObjectSearchResult;
import org.kisses.core.mapping.DocumentMapping;
import org.kisses.core.mapping.MappingRegistry;
import org.kisses.core.pagination.Pageable;
import org.kisses.core.pagination.impl.DefaultPage;
import org.kisses.core.pagination.impl.PageRequest;
import org.kisses.core.scope.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * @author Charles Fleury
 * @since 28/10/16.
 */
public class SearchRequests {

  public static final Logger LOGGER = LoggerFactory.getLogger(SearchRequests.class);
  protected static final long DEFAULT_KEEP_CONTEXT = 15000;

  private Client client;
  private MappingRegistry mappingRegistry;
  private ObjectMapper mapper;

  public SearchRequests(Client client, MappingRegistry mappingRegistry, ObjectMapper mapper) {
    this.client = client;
    this.mappingRegistry = mappingRegistry;
    this.mapper = mapper;
  }

  public <T> ObjectSearchResponse<T> search(QueryBuilder query, DocumentMapping<T> mapping, Pageable pageable, SortBuilder...sorts) {
    return search(query, mapping.getDocumentClass(), mapping.getScope(), pageable);
  }

  public <T> ObjectSearchResponse<T> search(QueryBuilder query, Class<T> target, Scope scope, Pageable pageable, SortBuilder...sorts) {
    SearchRequestBuilder request = prepare(scope, query);

    if(sorts != null) {
      for(SortBuilder sort : sorts) {
        request.addSort(sort);
      }
    }

    return search(
            request,
            target,
            scope.getTypes().length > 1,
            pageable
    );
  }

  public SearchRequestBuilder prepare(Scope scope) {
    return prepare(scope, null);
  }

  public SearchRequestBuilder prepare(Scope scope, @Nullable QueryBuilder query) {
    return client.prepareSearch(scope.getIndices())
            .setTypes(scope.getTypes())
            .setQuery(query);
  }

  public <T> ObjectSearchResponse<T> search(SearchRequestBuilder request, Class<T> target, boolean dynamicMapping, Pageable pageable) {
    try {
      SearchResponse searchResponse = apply(request, pageable).get();
      List<ObjectSearchResult<T>> content = convert(searchResponse.getHits(), target, dynamicMapping);
      return new ObjectSearchResponse<>(searchResponse, new DefaultPage<>(content, pageable, searchResponse.getHits().getTotalHits()));
    } catch (IOException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> void forEach(QueryBuilder query, DocumentMapping<T> mapping, Consumer<T> consumer) {
    forEach(query, mapping.getDocumentClass(), mapping.getScope(), consumer);
  }

  public <T> void forEach(QueryBuilder query, Class<T> target, Scope scope, Consumer<T> consumer) {
    forEach(
            prepare(scope, query),
            target,
            scope.getTypes().length > 1,
            consumer
    );
  }

  public <T> void forEach(SearchRequestBuilder request, Class<T> targetClass, boolean dynamicMapping, Consumer<T> consumer) {
    ObjectScrollResponse<T> response = scroll(request, targetClass, dynamicMapping, DEFAULT_KEEP_CONTEXT);
    boolean hasNext = true;
    while(response.getResults().hasContent() && hasNext) {
      response.getResults().getContent().stream().map(ObjectSearchResult::getObject).forEach(consumer);
      if(response.getResults().hasNext()) {
        response = scroll(targetClass, response);
      } else {
        hasNext = false;
      }
    }
  }

  public <T> ObjectScrollResponse<T> scroll(QueryBuilder query, Class<T> target, Scope scope, long millis) {
    return scroll(
            prepare(scope, query),
            target,
            scope.getTypes().length > 1,
            millis
    );
  }

  public <T> ObjectScrollResponse<T> scroll(SearchRequestBuilder request, Class<T> target, boolean dynamicMapping, long millis) {
    try {
      SearchResponse searchResponse = request
              .addSort("_doc", SortOrder.ASC)
              .setScroll(new TimeValue(millis))
              .setSize(PageRequest.MAX_SIZE)
              .get();
      List<ObjectSearchResult<T>> content = convert(searchResponse.getHits(), target, dynamicMapping);
      return new ObjectScrollResponse<>(searchResponse, new DefaultPage<>(content, PageRequest.SCROLL, searchResponse.getHits().getTotalHits()), millis, dynamicMapping);
    } catch (IOException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> ObjectScrollResponse<T> scroll(Class<T> target, ObjectScrollResponse<T> previousResponse) {
    try {
      SearchResponse searchResponse = client.prepareSearchScroll(previousResponse.getScrollId()).setScroll(new TimeValue(previousResponse.getKeepContextMillis())).get();
      List<ObjectSearchResult<T>> content = convert(searchResponse.getHits(), target, previousResponse.isDynamicMapping());
      return new ObjectScrollResponse<>(searchResponse, new DefaultPage<>(content, previousResponse.getResults().nextPageable(), searchResponse.getHits().getTotalHits()), previousResponse.getKeepContextMillis(), previousResponse.isDynamicMapping());
    } catch (IOException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> ObjectMultiSearchResponse<T> multi(DocumentMapping<T> mapping, Pageable pageable, QueryBuilder...queries) {
    return multi(mapping, pageable, asList(queries));
  }

  public <T> ObjectMultiSearchResponse<T> multi(DocumentMapping<T> mapping, Pageable pageable, List<QueryBuilder> queries) {
    List<SearchRequestBuilder> requests = queries.stream().map(query -> prepare(mapping.getScope(), query)).collect(toList());
    return multi(mapping.getDocumentClass(), mapping.getScope().getTypes().length > 1, pageable, requests);
  }

  public <T> ObjectMultiSearchResponse<T> multi(DocumentMapping<T> mapping, Pageable pageable, SearchRequestBuilder...requests) {
    return multi(mapping.getDocumentClass(), mapping.getScope().getTypes().length > 1, pageable, asList(requests));
  }

  public <T> ObjectMultiSearchResponse<T> multi(Class<T> target, boolean dynamicMapping, Pageable pageable, SearchRequestBuilder...requests) {
    return multi(target, dynamicMapping, pageable, asList(requests));
  }

  public <T> ObjectMultiSearchResponse<T> multi(Class<T> target, boolean dynamicMapping, Pageable pageable, List<SearchRequestBuilder> requests) {
    MultiSearchRequestBuilder multiRequest = client.prepareMultiSearch();
    for(SearchRequestBuilder request : requests) {
      multiRequest.add(apply(request, pageable));
    }

    MultiSearchResponse response = multiRequest.get();
    List<ObjectSearchResponse<T>> results = new ArrayList<>(requests.size());
    for(MultiSearchResponse.Item item : response.getResponses()) {
      if(!item.isFailure()) {
        try {
          SearchResponse subResponse = item.getResponse();
          List<ObjectSearchResult<T>> content = convert(subResponse.getHits(), target, dynamicMapping);
          results.add(new ObjectSearchResponse<>(subResponse, new DefaultPage<>(content, pageable, subResponse.getHits().getTotalHits())));
        } catch (IOException | IllegalAccessException e) {
          LOGGER.error("Multisearch error", e);
        }
      } else {
        LOGGER.error("Multisearch error : " + item.getFailureMessage(), item.getFailure());
      }
    }

    return new ObjectMultiSearchResponse<>(response, results);
  }

  private <T> List<ObjectSearchResult<T>> convert(SearchHits hits, Class<T> targetClass, boolean dynamicMapping) throws IOException, IllegalAccessException {
    List<ObjectSearchResult<T>> list = new ArrayList<>(hits.getHits().length);
    DocumentMapping documentMapping = null;
    if(!dynamicMapping) {
      documentMapping = mappingRegistry.get(targetClass);
    }
    for (SearchHit hit : hits) {
      Class<?> target = dynamicMapping ? mappingRegistry.get(hit.getType()) : targetClass;
      if(dynamicMapping) {
        documentMapping = mappingRegistry.get(target);
      }
      T object = (T) mapper.readValue(hit.source(), target);
      if(documentMapping != null && documentMapping.getIdField() != null) {
        documentMapping.getIdField().set(object, hit.getId());
      }
      ObjectSearchResult<T> result = new ObjectSearchResult<>(hit, object);
      list.add(result);
    }
    return list;
  }

  private SearchRequestBuilder apply(SearchRequestBuilder request, Pageable pageable) {
    return pageable == null ? request : request.setSize(pageable.getPageSize()).setFrom(pageable.getOffset());
  }
}
