package com.samsung.ciam.models;

import jakarta.persistence.*;

@Entity
@Table(name = "channel_ad_check")
public class ChannelAdCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel")
    private String channel;

    @Column(name = "ad_check_yn")
    private String adCheckYn;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getAdCheckYn() {
        return adCheckYn;
    }

    public void setAdCheckYn(String adCheckYn) {
        this.adCheckYn = adCheckYn;
    }
}
