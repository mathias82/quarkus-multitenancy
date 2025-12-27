package io.github.mathias82.quarkus.multitenancy.orm.runtime.filter;

import io.github.mathias82.quarkus.multitenancy.core.runtime.context.TenantContext;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrmTenantHeaderFilterTest {

    @Test
    void setsTenantFromHeader() {
        OrmTenantHeaderFilter filter = new OrmTenantHeaderFilter();

        TestTenantContext tenantContext = new TestTenantContext();
        filter.tenantContext = tenantContext;

        filter.filter(dummyRequestWithTenant("tenant2"));

        assertEquals(
                "tenant2",
                tenantContext.getTenantId().orElseThrow()
        );
    }

    @Test
    void throwsExceptionWhenTenantHeaderMissing() {
        OrmTenantHeaderFilter filter = new OrmTenantHeaderFilter();
        filter.tenantContext = new TestTenantContext();

        WebApplicationException ex =
                assertThrows(WebApplicationException.class,
                        () -> filter.filter(dummyRequestWithoutTenant()));

        assertEquals(400, ex.getResponse().getStatus());
    }


    private static ContainerRequestContext dummyRequestWithTenant(String tenant) {
        return (ContainerRequestContext) Proxy.newProxyInstance(
                ContainerRequestContext.class.getClassLoader(),
                new Class[]{ContainerRequestContext.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getHeaderString" -> "X-Tenant".equals(args[0]) ? tenant : null;
                    case "getMethod" -> "GET";
                    case "getUriInfo" -> dummyUriInfo();
                    default -> null;
                }
        );
    }

    private static ContainerRequestContext dummyRequestWithoutTenant() {
        return (ContainerRequestContext) Proxy.newProxyInstance(
                ContainerRequestContext.class.getClassLoader(),
                new Class[]{ContainerRequestContext.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getHeaderString" -> null;
                    case "getMethod" -> "GET";
                    case "getUriInfo" -> dummyUriInfo();
                    default -> null;
                }
        );
    }

    private static jakarta.ws.rs.core.UriInfo dummyUriInfo() {
        return (jakarta.ws.rs.core.UriInfo) Proxy.newProxyInstance(
                jakarta.ws.rs.core.UriInfo.class.getClassLoader(),
                new Class[]{jakarta.ws.rs.core.UriInfo.class},
                (proxy, method, args) -> {
                    if ("getRequestUri".equals(method.getName())) {
                        return java.net.URI.create("http://localhost/test");
                    }
                    return null;
                }
        );
    }


    static class TestTenantContext implements TenantContext {

        private String tenant;

        @Override
        public Optional<String> getTenantId() {
            return Optional.ofNullable(tenant);
        }

        @Override
        public void setTenantId(String tenantId) {
            this.tenant = tenantId;
        }

        @Override
        public void clear() {
            this.tenant = null;
        }
    }
}
