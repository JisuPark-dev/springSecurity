package com.zerobase.openstack.config.provider;

import com.zerobase.openstack.data.OpenStackAuth;
import lombok.extern.slf4j.Slf4j;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.exceptions.ClientResponseException;
import org.openstack4j.openstack.identity.v3.domain.KeystonePolicy;
import org.openstack4j.openstack.identity.v3.domain.KeystoneProject;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class OpenStackTokenAuthProvider implements AuthenticationProvider {

    private final OSClient.OSClientV3 adminOsClient;

    public OpenStackTokenAuthProvider(OSClient.OSClientV3 adminOsClient) {
        this.adminOsClient = adminOsClient;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        String domain = (String) authentication.getDetails();

        try {
            OpenStackAuth openStackAuth = new OpenStackAuth(username, password, domain);
            log.info("Login succeed:[{}]", username);
            String userId = openStackAuth.getOsClient().getToken().getUser().getId();
            String defaultProjected = adminOsClient.identity().users().get(userId).getDefaultProjectId();
            String projectId = null;
            String projectName = null;
            String domainId = null;
            String domainName = null;

            List<KeystoneProject> list = (List<KeystoneProject>) adminOsClient.identity().users().listUserProjects(userId);

            if (defaultProjected == null) {
                projectId = list.get(0).getId();
                projectName = list.get(0).getName();
                domainId = list.get(0).getDomainId();
                domainName = list.get(0).getDomain().getName();
            }else{
                KeystoneProject keystoneProject = list.stream().filter(x -> x.getId().equals(defaultProjected)).collect(Collectors.toList()).get(0);
                projectId = defaultProjected;
                projectName = keystoneProject.getName();
                domainId = keystoneProject.getDomainId();
                domainName = keystoneProject.getDomain().getName();
            }
            openStackAuth = OpenStackAuth.projectScopedAuth(username, password, domainName, projectName);

        } catch (AuthenticationException authenticationException) {

        } catch (ClientResponseException clientResponseException) {

        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return false;
    }
}
