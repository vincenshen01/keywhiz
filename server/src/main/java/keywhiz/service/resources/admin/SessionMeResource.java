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
import io.dropwizard.auth.Auth;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import keywhiz.auth.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * parentEndpointName me
 *
 * resourceDescription Retrieve user information
 */
@Path("/admin/me")
public class SessionMeResource {
  private static final Logger logger = LoggerFactory.getLogger(SessionMeResource.class);

  /**
   * Retrieve own user information
   * @param user the current Keywhiz user
   * @return information about the input user
   *
   * description Returns JSON information about the current Keywhiz user
   * responseMessage 200 Found and retrieved User information
   */
  @Timed @ExceptionMetered
  @GET
  @Produces(APPLICATION_JSON)
  public User getInformation(@Auth User user) {
    logger.info("User '{}' accessing me.", user);
    return user;
  }
}
