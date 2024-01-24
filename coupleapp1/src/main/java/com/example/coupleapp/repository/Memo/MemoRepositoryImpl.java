package com.example.coupleapp.repository.Memo;

import com.example.coupleapp.entity.MemberEntity;
import com.example.coupleapp.entity.QMemoEntity;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import javax.persistence.EntityManager;

public class MemoRepositoryImpl implements MemoRepositoryCustom{

    final private EntityManager em;
    final private JPAQueryFactory queryFactory;

    public MemoRepositoryImpl(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    QMemoEntity memo = new QMemoEntity("memo");

    @Override
    public Page<Tuple> findMemoList(MemberEntity member, Pageable pageable) {
        QueryResults<Tuple> results = queryFactory
                .select(memo.id,memo.memoContent,memo.created_at)
                .from(memo)
                .where(memo.myPhoneNumber.eq(member.getMy_phone_number())
                                .or(memo.myPhoneNumber.eq(member.getYour_phone_number())))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        return new PageImpl<>(results.getResults(),pageable, results.getTotal());
    }
}
