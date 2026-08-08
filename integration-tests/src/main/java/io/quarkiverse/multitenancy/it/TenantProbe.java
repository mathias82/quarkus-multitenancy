/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkiverse.multitenancy.it;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Minimal entity used by the integration suite to prove that Hibernate ORM routes a request to
 * the datasource selected by the resolved tenant id.
 */
@Entity
@Table(name = "tenant_probe")
public class TenantProbe {

    @Id
    private Long id;

    @Column(name = "tenant_label", nullable = false)
    private String tenantLabel;

    protected TenantProbe() {
    }

    public TenantProbe(Long id, String tenantLabel) {
        this.id = id;
        this.tenantLabel = tenantLabel;
    }

    public String getTenantLabel() {
        return tenantLabel;
    }
}
