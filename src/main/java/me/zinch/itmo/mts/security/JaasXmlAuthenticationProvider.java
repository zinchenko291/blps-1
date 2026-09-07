package me.zinch.itmo.mts.security;

import me.zinch.itmo.mts.security.jaas.XmlAccountLoginModule;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import java.util.Map;

@Component
public class JaasXmlAuthenticationProvider implements AuthenticationProvider {
    private final XmlAccountStore accountStore;
    public JaasXmlAuthenticationProvider(XmlAccountStore accountStore) { this.accountStore = accountStore; }
    @Override public Authentication authenticate(Authentication authentication) {
        String login = authentication.getName(), password = String.valueOf(authentication.getCredentials());
        try {
            var config = new Configuration() { @Override public AppConfigurationEntry[] getAppConfigurationEntry(String name) { return new AppConfigurationEntry[]{new AppConfigurationEntry(XmlAccountLoginModule.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, Map.of())}; }};
            new LoginContext("MTS", null, callbacks(login, password), config).login();
            SecurityAccount account = accountStore.byLogin(login);
            return UsernamePasswordAuthenticationToken.authenticated(account, null, RolePrivileges.authorities(account));
        } catch (LoginException exception) { throw new BadCredentialsException("Неправильный пароль или логин", exception); }
    }
    @Override public boolean supports(Class<?> type) { return UsernamePasswordAuthenticationToken.class.isAssignableFrom(type); }
    private CallbackHandler callbacks(String login, String password) { return callbacks -> { for (Callback callback : callbacks) { if (callback instanceof NameCallback name) name.setName(login); else if (callback instanceof PasswordCallback pass) pass.setPassword(password.toCharArray()); else throw new UnsupportedCallbackException(callback); } }; }
}
