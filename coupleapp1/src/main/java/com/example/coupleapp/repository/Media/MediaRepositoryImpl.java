package com.example.coupleapp.repository.Media;

import com.example.coupleapp.entity.MediaEntity;
import com.example.coupleapp.entity.MemberEntity;
import com.example.coupleapp.entity.QMediaEntity;
import com.example.coupleapp.entity.QMemberEntity;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.util.List;

public class MediaRepositoryImpl implements MediaRepositoryCustom{
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MediaRepositoryImpl(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    QMediaEntity media = new QMediaEntity("media");

    @Override
    public Page<Tuple> findMediaList(MemberEntity member, Pageable pageable) {
        QueryResults<Tuple> results = queryFactory
                .select(media.id,media.media_url,media.created_at)
                .from(media)
                .where(media.my_phone_number.eq(member.getMy_phone_number())
                                .or(media.my_phone_number.eq(member.getYour_phone_number())))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        return new PageImpl<>(results.getResults(),pageable, results.getTotal());
    }

}

