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

package keywhiz.service.resources.admin;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import keywhiz.auth.User;
import keywhiz.auth.cookie.AuthenticatedEncryptedCookieFactory;
import keywhiz.auth.cookie.CookieAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * parentEndpointName logout
 *
 * resourceDescription Logout from the Keywhiz admin interface
 */
@Path("/admin/logout")
public class SessionLogoutResource {
  private static final Logger logger = LoggerFactory.getLogger(SessionLogoutResource.class);

  private final CookieAuthenticator cookieAuthenticator;
  private final AuthenticatedEncryptedCookieFactory cookieFactory;

  @Inject
  public SessionLogoutResource(CookieAuthenticator cookieAuthenticator,
      AuthenticatedEncryptedCookieFactory cookieFactory) {
    this.cookieAuthenticator = cookieAuthenticator;
    this.cookieFactory = cookieFactory;
  }

  /**
   * Logout and remove any session cookies
   *
   * @return 200 on success
   * description Log out and remove any session cookies
   * responseMessage 200 Logged out successfully
   */
  @Timed @ExceptionMetered
  @POST
  @Produces(APPLICATION_JSON)
  public Response logout(@Nullable @CookieParam(value = "session") Cookie sessionCookie) {
    if (sessionCookie != null) {
      Optional<User> user = cookieAuthenticator.authenticate(sessionCookie);

      if (user.isPresent()) {
        logger.info("User logged out: {}", user.get().getName());
      } else {
        logger.warn("Invalid user cookie on logout.");
      }
    }

    NewCookie expiredCookie = cookieFactory.getExpiredSessionCookie();

    return Response.ok()
        .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
        .build();
  }
}
