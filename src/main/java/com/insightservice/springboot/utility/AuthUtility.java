package com.insightservice.springboot.utility;

import java.util.HashMap;

public class AuthUtility {
    private HashMap<String, String> userTokenMap;

    private AuthUtility() {
        this.userTokenMap = new HashMap<>();
    }

    private static AuthUtility instance;

    public static AuthUtility getInstance() {
        if(AuthUtility.instance == null) {
            AuthUtility.instance = new AuthUtility();
        }
        return AuthUtility.instance;
    }

    public String getToken(String username) {
        return this.userTokenMap.get(username);
    }

    public void setToken(String username, String token) {
        this.userTokenMap.put(username, token);
    }
}
