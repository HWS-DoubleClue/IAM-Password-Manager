package com.doubleclue.dcem.core.logic;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import com.microsoft.graph.authentication.IAuthenticationProvider;

public class MsGraphSimpleAuthProvider implements IAuthenticationProvider {

    private String accessToken = null;

    public MsGraphSimpleAuthProvider(String accessToken) {

        this.accessToken = accessToken;

    }

    @Override
    public CompletableFuture<String> getAuthorizationTokenAsync(URL requestUrl) {

        return CompletableFuture.completedFuture(accessToken);

    }

}