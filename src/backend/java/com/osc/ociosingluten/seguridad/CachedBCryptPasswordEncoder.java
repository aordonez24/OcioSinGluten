package com.osc.ociosingluten.seguridad;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

public class CachedBCryptPasswordEncoder extends BCryptPasswordEncoder {
    static Map<String, CharSequence> cache = new HashMap<>();

    public CachedBCryptPasswordEncoder(){
        super();
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        CharSequence cachedMatch = cache.get(encodedPassword);

        if (cachedMatch != null && cachedMatch.equals(rawPassword)) {
            return true;
        }

        boolean result = super.matches(rawPassword, encodedPassword);

        if (result == true) {
            cache.put(encodedPassword, rawPassword);
        }

        return result;
    }
}
