package me.zinch.itmo.mts.security;
import io.jsonwebtoken.Jwts; import io.jsonwebtoken.security.Keys; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service; import javax.crypto.SecretKey; import java.nio.charset.StandardCharsets; import java.util.Date; import java.util.UUID;
@Service public class JwtService { private final SecretKey key; private final long ttl;
 public JwtService(@Value("${app.security.jwt-secret}") String secret,@Value("${app.security.jwt-ttl-minutes}") long minutes){key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));ttl=minutes*60000;}
 public String issue(SecurityAccount a){Date n=new Date();return Jwts.builder().subject(a.id().toString()).issuedAt(n).expiration(new Date(n.getTime()+ttl)).signWith(key).compact();}
 public SecurityAccount parse(String token,XmlAccountStore store){return store.byId(UUID.fromString(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject()));}}
