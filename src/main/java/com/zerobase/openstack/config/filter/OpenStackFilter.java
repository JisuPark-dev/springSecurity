package com.zerobase.openstack.config.filter;

import com.zerobase.openstack.data.OpenStackAuth;
import org.openstack4j.api.exceptions.ClientResponseException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OpenStackFilter extends UsernamePasswordAuthenticationFilter {
    //UsernamePasswordAuthenticationFilter 내에 있는 변수들 그대로 생성
    private boolean postOnly = true;
    private SessionAuthenticationStrategy sessionAuthenticationStrategy = new NullAuthenticatedSessionStrategy();
    private boolean continueChainBeforeSuccessfulAuthentication = false;

    public String obtainDomain(HttpServletRequest httpServletRequest) {
        return (String) httpServletRequest.getParameter("domain");
    }

    public OpenStackFilter() {
    }

    public OpenStackFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    @Override
    public void setSessionAuthenticationStrategy(SessionAuthenticationStrategy sessionAuthenticationStrategy) {
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
    }

    @Override
    public void setContinueChainBeforeSuccessfulAuthentication(boolean continueChainBeforeSuccessfulAuthentication) {
        this.continueChainBeforeSuccessfulAuthentication = continueChainBeforeSuccessfulAuthentication;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // POST로 넘어오는 username, password, domain 정보 받기
        String username = obtainUsername(request);
        username = (username != null) ? username : "";
        username = username.trim();
        String password = obtainPassword(request);
        password = (password != null) ? password : "";
        String domain = obtainDomain(request);
        domain = (domain != null) ? domain : "";

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
        usernamePasswordAuthenticationToken.setDetails(domain);

        return this.getAuthenticationManager().authenticate(usernamePasswordAuthenticationToken);
    }

    private boolean checkExpire(String tokenExpire) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date tokenExpireDateTime = dateFormat.parse(tokenExpire);
        // Date 객체의 after 메서드를 사용하여 tokenExpireDateTime이 현재 시점 (new Date())보다 미래인지 확인합니다.
        return tokenExpireDateTime.after(new Date());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        if (!requiresAuthentication(httpServletRequest, httpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession httpSession = httpServletRequest.getSession();
        String tokenId = (String) httpSession.getAttribute("unscopedTokenId");
        String tokenExpired = (String) httpSession.getAttribute("tokenExpired");

        //토큰이 있다면
        if (tokenId != null && tokenExpired != null) {
            try {
                if (checkExpire(tokenExpired)) {
                    //TODO :  인증 객체 구현 필요
                    new OpenStackAuth(tokenId);
                    chain.doFilter(httpServletRequest, httpServletResponse);
                } else {
                    unsuccessfulAuthentication(httpServletRequest, httpServletResponse, new AuthenticationServiceException("Token has been expired"));
                }
            } catch (ClientResponseException clientResponseException) {
                unsuccessfulAuthentication(httpServletRequest, httpServletResponse, new AuthenticationServiceException("Token id is invalidated"));
            } catch (ParseException parseException) {
                unsuccessfulAuthentication(httpServletRequest, httpServletResponse, new AuthenticationServiceException("Token expired date time format invalidated"));
            }
        // 토큰이 없다면
        }else{
            if (this.postOnly && httpServletRequest.getMethod().equals("POST")) {
                String username = this.obtainUsername(httpServletRequest);
                String password = this.obtainPassword(httpServletRequest);
                String domain = this.obtainDomain(httpServletRequest);

                if (username != null && password != null && domain != null) {
                    try {
                        Authentication authenticationResult = attemptAuthentication(httpServletRequest, httpServletResponse);
                        if (authenticationResult == null) {
                            return;
                        }
                        this.sessionAuthenticationStrategy.onAuthentication(authenticationResult, httpServletRequest, httpServletResponse);
                        if (this.continueChainBeforeSuccessfulAuthentication) {
                            chain.doFilter(httpServletRequest, httpServletResponse);
                        }
                        successfulAuthentication(httpServletRequest, httpServletResponse, chain, authenticationResult);
                    } catch (InternalAuthenticationServiceException internalAuthenticationServiceException) {
                        unsuccessfulAuthentication(httpServletRequest, httpServletResponse, internalAuthenticationServiceException);
                    } catch (AuthenticationServiceException authenticationServiceException) {
                        unsuccessfulAuthentication(httpServletRequest, httpServletResponse, authenticationServiceException);
                    }
                } else {
                    unsuccessfulAuthentication(httpServletRequest, httpServletResponse, new AuthenticationServiceException("Bye bye."));
                }
            } else {
                unsuccessfulAuthentication(httpServletRequest, httpServletResponse, new AuthenticationServiceException("Bye bye."));
            }
        }
    }
}
