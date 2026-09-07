package me.zinch.itmo.mts.security.jaas;

import me.zinch.itmo.mts.security.SecurityAccount;
import org.mindrot.jbcrypt.BCrypt;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.spi.LoginModule;
import java.io.InputStream;
import java.security.Principal;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import java.util.UUID;

public class XmlAccountLoginModule implements LoginModule {
    private Subject subject; private CallbackHandler callbackHandler; private SecurityAccount account;
    @Override public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) { this.subject = subject; this.callbackHandler = callbackHandler; }
    @Override public boolean login() throws javax.security.auth.login.LoginException {
        var name = new NameCallback("login"); var password = new PasswordCallback("password", false);
        try { callbackHandler.handle(new Callback[]{name, password}); account = load(name.getName()); }
        catch (Exception exception) { throw new FailedLoginException("Не удалось проверить учётную запись"); }
        if (account == null || !BCrypt.checkpw(new String(password.getPassword()), account.passwordHash())) throw new FailedLoginException("Неправильный пароль или логин");
        return true;
    }
    @Override public boolean commit() { subject.getPrincipals().add(new AccountPrincipal(account)); return true; }
    @Override public boolean abort() { account = null; return true; }
    @Override public boolean logout() { subject.getPrincipals().clear(); return true; }
    private SecurityAccount load(String login) throws Exception {
        try (InputStream input = XmlAccountLoginModule.class.getClassLoader().getResourceAsStream("security/accounts.xml")) {
            var nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input).getDocumentElement().getElementsByTagName("account");
            for (int i = 0; i < nodes.getLength(); i++) { Element a = (Element) nodes.item(i); if (login.equals(a.getAttribute("login"))) return new SecurityAccount(UUID.fromString(a.getAttribute("id")), login, a.getAttribute("passwordHash"), a.getAttribute("name"), me.zinch.itmo.mts.domain.enums.UserRole.valueOf(a.getAttribute("role"))); }
            return null;
        }
    }
    public record AccountPrincipal(SecurityAccount account) implements Principal { @Override public String getName() { return account.login(); } }
}
