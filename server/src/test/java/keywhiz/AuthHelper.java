/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package keywhiz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.net.HttpHeaders;
import io.dropwizard.jackson.Jackson;
import java.io.IOException;
import java.util.Map;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import keywhiz.client.KeywhizClient;
import keywhiz.testing.HttpClients;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.google.common.base.Preconditions.checkArgument;

/** Static utility methods useful for establishing authentication in tests. */
public class AuthHelper {
  private static final ObjectMapper MAPPER =
      KeywhizService.customizeObjectMapper(Jackson.newObjectMapper());

  /**
   * Builds a Keywhiz login request.
   *
   * @param username login username
   * @param password login password
   * @return valid login request, given a valid username and password
   */
  public static Request buildLoginPost(@Nullable String username, @Nullable String password) {
    Map<String, String> login = Maps.newHashMap();
    if (username != null) {
      login.put("username", username);
    }

    if (password != null) {
      login.put("password", password);
    }

    RequestBody body;
    try {
      body = RequestBody.create(KeywhizClient.JSON, MAPPER.writeValueAsString(login));
    } catch (JsonProcessingException e) {
      throw new AssertionError(e);
    }

    return new Request.Builder()
        .url(HttpClients.testUrl("/admin/login"))
        .post(body)
        .addHeader("Content-Type", MediaType.APPLICATION_JSON)
        .build();
  }

  public static class AcceptRequestInterceptor implements Interceptor {
    private final String accept;

    /**
     * Creates request interceptor which sets the Accept header.
     *
     * @param accept string to set as Accept header value
     */
    public AcceptRequestInterceptor(final String accept) {
      this.accept = accept;
      checkArgument(!accept.isEmpty(), "accept must not be empty");
    }

    @Override public Response intercept(Chain chain) throws IOException {
      Request originalRequest = chain.request();
      if (originalRequest.body() == null ||
          originalRequest.header(HttpHeaders.CONTENT_ENCODING) != null) {
        return chain.proceed(originalRequest);
      }

      Request acceptRequest = originalRequest.newBuilder()
          .addHeader(HttpHeaders.ACCEPT, accept)
          .build();
      return chain.proceed(acceptRequest);
    }
  }
}
