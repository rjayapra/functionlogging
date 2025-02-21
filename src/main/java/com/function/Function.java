package com.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;


import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    private static final Logger logger = LogManager.getLogger(Function.class);
    private static final Level AUDIT = Level.getLevel("AUDIT");

    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
    @FunctionName("HttpExample")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");
        String invocationId = context.getInvocationId();

        // Setting invocation id to Log4j context for correlation of execution context logs and log4j logs in AI
        ThreadContext.put("InvocationId", invocationId);
        ThreadContext.put("FunctionName", context.getFunctionName());
        logger.info("Log4j : Java HTTP trigger processed a request.");

        // Parse query parameter
        final String query = request.getQueryParameters().get("name");
        final String name = request.getBody().orElse(query);

        if (name == null) {
            logger.warn("Name parameter is missing in the request.");
            logger.log(AUDIT, "Audit log event from Log4j. Name: " + name + ", InvocationID: " + invocationId);  
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
            logger.info("Name parameter found: {}", name);
            logger.log(AUDIT, "Audit log event from Log4j. Name: " + name + ", InvocationID: " + invocationId);  
            return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build();
        }
        
    }
}
