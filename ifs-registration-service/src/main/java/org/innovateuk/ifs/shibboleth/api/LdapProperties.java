package org.innovateuk.ifs.shibboleth.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Component
//@ConfigurationProperties(prefix = LdapProperties.PREFIX)
public class LdapProperties {

    public static final String PREFIX = "shibboleth.ldap";

    @NotNull
    @Value("${shibboleth.ldap.port}")
    private Integer port;

    @NotNull
    @Value("${shibboleth.ldap.url}")
    private String url;

    @NotNull
    @Value("${shibboleth.ldap.user}")
    private String user;

    @NotNull
    @Value("${shibboleth.ldap.password}")
    private String password;

    @NotNull
    @Value("${shibboleth.ldap.baseDn}")
    private String baseDn;

    @NotNull
    @Value("${shibboleth.ldap.ppolicyAttrib}")
    private String ppolicyAttrib;

    // default to true - if the app can't read and parse the password lockout duration, it won't start.
    // The integration tests set this to false.
    private Boolean requireValidPPolicy = true;

    // By default set this on; but it can be set true/false in the properties file
    private Boolean nativePooling = true;

    public Integer getPort() {
        return port;
    }


    public void setPort(Integer port) {
        this.port = port;
    }


    public String getUrl() {
        return url;
    }


    public void setUrl(final String url) {
        this.url = url;
    }


    public String getUser() {
        return user;
    }


    public void setUser(final String user) {
        this.user = user;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(final String password) {
        this.password = password;
    }


    public String getBaseDn() {
        return baseDn;
    }


    public void setBaseDn(final String baseDn) {
        this.baseDn = baseDn;
    }

    public String getPpolicyAttrib() {
        return ppolicyAttrib;
    }

    public void setPpolicyAttrib(String ppolicyAttrib) {
        this.ppolicyAttrib = ppolicyAttrib;
    }

    public Boolean getRequireValidPPolicy() {
        return requireValidPPolicy;
    }

    public void setRequireValidPPolicy(Boolean requireValidPPolicy) {
        this.requireValidPPolicy = requireValidPPolicy;
    }

    public Boolean getNativePooling() {
        return nativePooling;
    }

    public void setNativePooling(Boolean nativePooling) {
        this.nativePooling = nativePooling;
    }
}
