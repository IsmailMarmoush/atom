package io.memoria.atom.core.file;

import io.vavr.Tuple;
import io.vavr.collection.*;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.BinaryOperator;
import java.util.regex.Pattern;

import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;

public class ConfigFileOps {
  public static final BinaryOperator<String> JOIN_LINES = (a, b) -> a + System.lineSeparator() + b;
  public static final String VAR_PREFIX = "${";
  public static final String VAR_POSTFIX = "}";
  private final Option<String> nestingPrefix;
  private final boolean enableVariableInterpolation;
  private final Map<String, String> envVars;

  public ConfigFileOps(boolean enableVariableInterpolation) {
    this(null, enableVariableInterpolation);
  }

  /**
   * @param enableVariableInterpolation when true, any line which contains ${ENV_VALUE:-defaultValue} will be resolved
   *                                    from system environment then from java systemProperties
   */
  public ConfigFileOps(String nestingPrefix, boolean enableVariableInterpolation) {
    this.enableVariableInterpolation = enableVariableInterpolation;
    this.envVars = (enableVariableInterpolation) ? toMap(System.getProperties()).merge(HashMap.ofAll(System.getenv()))
                                                 : HashMap.empty();
    this.nestingPrefix = Option.of(nestingPrefix).flatMap(s -> (s.isEmpty()) ? none() : some(s));
  }

  Map<String, String> toMap(Properties props) {
    return HashSet.ofAll(props.keySet())
                  .filter(String.class::isInstance)
                  .map(k -> (String) k)
                  .toMap(k -> Tuple.of(k, props.getProperty(k)));
  }

  /**
   * if the path parameter doesn't start with "/" it's considered a file under the resources directory
   */
  public Try<String> read(String path) {
    return Try.of(() -> expand(path, null).reduce(JOIN_LINES));
  }

  private List<String> expand(String path, String line) {
    if (line == null)
      return ResourceFileOps.readResourceOrFile(path)
                            .get()
                            .flatMap(l -> expand(path, l))
                            .map(this::resolveLineExpression);
    if (nestingPrefix.isDefined() && line.trim().startsWith(nestingPrefix.get())) {
      var subFilePath = line.substring(nestingPrefix.get().length()).trim();
      var relativePath = parentPath(path) + subFilePath;
      return expand(relativePath, null);
    } else {
      return List.of(line);
    }
  }

  private String removeBraces(String line) {
    StringBuilder stringBuilder = new StringBuilder(line);
    var openingIdx = stringBuilder.indexOf(VAR_PREFIX);
    stringBuilder.replace(openingIdx, openingIdx + 2, "");
    var closingIdx = stringBuilder.lastIndexOf(VAR_POSTFIX);
    stringBuilder.replace(closingIdx, closingIdx + 1, "");
    return stringBuilder.toString().trim();
  }

  private Option<String> resolveExpression(String expression) {
    expression = removeBraces(expression);
    var split = expression.split(":-");
    if (split.length == 1) {
      var key = split[0];
      return this.envVars.get(key).orElse(none());
    }
    if (split.length == 2) {
      var key = split[0];
      var defaultValue = split[1];
      return this.envVars.get(key).orElse(some(defaultValue));
    }
    return none();
  }

  private String resolveLineExpression(String line) {
    if (this.enableVariableInterpolation) {
      var p = Pattern.compile("\\$\\{[\\sa-zA-Z_0-9]+(:-)?.+}");//NOSONAR
      var f = p.matcher(line);
      var matches = new java.util.HashMap<String, String>();
      while (f.find()) {
        var match = line.substring(f.start(), f.end());
        matches.put(match, resolveExpression(match).getOrElse(match));
      }
      for (Entry<String, String> entry : matches.entrySet()) {
        line = line.replace(entry.getKey(), entry.getValue());
      }
    }
    return line;
  }

  private static String parentPath(String filePath) {
    return filePath.replaceFirst("[^/]+$", ""); //NOSONAR
  }
}
