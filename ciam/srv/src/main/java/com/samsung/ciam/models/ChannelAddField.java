package com.samsung.ciam.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "channel_add_field")
public class ChannelAddField implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "channel_special_field_id_seq")
    @SequenceGenerator(name = "channel_special_field_id_seq", sequenceName = "channel_special_field_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "element_id", nullable = false)
    private String elementId;

    @Column(name = "element_name", nullable = false)
    private String elementName;

    @Column(name = "options", columnDefinition = "json")
    private String option;

    @Column(name = "channel")
    private String channel;

    @Column(name = "display_seq")
    private Long displaySeq;

    @Column(name = "required_yn")
    private String requiredYn;

    @Column(name = "add_type")
    private String addType;

    @Column(name = "half_type")
    private String halfType;

    @Column(name = "post_url")
    private String postUrl;

    @Column(name = "tab_type")
    private String tabType;

    @Column(name = "readonly")
    private String readonly;

    @Column(name = "disabled")
    private String disabled;

    @Column(name = "maxlength")
    private String maxlength;

    @Column(name = "cdc_data_field")
    private String cdcDataField;

    @Column(name = "btn_link_element_id")
    private String btnLinkElementId;

    @Column(name = "cdc_type")
    private String cdcType;

    @Column(name = "parameter_id")
    private String parameterId;

    @Column(name = "data_map_id")
    private String dataMapId;

    @Column(name = "array_yn")
    private String arrayYn;

    @Transient
    private List<Map<String, String>> options;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }


    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Long getDisplaySeq() {
        return displaySeq;
    }

    public void setDisplaySeq(Long displaySeq) {
        this.displaySeq = displaySeq;
    }

    public String getRequiredYn() {
        return requiredYn;
    }

    public void setRequiredYn(String requiredYn) {
        this.requiredYn = requiredYn;
    }

    public String getAddType() {
        return addType;
    }

    public void setAddType(String addType) {
        this.addType = addType;
    }

    public String getHalfSizeYn() {
        return halfType;
    }

    public void setHalfSizeYn(String halfSizeYn) {
        this.halfType = halfSizeYn;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public String getTabType() {
        return tabType;
    }

    public void setTabType(String tabType) {
        this.tabType = tabType;
    }

    public String getHalfType() {
        return halfType;
    }

    public void setHalfType(String halfType) {
        this.halfType = halfType;
    }

    public String getReadonly() {
        return readonly;
    }

    public void setReadonly(String readonly) {
        this.readonly = readonly;
    }

    public String getDisabled() {
        return disabled;
    }

    public void setDisabled(String disabled) {
        this.disabled = disabled;
    }

    public String getMaxlength() {
        return maxlength;
    }

    public void setMaxlength(String maxlength) {
        this.maxlength = maxlength;
    }

    public String getCdcDataField() {
        return cdcDataField;
    }

    public void setCdcDataField(String cdcDataField) {
        this.cdcDataField = cdcDataField;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public List<Map<String, String>> getOptions() {
        return options;
    }

    public void setOptions(List<Map<String, String>> options) {
        this.options = options;
    }

    public String getBtnLinkElementId() {
        return btnLinkElementId;
    }

    public void setBtnLinkElementId(String btnLinkElementId) {
        this.btnLinkElementId = btnLinkElementId;
    }

    public String getCdcType() {
        return cdcType;
    }

    public void setCdcType(String cdcType) {
        this.cdcType = cdcType;
    }

    public String getParameterId() {
        return parameterId;
    }

    public void setParameterId(String parameterId) {
        this.parameterId = parameterId;
    }

    public String getDataMapId() {
        return dataMapId;
    }

    public void setDataMapId(String dataMapId) {
        this.dataMapId = dataMapId;
    }

    public String getArrayYn() {
        return arrayYn;
    }

    public void setArrayYn(String arrayYn) {
        this.arrayYn = arrayYn;
    }
}
