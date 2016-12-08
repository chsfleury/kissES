package org.kisses.core.type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kisses.core.Kisses;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Charles Fleury
 * @since 08/12/16.
 */
public class CompletionFieldTest {

  private ObjectMapper mapper = Kisses.mapper();

  @Test
  public void testJson() throws JsonProcessingException {
    List<String> input = asList("timmy's", "starbucks", "dunkin donuts");
    Map<String, List<String>> contexts = new HashMap<>();

    List<String> contextList = asList("cafe", "food");
    contexts.put("place_type", contextList);

    CompletionField field = new CompletionField(input, contexts);

    assertThat(mapper.writeValueAsString(field)).isEqualTo("{\"input\":[\"timmy's\",\"starbucks\",\"dunkin donuts\"],\"contexts\":{\"place_type\":[\"cafe\",\"food\"]}}");
  }

}