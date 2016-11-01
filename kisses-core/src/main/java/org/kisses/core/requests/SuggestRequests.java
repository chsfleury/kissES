package org.kisses.core.requests;

import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.kisses.core.scope.Scope;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Charles Fleury
 * @since 01/11/16.
 */
public class SuggestRequests {

  private SearchRequests searchRequests;

  public SuggestRequests(SearchRequests searchRequests) {
    this.searchRequests = searchRequests;
  }

  public List<String> suggest(Scope scope, SuggestionBuilder<?> suggestion, String text) {
    SuggestBuilder suggest = new SuggestBuilder().setGlobalText(text);
    suggest.addSuggestion("suggestion", suggestion);
    Suggest suggestResponse = suggest(scope, suggest);
    return suggestResponse
            .getSuggestion("suggestion")
            .getEntries()
            .stream()
            .map(entry -> entry.getText().toString())
            .collect(toList());
  }

  public Suggest suggest(Scope scope, SuggestBuilder suggest) {
    return searchRequests.prepare(scope).suggest(suggest).get().getSuggest();
  }

}
