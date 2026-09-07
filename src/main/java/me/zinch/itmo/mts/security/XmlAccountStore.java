package me.zinch.itmo.mts.security;

import me.zinch.itmo.mts.domain.enums.UserRole;
import me.zinch.itmo.mts.service.auth.UnauthorizedException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.List;
import java.util.UUID;

@Service
public class XmlAccountStore {
    private static final String ACCOUNTS_FILE = "security/accounts.xml";
    private final List<SecurityAccount> accounts = loadAccounts();

    public SecurityAccount byLogin(String login) {
        return accounts.stream().filter(account -> account.login().equals(login)).findFirst()
                .orElseThrow(() -> new UnauthorizedException("Неправильный пароль или логин"));
    }

    public SecurityAccount byId(UUID id) {
        return accounts.stream().filter(account -> account.id().equals(id)).findFirst()
                .orElseThrow(() -> new UnauthorizedException("Учётная запись не найдена"));
    }

    public List<SecurityAccount> all() { return accounts; }

    private List<SecurityAccount> loadAccounts() {
        try (var input = new ClassPathResource(ACCOUNTS_FILE).getInputStream()) {
            var document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
            var nodes = document.getDocumentElement().getElementsByTagName("account");
            var result = new java.util.ArrayList<SecurityAccount>();
            for (int i = 0; i < nodes.getLength(); i++) {
                Element account = (Element) nodes.item(i);
                result.add(new SecurityAccount(
                        UUID.fromString(account.getAttribute("id")), account.getAttribute("login"),
                        account.getAttribute("passwordHash"), account.getAttribute("name"),
                        UserRole.valueOf(account.getAttribute("role"))));
            }
            return List.copyOf(result);
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось прочитать XML-файл учётных записей", exception);
        }
    }
}
