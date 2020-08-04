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

package keywhiz.service.resources.automation;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.dropwizard.jersey.params.LongParam;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import keywhiz.api.model.AutomationClient;
import keywhiz.log.AuditLog;
import keywhiz.service.daos.AclDAO;
import keywhiz.service.daos.AclDAO.AclDAOFactory;
import keywhiz.service.resources.automation.v2.SecretResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * parentEndpointName enroll-secrets-automation
 * resourceDescription Assign or unassign secrets to groups
 * @deprecated Will be removed in a future release. Migrate to {@link SecretResource}.
 */
@Deprecated
@Path("/automation/secrets/{secretId}/groups/{groupId}")
@Produces(APPLICATION_JSON)
public class AutomationSecretAccessResource {
  private static final Logger logger = LoggerFactory.getLogger(AutomationSecretAccessResource.class);

  private final AclDAO aclDAO;
  private final AuditLog auditLog;

  @Inject public AutomationSecretAccessResource(AclDAOFactory aclDAOFactory, AuditLog auditLog) {
    this.aclDAO = aclDAOFactory.readwrite();
    this.auditLog = auditLog;
  }

  /**
   * Assign Secret to Group
   *
   * @param automationClient the client with automation access performing this operation
   * @param secretId the ID of the Secret to assign
   * @param groupId the ID of the Group to be assigned to
   * @return 200 on success, 404 if the secret or group is absent
   *
   * description Assigns the Secret specified by the secretID to the Group specified by the groupID
   * responseMessage 200 Successfully enrolled Secret in Group
   * responseMessage 404 Could not find Secret or Group
   */
  @Timed @ExceptionMetered
  @PUT
  public Response allowAccess(
      @Auth AutomationClient automationClient,
      @PathParam("secretId") LongParam secretId,
      @PathParam("groupId") LongParam groupId) {
    logger.info("Client '{}' allowing groupId={} access to secretId={}",
        automationClient, secretId, groupId);

    try {
      Map<String, String> extraInfo = new HashMap<>();
      extraInfo.put("deprecated", "true");
      aclDAO.findAndAllowAccess(secretId.get(), groupId.get(), auditLog, automationClient.getName(), extraInfo);
    } catch (IllegalStateException e) {
      throw new NotFoundException();
    }

    return Response.ok().build();
  }

  /**
   * Remove Secret from Group
   *
   * @param automationClient the client with automation access performing this operation
   * @param secretId the ID of the Secret to unassign
   * @param groupId the ID of the Group to be removed from
   * @return 200 on success, 404 if the secret or group is absent
   *
   * description Unassigns the Secret specified by the secretID from the Group specified by the groupID
   * responseMessage 200 Successfully removed Secret from Group
   * responseMessage 404 Could not find Secret or Group
   */
  @Timed @ExceptionMetered
  @DELETE
  public Response disallowAccess(
      @Auth AutomationClient automationClient,
      @PathParam("secretId") LongParam secretId,
      @PathParam("groupId") LongParam groupId) {
    logger.info("Client '{}' disallowing groupId={} access to secretId={}",
        automationClient, secretId, groupId);

    try {
      Map<String, String> extraInfo = new HashMap<>();
      extraInfo.put("deprecated", "true");
      aclDAO.findAndRevokeAccess(secretId.get(), groupId.get(), auditLog, automationClient.getName(), extraInfo);
    } catch (IllegalStateException e) {
      throw new NotFoundException();
    }

    return Response.ok().build();
  }
}
