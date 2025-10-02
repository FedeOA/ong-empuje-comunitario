// consumer/src/main/java/com/ong/empuje/comunitario/consumer/model/DonationRequestId.java

package com.ong.empuje.comunitario.consumer.model;

import java.io.Serializable;
import java.util.Objects;

public class DonationRequestId implements Serializable {
    private Integer requestId;
    private Integer organizationId;

    // Default constructor
    public DonationRequestId() {}

    // Constructor
    public DonationRequestId(Integer requestId, Integer organizationId) {
        this.requestId = requestId;
        this.organizationId = organizationId;
    }

    // Getters and Setters
    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }

    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DonationRequestId that = (DonationRequestId) o;
        return Objects.equals(requestId, that.requestId) &&
               Objects.equals(organizationId, that.organizationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, organizationId);
    }
}