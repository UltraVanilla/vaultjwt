package lordpipe.vaultjwt;

import io.jsonwebtoken.Claims;

import java.util.Date;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

public class TokenGenerator {
    private VaultJwt plugin;
    public TokenGenerator(VaultJwt instance) {
        plugin = instance;
    }
    public String genTokenWithClaims(Claims claims, String type) {
        Date now = new Date();
        claims.setExpiration(new Date(now.getTime() + plugin.config.getInt("expireAfter") * 1000));

        JwtBuilder builder = Jwts.builder()
            .setClaims(claims)
            .signWith(plugin.privKey);

        if (type == "bulk") return plugin.config.getString("bulkTokenUrl").replace("???", builder.compact());
        else if (type == "login") return plugin.config.getString("authTokenUrl").replace("???", builder.compact());
        else return builder.compact();
    }
}
