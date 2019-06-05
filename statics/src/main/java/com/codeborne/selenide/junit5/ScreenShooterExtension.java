package com.codeborne.selenide.junit5;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Screenshots;
import com.codeborne.selenide.ex.UIAssertionError;
import com.codeborne.selenide.logevents.SelenideLog;
import com.codeborne.selenide.logevents.SelenideLogger;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Logger;

import static com.codeborne.selenide.WebDriverRunner.driver;
import static com.codeborne.selenide.ex.ErrorMessages.screenshot;
import static com.codeborne.selenide.logevents.LogEvent.EventStatus.FAIL;

/**
 * @author Aliaksandr Rasolka
 * @since 4.12.2
 */
public class ScreenShooterExtension implements AfterAllCallback, BeforeAllCallback, TestWatcher {
  private static final Logger log = Logger.getLogger(ScreenShooterExtension.class.getName());

  private final boolean captureSuccessfulTests;

  public ScreenShooterExtension() {
    this(false);
  }

  /**
   * @param captureSuccessfulTests param that indicate if need to capture successful tests
   */
  public ScreenShooterExtension(final boolean captureSuccessfulTests) {
    this.captureSuccessfulTests = captureSuccessfulTests;
  }

  /**
   * One-liner to configure Configuration.reportsFolder property.
   *
   * @param folderWithScreenshots Folder to put screenshots to
   *
   * @return current extension instance
   */
  public ScreenShooterExtension to(final String folderWithScreenshots) {
    Configuration.reportsFolder = folderWithScreenshots;
    return this;
  }

  @Override
  public void beforeAll(final ExtensionContext context) {
    final Optional<Class<?>> testClass = context.getTestClass();
    final String className = testClass.isPresent()
      ? testClass.get().getName()
      : "EmptyClass";
    final Optional<Method> testMethod = context.getTestMethod();
    final String methodName = testMethod.isPresent()
      ? testMethod.get().getName()
      : "emptyMethod";
    Screenshots.startContext(className, methodName);
  }

  @Override
  public void afterAll(final ExtensionContext context) {
    Screenshots.finishContext();
  }

  @Override
  public void testDisabled(ExtensionContext context, Optional<String> reason) {

  }

  @Override
  public void testSuccessful(ExtensionContext context) {
    if (captureSuccessfulTests) {
      log.info(screenshot(driver()));
    }
  }

  @Override
  public void testAborted(ExtensionContext context, Throwable cause) {

  }

  @Override
  public void testFailed(ExtensionContext context, Throwable cause) {
    if (!(cause instanceof UIAssertionError) && driver() != null) {
      log.info(screenshot(driver()));
      SelenideLogger.commitStep(new SelenideLog(context.getTestMethod().toString(), context.getExecutionException().toString()), FAIL);
    }
  }
}
