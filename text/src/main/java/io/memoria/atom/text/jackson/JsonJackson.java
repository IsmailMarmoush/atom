package io.memoria.atom.text.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.memoria.atom.core.text.Json;
import io.memoria.atom.core.text.TextException;

public record JsonJackson(ObjectMapper mapper) implements Json {

  @Override
  public <T> String serialize(T t) {
    try {
      return mapper.writeValueAsString(t);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> T deserialize(String str, Class<T> tClass) throws TextException {
    try {
      return mapper.readValue(str, tClass);
    } catch (JsonProcessingException e) {
      throw TextException.of(e);
    }
  }
}
