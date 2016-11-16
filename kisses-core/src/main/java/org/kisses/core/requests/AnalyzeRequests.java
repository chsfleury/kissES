package org.kisses.core.requests;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.client.Client;

/**
 * @author Charles Fleury
 * @since 16/11/16.
 */
public class AnalyzeRequests {

  private Client client;

  public AnalyzeRequests(Client client) {
    this.client = client;
  }

  public AnalyzeResponse analyze(String index, String analyzer, String text) {
    return client.admin().indices().prepareAnalyze(index, text).setAnalyzer(analyzer).get();
  }
}
