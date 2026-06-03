package io.quarkiverse.multitenancy.http.runtime.filter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import org.junit.jupiter.api.Test;

import io.quarkiverse.multitenancy.core.runtime.api.TenantResolution;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolutionContext;
import io.quarkiverse.multitenancy.core.runtime.api.TenantResolver;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

/**
 * End-to-end regression coverage for {@link TenantFilter}'s dispatch matrix.
 *
 * <p>
 * Locks down the three-state {@link TenantResolution} contract so future
 * changes cannot reintroduce the silent-downgrade hole the PR closed:
 *
 * <ul>
 * <li>A custom resolver runs before built-ins and stops the chain when it
 * returns {@code Resolved}.</li>
 * <li>A custom resolver that returns {@code NotApplicable} lets built-ins
 * keep running.</li>
 * <li>A {@code Rejected} outcome aborts with HTTP 401 — the filter must not
 * fall back to {@code defaultTenant}.</li>
 * <li>A fully unmatched chain falls back to {@code defaultTenant}.</li>
 * <li>The built-in chain honours {@code quarkus.multi-tenant.http.strategy}
 * order.</li>
 * </ul>
 */
@QuarkusTest
@TestProfile(TenantFilterDispatchTest.HeaderCookieStrategyProfile.class)
class TenantFilterDispatchTest {

    @Test
    void customResolverResolvesBeforeBuiltins() {
        // The custom resolver fires on X-Custom-Tenant; with that header set,
        // the built-in chain (header, cookie) never runs.
        given()
                .header("X-Custom-Tenant", "from-custom")
                .header("X-Tenant", "from-builtin-header")
                .when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(is("Optional[from-custom]"));
    }

    @Test
    void notApplicableCustomLetsBuiltinsRun() {
        // No X-Custom-Tenant present → custom returns NotApplicable → built-in
        // header resolver fires.
        given()
                .header("X-Tenant", "from-builtin-header")
                .when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(is("Optional[from-builtin-header]"));
    }

    @Test
    void rejectedCustomAbortsWithUnauthorized() {
        // The custom resolver rejects whenever X-Bad-Tenant is present.
        // Filter must abort with 401 and must not fall through to the
        // default tenant even though X-Tenant would otherwise have resolved.
        given()
                .header("X-Bad-Tenant", "any-value")
                .header("X-Tenant", "would-have-resolved")
                .when()
                .get("/tenant")
                .then()
                .statusCode(401);
    }

    @Test
    void allNotApplicableFallsBackToDefaultTenant() {
        // Neither custom resolver, nor any built-in, matches → default tenant.
        given()
                .when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(containsString("public"));
    }

    @Test
    void builtinStrategyOrderIsHonoured() {
        // Strategy is "header,cookie". Header wins when both are present.
        given()
                .header("X-Tenant", "header-wins")
                .cookie("tenant_cookie", "cookie-loses")
                .when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(is("Optional[header-wins]"));
    }

    @Test
    void cookieRunsWhenHeaderNotApplicable() {
        given()
                .cookie("tenant_cookie", "cookie-fallback")
                .when()
                .get("/tenant")
                .then()
                .statusCode(200)
                .body(is("Optional[cookie-fallback]"));
    }

    /**
     * Restricts the built-in chain to {@code header,cookie} so the test does
     * not need to configure JWT verification. JWT is exercised end-to-end in
     * {@code JwtTenantResolverTest}.
     */
    public static class HeaderCookieStrategyProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "quarkus.multi-tenant.http.strategy", "header,cookie");
        }
    }

    /**
     * Custom resolver used by the tests. Returns:
     *
     * <ul>
     * <li>{@code Rejected} when {@code X-Bad-Tenant} is present (locks down
     * the rejection path),</li>
     * <li>{@code Resolved(value)} when {@code X-Custom-Tenant} is present,</li>
     * <li>{@code NotApplicable} otherwise.</li>
     * </ul>
     */
    @ApplicationScoped
    public static class CustomTenantResolver implements TenantResolver {

        @Override
        public TenantResolution resolve(TenantResolutionContext context) {
            return context.get(jakarta.ws.rs.container.ContainerRequestContext.class)
                    .map(req -> {
                        if (req.getHeaderString("X-Bad-Tenant") != null) {
                            return TenantResolution.rejected("X-Bad-Tenant header is not allowed");
                        }
                        String custom = req.getHeaderString("X-Custom-Tenant");
                        if (custom != null && !custom.isBlank()) {
                            return TenantResolution.resolved(custom);
                        }
                        return TenantResolution.notApplicable();
                    })
                    .orElseGet(TenantResolution::notApplicable);
        }
    }
}
