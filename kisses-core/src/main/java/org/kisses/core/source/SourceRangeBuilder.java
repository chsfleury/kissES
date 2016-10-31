package org.kisses.core.source;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.RangeQueryBuilder;

import java.io.IOException;

/**
 * @author Charles Fleury
 * @since 31/10/16.
 */
public class SourceRangeBuilder extends RangeQueryBuilder {

  private String source;

  public SourceRangeBuilder(String source) {
    super("foo");
    this.source = source;
  }

  @Override
  protected void doXContent(XContentBuilder builder, Params params) throws IOException {
    builder.field(RangeQueryBuilder.NAME);
    XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(source);
    builder.copyCurrentStructure(parser);
    parser.close();
  }

}
