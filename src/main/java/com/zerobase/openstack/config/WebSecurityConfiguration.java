package com.zerobase.openstack.config;

import com.zerobase.openstack.config.filter.OpenStackFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Override
    public void configure(WebSecurity web) throws Exception {
        // 해당 부분은 인증을 거치지 않고도 실행 가능하도록 함
        web.ignoring()
                .antMatchers("/i18n/**")
                .antMatchers("/static/**")
                .antMatchers("/css/**")
                .antMatchers("/js/**")
                .antMatchers("/images/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //At -> 해당 필터를 바꿔치기함.
        http.addFilterAt(openStackFilter(), UsernamePasswordAuthenticationFilter.class);
        http.headers().frameOptions().sameOrigin()
                .and().formLogin().loginPage("/login")
                .and().logout().logoutUrl("/logout")
                .and().authorizeRequests().antMatchers("/login", "/").permitAll()
                .and().authorizeRequests().anyRequest().authenticated();
        http.csrf().disable();
    }

    private OpenStackFilter openStackFilter() throws Exception{
        OpenStackFilter openStackFilter = new OpenStackFilter(this.authenticationManager());
        //TODO : successful, Failure Handler
        return openStackFilter;
    }
}
