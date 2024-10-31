package com.samsung.ciam.models;

import jakarta.persistence.*;

@Entity
@Table(name = "consent_languages")
public class ConsentLanguage {
    
    @Id
    private String id;

    @Column(name = "name_ko")
    private String nameKo;

    @Column(name = "name_en")
    private String nameEn;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNameKo() {
        return nameKo;
    }

    public void setNameKo(String nameKo) {
        this.nameKo = nameKo;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }
}
