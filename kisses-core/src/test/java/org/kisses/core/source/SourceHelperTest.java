package org.kisses.core.source;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.junit.Test;
import org.kisses.core.AbstractEsTest;
import org.kisses.core.scope.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Charles Fleury
 * @since 31/10/16.
 */
public class SourceHelperTest {

  @Test
  public void test() throws IOException {
    JsonFactory jsonFactory = new JsonFactory();
    JsonParser jsonParser = jsonFactory.createParser("{\"solutions\":{\"terms\":{\"field\":\"metadata.solution\"}}}");
    String fieldName = jsonParser.nextFieldName();
    System.out.println(fieldName);
  }

}