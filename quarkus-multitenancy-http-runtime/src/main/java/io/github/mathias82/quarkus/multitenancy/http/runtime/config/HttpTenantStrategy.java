package io.github.mathias82.quarkus.multitenancy.http.runtime.config;

public enum HttpTenantStrategy {
    header,
    jwt,
    path,
    cookie
}
