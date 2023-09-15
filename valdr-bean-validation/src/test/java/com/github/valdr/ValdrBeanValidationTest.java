package com.github.valdr;

import com.github.valdr.cli.ValdrBeanValidation;
import com.github.valdr.util.SysOutSlurper;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests ValdrBeanValidation.
 */
public class ValdrBeanValidationTest {
  /**
   * See method name.
   */
  @Test
  public void shouldThrowExceptionIfNoConfigFileExists() {
    // given
    String[] args = {""};

    // when
    try {
      ValdrBeanValidation.main(args);
      fail("Should throw IllegalArgumentException if no config file exists.");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  /**
   * See method name.
   */
  @Test
  public void shouldLookForConfigFileInClasspathIfNoneConfigured() {
    // given
    SysOutSlurper sysOutSlurper = new SysOutSlurper();
    sysOutSlurper.activate();
    String[] args = {""};

    // when
    try {
      ValdrBeanValidation.main(args);
    } catch (IllegalArgumentException e) {
      // expected
    }

    // then
    String sysOutContent = sysOutSlurper.deactivate();
    assertThat(sysOutContent, containsString("classpath."));
  }

  /**
   * See method name.
   */
  @Test
  @SneakyThrows(IOException.class)
  public void shouldComplainAboutInvalidConfigurations() {
    // given
    String[] args = {"-cf", createTempFile("{}")};

    // when
    try {
      ValdrBeanValidation.main(args);
    } catch (Exception e) {
      // expect
      assertThat(e.getClass().getName(), is(Options.InvalidConfigurationException.class.getName()));
    }
  }

  /**
   * See method name.
   */
  @Test
  public void shouldPrintToSysOutIfConfigFileValid() throws IOException {
    // given
    SysOutSlurper sysOutSlurper = new SysOutSlurper();
    sysOutSlurper.activate();

    String[] args = {"-cf", createTempFile("{\"modelPackages\":[\"bar.foo.inexistent\"]}")};

    // when
    ValdrBeanValidation.main(args);

    // then
    String sysOutContent = sysOutSlurper.deactivate();
    assertThat(sysOutContent, containsString("{ }"));
  }

  /**
   * See method name.
   */
  @Test
  public void shouldPrintToPrintToOutputFileIfConfigured() throws IOException {
    // given
    File outputTempFile = File.createTempFile("output", "txt");

    String[] args = {"-cf", createTempFile("{\"modelPackages\":[\"bar.foo.inexistent\"]," +
      "\"outputFile\":\""+outputTempFile.getAbsolutePath()+"\"}")};

    // when
    ValdrBeanValidation.main(args);

    // then
    assertThat(FileUtils.readFileToString(outputTempFile, Charset.defaultCharset()), is("{ }"));
  }

  private String createTempFile(String string) throws IOException {
    File tempFile = File.createTempFile("valdr", "json");
    FileWriter writer = new FileWriter(tempFile);
    writer.write(string);
    writer.close();
    return tempFile.getAbsolutePath();
  }

}
