package io.memoria.atom.jackson.cases.company;

import io.memoria.atom.core.text.TextException;
import io.memoria.atom.jackson.Resources;
import io.memoria.atom.jackson.TestDeps;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class YamlJacksonTest {

  @Test
  void serializeEngineer() throws TextException {
    // When
    var yamlEngineer = TestDeps.yaml.serialize(Resources.BOB_ENGINEER);

    // Then
    Assertions.assertThat(yamlEngineer).isEqualTo(Resources.BOB_ENGINEER_YAML);
  }

  @Test
  void serializeManager() throws TextException {
    // When
    var yamlEngineer = TestDeps.yaml.serialize(Resources.ANNIKA_MANAGER);

    // Then
    Assertions.assertThat(yamlEngineer).isEqualTo(Resources.ANNIKA_MANAGER_YAML);
  }

  @Test
  void toEngineer() throws TextException {
    // When
    var engineer = TestDeps.yaml.deserialize(Resources.BOB_ENGINEER_YAML, Engineer.class);

    // Then
    Assertions.assertThat(engineer).isEqualTo(Resources.BOB_ENGINEER);
  }

  @Test
  void toManager() throws TextException {
    // When
    var manager = TestDeps.yaml.deserialize(Resources.ANNIKA_MANAGER_YAML, Manager.class);
    // Then
    Assertions.assertThat(manager).isEqualTo(Resources.ANNIKA_MANAGER);
  }
}