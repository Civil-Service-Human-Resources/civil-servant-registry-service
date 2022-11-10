package uk.gov.cshr.civilservant.controller.models;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public enum OrganisationalUnitOrderingKey {
    NAME("name"),
    FORMATTED_NAME("formattedName");

    private String value;

    private OrganisationalUnitOrderingKey(String value) {
        this.value = value;
    }

    public static OrganisationalUnitOrderingKey fromValue(String value) {
        for (OrganisationalUnitOrderingKey orderingParam : values()) {
            if (orderingParam.value.equalsIgnoreCase(value)) {
                return orderingParam;
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("orderBy must be one of %s", EnumUtils.getEnumList(OrganisationalUnitOrderingKey.class)));
    }
}
