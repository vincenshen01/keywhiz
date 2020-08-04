package keywhiz.service.resources;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import io.dropwizard.setup.Environment;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import keywhiz.KeywhizConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/** Serve status information */
@Path("/_status")
@Produces(APPLICATION_JSON)
public class StatusResource {
  private static final Logger logger = LoggerFactory.getLogger(SecretDeliveryResource.class);
  Supplier<SortedMap<String, HealthCheck.Result>> memoizedCheck;

  @Inject public StatusResource(KeywhizConfig keywhizConfig, Environment environment) {
    Duration cacheExpiry = keywhizConfig.getStatusCacheExpiry();
    memoizedCheck = Suppliers.memoizeWithExpiration(() -> environment.healthChecks().runHealthChecks(),
        cacheExpiry.toMillis(), TimeUnit.MILLISECONDS);
  }

  @Timed @ExceptionMetered
  @GET
  public Response get() {
    SortedMap<String, HealthCheck.Result> results = memoizedCheck.get();

    List<String> failing = results.entrySet().stream()
        .filter(r -> !r.getValue().isHealthy())
        .map(Map.Entry::getKey)
        .collect(toList());

    if (!failing.isEmpty()) {
      logger.warn("Health checks failed: {}", results);
      String message = "failing health checks: " + Arrays.toString(failing.toArray());
      StatusResponse sr = new StatusResponse("critical", message, results);
      return Response.serverError().entity(sr).build();
    }
    StatusResponse sr = new StatusResponse("ok", "ok", results);
    return Response.ok(sr).build();
  }

  public static class StatusResponse {
    private String status;
    private String message;
    private SortedMap<String, HealthCheck.Result> results;

    public SortedMap<String, HealthCheck.Result> getResults() {
      return results;
    }

    public String getMessage() {
      return message;
    }

    public String getStatus() {
      return status;
    }

    StatusResponse(String status, String message, SortedMap<String, HealthCheck.Result> results) {
      this.status = status;
      this.message = message;
      this.results = results;

    }

    @Override public String toString() {
      return "StatusResponse{" +
          "status='" + this.getStatus() + '\'' +
          ", message='" + this.getMessage() + '\'' +
          ", results=" + this.getResults() +
          '}';
    }
  }
}
