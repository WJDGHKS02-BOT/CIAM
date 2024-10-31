package com.samsung.ciam.models;

import jakarta.persistence.*;

@Entity
@Table(name = "menu_access_control")
public class MenuAccessControl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_id")
    private String menuId;

    @Column(name = "menu_name")
    private String menuName;

    @Column(name = "ciam_admin_yn")
    private String ciamAdminYn;

    @Column(name = "channel_admin_yn")
    private String channelAdminYn;

    @Column(name = "channel_biz_admin_yn")
    private String channelBizAdminYn;

    @Column(name = "partner_admin_yn")
    private String partnerAdminYn;

    @Column(name = "temp_approver_yn")
    private String tempApproverYn;

    @Column(name = "general_user_yn")
    private String generalUserYn;

    @Column(name = "display_seq")
    private Long displaySeq;

    @Column(name = "main_menu_name")
    private String mainMenuName;

    @Column(name = "channel")
    private String channel;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getCiamAdminYn() {
        return ciamAdminYn;
    }

    public void setCiamAdminYn(String ciamAdminYn) {
        this.ciamAdminYn = ciamAdminYn;
    }

    public String getChannelAdminYn() {
        return channelAdminYn;
    }

    public void setChannelAdminYn(String channelAdminYn) {
        this.channelAdminYn = channelAdminYn;
    }

    public String getChannelBizAdminYn() {
        return channelBizAdminYn;
    }

    public void setChannelBizAdminYn(String channelBizAdminYn) {
        this.channelBizAdminYn = channelBizAdminYn;
    }

    public String getPartnerAdminYn() {
        return partnerAdminYn;
    }

    public void setPartnerAdminYn(String partnerAdminYn) {
        this.partnerAdminYn = partnerAdminYn;
    }

    public String getTempApproverYn() {
        return tempApproverYn;
    }

    public void setTempApproverYn(String tempApproverYn) {
        this.tempApproverYn = tempApproverYn;
    }

    public String getGeneralUserYn() {
        return generalUserYn;
    }

    public void setGeneralUserYn(String generalUserYn) {
        this.generalUserYn = generalUserYn;
    }

    public Long getDisplaySeq() {
        return displaySeq;
    }

    public void setDisplaySeq(Long displaySeq) {
        this.displaySeq = displaySeq;
    }

    public String getMainMenuName() {
        return mainMenuName;
    }

    public void setMainMenuName(String mainMenuName) {
        this.mainMenuName = mainMenuName;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}