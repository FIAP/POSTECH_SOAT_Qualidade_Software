package org.example.listener;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;

public class MockMvcListener implements TestWatcher {

  @Override
  public void testFailed(ExtensionContext context, Throwable cause) {
    MvcResult mvcResult = getMvcResultFromContext(context);
    try {
      printRequestResponse(mvcResult);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private MvcResult getMvcResultFromContext(ExtensionContext context) {
    return (MvcResult) context.getStore(ExtensionContext.Namespace.create(getClass()))
        .get("mvcResult");
  }

  private void printRequestResponse(MvcResult mvcResult) throws Exception {
    ResultHandler resultHandler = print();
    resultHandler.handle(mvcResult);
  }
}
