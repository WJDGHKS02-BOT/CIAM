package com.samsung.ciam.services;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.querydsl.jpa.impl.JPAQueryFactory;

import com.samsung.ciam.models.*;
import com.samsung.ciam.repositories.AuthorizedSubsidiaryRepository;


@Service
public class AuthorizedSubsidiaryService {
    @Autowired
    AuthorizedSubsidiaryRepository authorizedSubsidiaryRepository;

    @PersistenceContext
    EntityManager entityManager;

    public List<String> getCdcUidList(String channelName, String subsidiary, int limit) {
        QAuthorizedSubsidiary qAuthorizedSubsidiary = QAuthorizedSubsidiary.authorizedSubsidiary;
        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(entityManager);

        List<String> cdcUidList = jpaQueryFactory.select(qAuthorizedSubsidiary.cdcUid)
                            .from(qAuthorizedSubsidiary)
                            .where(qAuthorizedSubsidiary.channel.eq(channelName)
                                .and(qAuthorizedSubsidiary.authorizedSubsidiaryList.eq(subsidiary))
                            )
                            .orderBy(qAuthorizedSubsidiary.updatedAt.desc())
                            .limit(Long.valueOf(limit))
                            .fetch();

        return cdcUidList;
    }
}
