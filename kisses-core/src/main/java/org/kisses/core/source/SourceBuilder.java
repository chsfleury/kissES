package org.kisses.core.source;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;

import java.io.IOException;

/**
 * @author Charles Fleury
 * @since 31/10/16.
 */
public class SourceBuilder {

  private JsonFactory jsonFactory;

  public SourceBuilder() {
    jsonFactory = new JsonFactory();
  }

  public AggregationBuilder agg(String jsonSource) throws IOException {
    JsonParser jsonParser = jsonFactory.createParser(jsonSource);
    String aggName, aggBody;
    int braceToRemove = 0, braceSkipped = 0;

    JsonToken token = jsonParser.nextToken();
    if(token == JsonToken.START_OBJECT) {
      aggName = jsonParser.nextFieldName();
      braceToRemove = braceSkipped = 1;
    } else if (token == JsonToken.FIELD_NAME) {
      aggName = jsonParser.getCurrentName();
    } else {
      throw new IllegalArgumentException(jsonSource);
    }

    JsonLocation jsonLocation = jsonParser.getCurrentLocation();
    int offset = jsonLocation.getColumnNr();
    aggBody = jsonLocation.getSourceRef().toString().substring(offset - 1 - braceSkipped);
    if(braceToRemove > 0) {
      aggBody = aggBody.substring(0, aggBody.length() - braceToRemove);
    }

    return agg(aggName, aggBody);
  }

  public AggregationBuilder agg(String name, String jsonSource) {
    return new SourceAggregationBuilder(name, jsonSource);
  }

  public QueryBuilder range(String jsonSource) {
    return new SourceRangeBuilder(jsonSource);
  }
}
