package org.kisses.core.source;

import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.PipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.support.AggregationContext;

import java.io.IOException;
import java.util.Map;

/**
 * @author Charles Fleury
 * @since 31/10/16.
 */
public class SourceAggregationBuilder extends AggregationBuilder {

  private String source;

  /**
   * Constructs a new aggregation builder.
   *
   * @param name The aggregation name
   * @param type The aggregation type
   */
  protected SourceAggregationBuilder(String name, String source) {
    super(name, new InternalAggregation.Type(name));
    this.source = source;
  }

  @Override
  protected AggregatorFactory<?> build(AggregationContext context, AggregatorFactory<?> parent) throws IOException {
    return null;
  }

  @Override
  public AggregationBuilder setMetaData(Map<String, Object> metaData) {
    return this;
  }

  @Override
  public AggregationBuilder subAggregation(AggregationBuilder aggregation) {
    return this;
  }

  @Override
  public AggregationBuilder subAggregation(PipelineAggregationBuilder aggregation) {
    return this;
  }

  @Override
  protected AggregationBuilder subAggregations(AggregatorFactories.Builder subFactories) {
    return this;
  }

  @Override
  public String getWriteableName() {
    return name;
  }

  @Override
  public void writeTo(StreamOutput out) throws IOException {

  }

  @Override
  public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
    builder.field(name);
    XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(source);
    builder.copyCurrentStructure(parser);
    parser.close();
    return builder;
  }
}
