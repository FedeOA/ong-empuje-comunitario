// consumer/src/main/java/com/ong/empuje/comunitario/consumer/model/DonationRequestId.java

package com.ong.empuje.comunitario.consumer.model;

import java.io.Serializable;
import java.util.Objects;

public class DonationRequestId implements Serializable {
    private Integer requestId;
    private Integer organizationId;

    public DonationRequestId() {}

    public DonationRequestId(Integer requestId, Integer organizationId) {
        this.requestId = requestId;
        this.organizationId = organizationId;
    }

    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }

    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }
}