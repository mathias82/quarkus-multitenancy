package io.quarkiverse.multitenancy.http.runtime.config;

public enum HttpTenantStrategy {
    header,
    jwt,
    path,
    cookie
}
