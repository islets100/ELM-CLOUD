package team.tjusw.elm.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

	private final SecretKey secretKey;
	private final long tokenValiditySeconds;
	private final long tokenValidityRememberMeSeconds;

	public JwtTokenProvider(
			@Value("${jwt.secret:elm-cloud-user-service-dev-secret-key-please-change-1234567890-abcdefghijk}") String secret,
			@Value("${jwt.token-validity-in-seconds:86400}") long tokenValiditySeconds,
			@Value("${jwt.token-validity-in-seconds-for-remember-me:2592000}") long tokenValidityRememberMeSeconds) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.tokenValiditySeconds = tokenValiditySeconds;
		this.tokenValidityRememberMeSeconds = tokenValidityRememberMeSeconds;
	}

	public String createToken(String userId, boolean rememberMe, Integer userType) {
		long now = Instant.now().getEpochSecond();
		long ttl = rememberMe ? tokenValidityRememberMeSeconds : tokenValiditySeconds;
		String auth = userType != null && userType == 1 ? "BUSINESS" : ("admin".equals(userId) ? "ADMIN" : "USER");
		return Jwts.builder().setSubject(userId).claim("auth", auth).setIssuedAt(new Date(now * 1000))
				.setExpiration(new Date((now + ttl) * 1000)).signWith(secretKey, SignatureAlgorithm.HS512).compact();
	}

	public String getUserIdFromToken(String token) {
		try {
			Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
			return claims.getSubject();
		} catch (JwtException | IllegalArgumentException e) {
			return null;
		}
	}
}
