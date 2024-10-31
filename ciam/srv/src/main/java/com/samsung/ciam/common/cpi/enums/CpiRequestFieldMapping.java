package com.samsung.ciam.common.cpi.enums;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public enum CpiRequestFieldMapping {

    SBA(Map.ofEntries(
            new AbstractMap.SimpleEntry<>("name", "name_lo"),
            new AbstractMap.SimpleEntry<>("filter_country", "country"),
            new AbstractMap.SimpleEntry<>("filter_bizregno1", "bizregno1"),
            new AbstractMap.SimpleEntry<>("filter_vatno", "vatno"),
            new AbstractMap.SimpleEntry<>("filter_dunsno", "dunsno"),
            new AbstractMap.SimpleEntry<>("filter_ciscode", "ciscode"),
            new AbstractMap.SimpleEntry<>("filter_accountwebsite", "accountwebsite"),
            new AbstractMap.SimpleEntry<>("filter_email", "email"),
            new AbstractMap.SimpleEntry<>("filter_phonenumber1", "phonenumber1"),
            new AbstractMap.SimpleEntry<>("filter_faxno", "faxno"),
            new AbstractMap.SimpleEntry<>("filter_street_address", "street_lo"),
            new AbstractMap.SimpleEntry<>("filter_city", "city_lo"),
            new AbstractMap.SimpleEntry<>("filter_state", "district_lo"),
            new AbstractMap.SimpleEntry<>("filter_zip_code", "postalcode")
    )),
    TYPE_B(Map.ofEntries(
            new AbstractMap.SimpleEntry<>("filter_Name", "organizationName"),
            new AbstractMap.SimpleEntry<>("filter_country", "countryCode"),
            new AbstractMap.SimpleEntry<>("filter_bizregno1", "businessRegNumber1"),
            new AbstractMap.SimpleEntry<>("filter_vatno", "vatNumber"),
            new AbstractMap.SimpleEntry<>("filter_dunsno", "dunsNumber"),
            new AbstractMap.SimpleEntry<>("filter_ciscode", "cisCode"),
            new AbstractMap.SimpleEntry<>("filter_accountwebsite", "website"),
            new AbstractMap.SimpleEntry<>("filter_email", "emailAddress"),
            new AbstractMap.SimpleEntry<>("filter_phonenumber1", "phone1"),
            new AbstractMap.SimpleEntry<>("filter_faxno", "faxNumber"),
            new AbstractMap.SimpleEntry<>("filter_street_address", "street"),
            new AbstractMap.SimpleEntry<>("filter_city", "city"),
            new AbstractMap.SimpleEntry<>("filter_state", "state"),
            new AbstractMap.SimpleEntry<>("filter_zip_code", "postalCode")
    ));

    private final Map<String, String> fieldMap;

    CpiRequestFieldMapping(Map<String, String> fieldMap) {
        this.fieldMap = fieldMap;
    }

    public Map<String, String> getFieldMap() {
        return fieldMap;
    }

    public static CpiRequestFieldMapping fromString(String value) {
        return CpiRequestFieldMapping.valueOf(value.toUpperCase());
    }

    public Map<String, Object> getDefaultValues() {
        Map<String, Object> defaultValues = new HashMap<>();
        switch (this) {
            case SBA:
                defaultValues.put("name_gl", "");
                defaultValues.put("getmaxrank_recode", false);
                break;
            case TYPE_B:
                // Add any default values specific to TYPE_B if needed
                break;
        }
        return defaultValues;
    }
}
