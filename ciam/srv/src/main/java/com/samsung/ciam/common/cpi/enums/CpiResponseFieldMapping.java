package com.samsung.ciam.common.cpi.enums;

import java.util.AbstractMap;
import java.util.Map;

public enum CpiResponseFieldMapping {

    SBA(Map.ofEntries(
            new AbstractMap.SimpleEntry<>("bpid", "acctid"),
            new AbstractMap.SimpleEntry<>("vendorcode", "acctid"),
            new AbstractMap.SimpleEntry<>("orgName", "name_lo"),
            new AbstractMap.SimpleEntry<>("name", "name_lo"),
            new AbstractMap.SimpleEntry<>("country", "country"),
            new AbstractMap.SimpleEntry<>("state", "district_lo"),
            new AbstractMap.SimpleEntry<>("city", "city_lo"),
            new AbstractMap.SimpleEntry<>("street_address", "street_lo"),
            new AbstractMap.SimpleEntry<>("zip_code", "postalcode"),
            new AbstractMap.SimpleEntry<>("dunsno", "dunsno"),
            new AbstractMap.SimpleEntry<>("companycode", "companycode"),
            new AbstractMap.SimpleEntry<>("ciscode", "ciscode"),
            new AbstractMap.SimpleEntry<>("phonenumber1", "phonenumber1"),
            new AbstractMap.SimpleEntry<>("accountwebsite", "accountwebsite"),
            new AbstractMap.SimpleEntry<>("email", "email"),
            new AbstractMap.SimpleEntry<>("bizregno1", "bizregno1"),
            new AbstractMap.SimpleEntry<>("vatno", "vatno"),
            new AbstractMap.SimpleEntry<>("faxno", "faxno"),
            new AbstractMap.SimpleEntry<>("industry_type", "industrytype1,industrytype2"),
            new AbstractMap.SimpleEntry<>("bizregno2", "bizregno2"),
            new AbstractMap.SimpleEntry<>("approverid", "approverid"),
            new AbstractMap.SimpleEntry<>("createdate", "createdate"),
            new AbstractMap.SimpleEntry<>("regch", "regch"),
            new AbstractMap.SimpleEntry<>("identifystatus", "identifystatus"),
            new AbstractMap.SimpleEntry<>("approvedate", "approvedate"),
            new AbstractMap.SimpleEntry<>("accountowner", "accountowner"),
            new AbstractMap.SimpleEntry<>("rank", "rank"),
            new AbstractMap.SimpleEntry<>("validstatus", "validstatus"),
            new AbstractMap.SimpleEntry<>("changerid", "changerid"),
            new AbstractMap.SimpleEntry<>("phonenumber2", "phonenumber2"),
            new AbstractMap.SimpleEntry<>("statuschgreason", "statuschgreason"),
            new AbstractMap.SimpleEntry<>("creatorid", "creatorid"),
            new AbstractMap.SimpleEntry<>("changedate", "changedate"),
            new AbstractMap.SimpleEntry<>("closeyn", "closeyn")
    )),
    TYPE_B(Map.ofEntries(
            new AbstractMap.SimpleEntry<>("bpid", "accountId"),
            new AbstractMap.SimpleEntry<>("orgName", "organizationName"),
            new AbstractMap.SimpleEntry<>("country", "countryCode"),
            new AbstractMap.SimpleEntry<>("state", "state"),
            new AbstractMap.SimpleEntry<>("city", "city"),
            new AbstractMap.SimpleEntry<>("street_address", "street"),
            new AbstractMap.SimpleEntry<>("zip_code", "postalCode"),
            new AbstractMap.SimpleEntry<>("dunsno", "dunsNumber"),
            new AbstractMap.SimpleEntry<>("companycode", "companyCode"),
            new AbstractMap.SimpleEntry<>("ciscode", "cisCode"),
            new AbstractMap.SimpleEntry<>("phonenumber1", "phone1"),
            new AbstractMap.SimpleEntry<>("accountwebsite", "website"),
            new AbstractMap.SimpleEntry<>("email", "emailAddress"),
            new AbstractMap.SimpleEntry<>("bizregno1", "businessRegNumber1"),
            new AbstractMap.SimpleEntry<>("vatno", "vatNumber"),
            new AbstractMap.SimpleEntry<>("faxno", "faxNumber"),
            new AbstractMap.SimpleEntry<>("industry_type", "industryType"),
            new AbstractMap.SimpleEntry<>("bizregno2", "businessRegNumber2"),
            new AbstractMap.SimpleEntry<>("approverid", "approverId"),
            new AbstractMap.SimpleEntry<>("createdate", "createDate"),
            new AbstractMap.SimpleEntry<>("regch", "registrationChannel"),
            new AbstractMap.SimpleEntry<>("identifystatus", "identityStatus"),
            new AbstractMap.SimpleEntry<>("approvedate", "approvalDate"),
            new AbstractMap.SimpleEntry<>("accountowner", "accountOwner"),
            new AbstractMap.SimpleEntry<>("rank", "accountRank"),
            new AbstractMap.SimpleEntry<>("validstatus", "validationStatus"),
            new AbstractMap.SimpleEntry<>("changerid", "changerId"),
            new AbstractMap.SimpleEntry<>("phonenumber2", "phone2"),
            new AbstractMap.SimpleEntry<>("statuschgreason", "statusChangeReason"),
            new AbstractMap.SimpleEntry<>("creatorid", "creatorId"),
            new AbstractMap.SimpleEntry<>("changedate", "changeDate"),
            new AbstractMap.SimpleEntry<>("closeyn", "closeYn")
    ));

    private final Map<String, String> fieldMap;

    CpiResponseFieldMapping(Map<String, String> fieldMap) {
        this.fieldMap = fieldMap;
    }

    public Map<String, String> getFieldMap() {
        return fieldMap;
    }

    public static CpiResponseFieldMapping fromString(String value) {
        return CpiResponseFieldMapping.valueOf(value.toUpperCase());
    }
}
