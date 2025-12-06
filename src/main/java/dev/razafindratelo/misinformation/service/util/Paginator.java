package dev.razafindratelo.misinformation.service.util;

import java.util.Map;
import java.util.function.BiFunction;
import org.springframework.stereotype.Component;

@Component
public class Paginator implements BiFunction<Integer, Integer, Map<String, Integer>> {
  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_SIZE = 10;

  @Override
  public Map<String, Integer> apply(Integer page, Integer size) {
    var fPage = (null == page) ? DEFAULT_PAGE : page;
    var fSize = (null == size) ? DEFAULT_SIZE : size;

    if (0 > fPage) throw new IllegalArgumentException("Page cannot be negative");
    if (1 > fSize) throw new IllegalArgumentException("Size cannot be less than 1.");

    return Map.of(
        "page", fPage,
        "size", fSize);
  }
}
