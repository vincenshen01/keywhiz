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

package keywhiz.service.providers;

import java.security.Principal;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.SecurityContext;
import keywhiz.api.model.AutomationClient;
import keywhiz.api.model.Client;
import keywhiz.auth.mutualssl.SimplePrincipal;
import keywhiz.service.config.ClientAuthConfig;
import keywhiz.service.config.ClientAuthTypeConfig;
import keywhiz.service.daos.ClientDAO;
import org.glassfish.jersey.server.ContainerRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class AutomationClientAuthFactoryTest {
  @Rule public MockitoRule mockito = MockitoJUnit.rule();

  private static final Principal principal = SimplePrincipal.of("CN=principal,OU=blah");
  private static final Client client =
      new Client(0, "principal", null, null, null, null, null, null, null, null, true, true);
  private static final AutomationClient automationClient = AutomationClient.of(client);

  private static final int xfccDisallowedPort = 4445;

  @Mock ClientAuthTypeConfig clientAuthTypeConfig;
  @Mock ClientAuthConfig clientAuthConfig;

  @Mock ContainerRequest request;
  @Mock HttpServletRequest httpServletRequest;
  @Mock SecurityContext securityContext;
  @Mock ClientDAO clientDAO;

  AutomationClientAuthFactory factory;

  @Before public void setUp() {
    factory = new AutomationClientAuthFactory(clientDAO, clientAuthConfig);

    when(request.getSecurityContext()).thenReturn(securityContext);
    when(httpServletRequest.getLocalPort()).thenReturn(xfccDisallowedPort);
    when(clientAuthConfig.typeConfig()).thenReturn(clientAuthTypeConfig);
    when(clientAuthTypeConfig.useCommonName()).thenReturn(true);
  }

  @Test public void automationClientWhenClientPresent() {
    when(securityContext.getUserPrincipal()).thenReturn(principal);
    when(clientDAO.getClientByName("principal")).thenReturn(Optional.of(client));

    assertThat(factory.provide(request, httpServletRequest)).isEqualTo(automationClient);
  }

  @Test(expected = ForbiddenException.class)
  public void automationClientRejectsClientsWithoutAutomation() {
    Client clientWithoutAutomation =
        new Client(3423, "clientWithoutAutomation", null, null, null, null, null, null, null, null,
            true, false
        );

    when(securityContext.getUserPrincipal()).thenReturn(
        SimplePrincipal.of("CN=clientWithoutAutomation"));
    when(clientDAO.getClientByName("clientWithoutAutomation"))
        .thenReturn(Optional.of(clientWithoutAutomation));

    factory.provide(request, httpServletRequest);
  }

  @Test(expected = NotAuthorizedException.class)
  public void automationClientRejectsClientsWithoutDBEntries() {
    when(securityContext.getUserPrincipal()).thenReturn(
        SimplePrincipal.of("CN=clientWithoutDBEntry"));
    when(clientDAO.getClientByName("clientWithoutDBEntry")).thenReturn(Optional.empty());

    factory.provide(request, httpServletRequest);
  }
}
