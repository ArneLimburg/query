package com.ctp.cdi.query.criteria.predicate;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.SingularAttribute;

public class IsEmpty<E, V extends Collection<?>> extends NoValueBuilder<E, V> {
    
    public IsEmpty(SingularAttribute<? super E, V> att) {
        super(att);
    }

    @Override
    public List<Predicate> build(CriteriaBuilder builder, Path<E> path) {
        return Arrays.asList(builder.isEmpty(path.get(att)));
    }

}
