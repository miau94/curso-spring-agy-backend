package com.andres.course.agy.springboot.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final String secretKey;
    private final long jwtExpiration;
    private final String cookieName;

    /**
     * Constructor que inyecta las propiedades de configuración del JWT y de la cookie desde application.properties.
     *
     * @param secretKey     Clave secreta en Base64 para firmar y verificar los tokens.
     * @param jwtExpiration Tiempo de expiración del token en milisegundos.
     * @param cookieName    Nombre de la cookie HttpOnly donde se almacenará el JWT.
     */
    public JwtService(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration}") long jwtExpiration,
            @Value("${jwt.cookie-name}") String cookieName) {
        this.secretKey = secretKey;
        this.jwtExpiration = jwtExpiration;
        this.cookieName = cookieName;
    }

    /**
     * Extrae el nombre de usuario (email) almacenado en el subject del token JWT.
     *
     * @param token Token JWT del cual extraer el usuario.
     * @return El email del usuario contenido en el token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae un claim específico del token JWT aplicando una función de resolución sobre todos los claims.
     *
     * @param token          Token JWT a analizar.
     * @param claimsResolver Función encargada de obtener el dato específico de los claims.
     * @param <T>            Tipo de dato del claim que se desea obtener.
     * @return El valor del claim solicitado.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Genera un token JWT para el usuario autenticado incluyendo sus roles como claim adicional.
     *
     * @param userDetails Detalles del usuario autenticado (email y autoridades/roles).
     * @return El token JWT firmado en formato String.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        extraClaims.put("roles", roles);
        return generateToken(extraClaims, userDetails);
    }

    /**
     * Construye y firma un token JWT con claims personalizados, fecha de emisión, expiración y clave secreta.
     *
     * @param extraClaims Mapa con información adicional para incluir en el payload del token.
     * @param userDetails Detalles del usuario para establecer el subject del token.
     * @return El token JWT compactado y firmado.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Verifica si un token JWT es válido comprobando que pertenezca al usuario indicado y que no haya expirado.
     *
     * @param token       Token JWT a validar.
     * @param userDetails Detalles del usuario contra el cual comparar el subject del token.
     * @return true si el token es válido y vigente; false en caso contrario.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Comprueba si la fecha de expiración del token JWT es anterior a la fecha y hora actual.
     *
     * @param token Token JWT a verificar.
     * @return true si el token ya ha expirado; false si aún es válido.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Obtiene la fecha de expiración configurada dentro de los claims del token JWT.
     *
     * @param token Token JWT del cual extraer la fecha de expiración.
     * @return Objeto Date con el momento de expiración del token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parsea el token JWT, verifica su firma criptográfica con la clave secreta y devuelve todos sus claims.
     *
     * @param token Token JWT firmado.
     * @return Objeto Claims con todo el contenido (payload) del token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Decodifica la clave secreta en Base64 y genera la clave criptográfica HMAC-SHA para firmar/verificar el JWT.
     *
     * @return Clave secreta SecretKey lista para usar con el algoritmo HMAC.
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Crea una cookie segura con el atributo HttpOnly activado que almacena el token JWT generado.
     *
     * @param token Token JWT que se guardará como valor de la cookie.
     * @return Objeto ResponseCookie configurado con HttpOnly, expiración y política SameSite=Strict.
     */
    public ResponseCookie generateJwtCookie(String token) {
        return ResponseCookie.from(cookieName, token)
                .path("/")
                .maxAge(jwtExpiration / 1000)
                .httpOnly(true)
                .sameSite("Strict")
                .build();
    }

    /**
     * Genera una cookie vacía con tiempo de vida cero (maxAge = 0) para eliminar la cookie JWT en el navegador al cerrar sesión.
     *
     * @return Objeto ResponseCookie limpio para invalidar la cookie en el cliente.
     */
    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(cookieName, "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .sameSite("Strict")
                .build();
    }

    /**
     * Busca y extrae el valor del token JWT desde las cookies enviadas en la petición HTTP entrante.
     *
     * @param request Petición HTTP recibida por el servidor.
     * @return El valor del token JWT si la cookie existe, o null si no se encuentra.
     */
    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
