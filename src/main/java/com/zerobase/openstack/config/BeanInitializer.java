package com.zerobase.openstack.config;

import com.zerobase.openstack.data.Constants;
import com.zerobase.openstack.data.OpenStackAuth;
import org.openstack4j.api.OSClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class BeanInitializer {
    @Bean
    @RequestScope
    public OSClient.OSClientV3 osClient() {
        HttpServletRequest httpServletRequest =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String scopedTokenId = (String) httpServletRequest.getSession().getAttribute("scopedTokenId");
        OpenStackAuth openStackAuth =
                OpenStackAuth.projectScopedAuth(
                        Constants.ADMIN_NAME,
                        Constants.ADMIN_PASSWORD,
                        "Default",
                        Constants.ADMIN_PROJECT
                );

        if (openStackAuth.validateToken(scopedTokenId)) {
            openStackAuth.setToken(openStackAuth.getTokenDetails(scopedTokenId));
        }

        return openStackAuth.getOsClient();
    }

    @Bean
    public OSClient.OSClientV3 adminOsClient() {
        return OpenStackAuth.projectScopedAuth(Constants.ADMIN_NAME, Constants.ADMIN_PASSWORD, "Default", Constants.ADMIN_PROJECT).getOsClient();
    }


}
