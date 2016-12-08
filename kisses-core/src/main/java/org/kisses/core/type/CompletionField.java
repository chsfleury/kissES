package org.kisses.core.type;

import java.util.List;
import java.util.Map;

/**
 * @author Charles Fleury
 * @since 08/12/16.
 */
public class CompletionField {

  private List<String> input;
  private Map<String, List<String>> contexts;

  public CompletionField() {}

  public CompletionField(List<String> input, Map<String, List<String>> contexts) {
    this.input = input;
    this.contexts = contexts;
  }

  public List<String> getInput() {
    return input;
  }

  public void setInput(List<String> input) {
    this.input = input;
  }

  public Map<String, List<String>> getContexts() {
    return contexts;
  }

  public void setContexts(Map<String, List<String>> contexts) {
    this.contexts = contexts;
  }
}
