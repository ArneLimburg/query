package org.apache.deltaspike.query.impl.meta;

import static org.apache.deltaspike.query.impl.util.QueryUtils.isNotEmpty;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.deltaspike.query.api.Query;
import org.apache.deltaspike.query.impl.builder.MethodExpressionException;
import org.apache.deltaspike.query.impl.builder.part.QueryRoot;
import org.apache.deltaspike.query.impl.builder.result.QueryProcessor;
import org.apache.deltaspike.query.impl.builder.result.QueryProcessorFactory;


/**
 * Stores information about a specific method of a DAO:
 * <ul>
 *     <li>The reference to the Method reflection object</li>
 *     <li>Whether this method delegates, is annotated or is parsed</li>
 *     <li>A reference to the parent DAO</li>
 *     <li>For parsed DAO methods, also the JPQL string is cached</li>
 * </ul>
 * 
 * @author thomashug
 */
public class DaoMethod {

    private final Method method;
    private final MethodType methodType;
    private final DaoComponent dao;
    private final QueryRoot queryRoot;
    private final QueryProcessor queryProcessor;
    
    public DaoMethod(Method method, DaoComponent dao) {
        this.method = method;
        this.dao = dao;
        this.methodType  = extractMethodType();
        this.queryRoot = initQueryRoot();
        this.queryProcessor = QueryProcessorFactory.newInstance(method).build();
    }
    
    public boolean returns(Class<?> returnType) {
        return returnType.equals(method.getReturnType());
    }
    
    private MethodType extractMethodType() {
        if (isAnnotated()) {
            return MethodType.ANNOTATED;
        }
        if (isMethodExpression()) {
            return MethodType.PARSE;
        }        
        return MethodType.DELEGATE;
    }
    
    private QueryRoot initQueryRoot() {
        if (methodType == MethodType.PARSE) {
            return QueryRoot.create(method.getName(), dao);
        }
        return QueryRoot.UNKNOWN_ROOT;
    }
    
    private boolean isAnnotated() {
        if (method.isAnnotationPresent(Query.class)) {
            Query query = method.getAnnotation(Query.class);
            return isValid(query);
        }
        return false;
    }

    private boolean isValid(Query query) {
        return isNotEmpty(query.value()) || isNotEmpty(query.named()) || isNotEmpty(query.sql());
    }
    
    private boolean isMethodExpression() {
        if (!Modifier.isAbstract(method.getModifiers())) {
            return false;
        }
        try {
            QueryRoot.create(method.getName(), dao);
            return true;
        } catch (MethodExpressionException e) {
            return false;
        }
    }

    public MethodType getMethodType() {
        return methodType;
    }

    public DaoComponent getDao() {
        return dao;
    }

    public QueryRoot getQueryRoot() {
        return queryRoot;
    }

    public QueryProcessor getQueryProcessor() {
        return queryProcessor;
    }

}