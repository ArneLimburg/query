package com.ctp.cdi.query.home;

import static com.ctp.cdi.query.home.EntityMessage.HomeOperation.CREATE;
import static com.ctp.cdi.query.home.EntityMessage.HomeOperation.DELETE;
import static com.ctp.cdi.query.home.EntityMessage.HomeOperation.UPDATE;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.ctp.cdi.query.EntityDao;
import com.ctp.cdi.query.QueryResult;
import com.ctp.cdi.query.home.EntityMessage.HomeOperation;

public abstract class EntityHome<E, PK extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private PK id;
    private E entity;
    private Class<E> entityClass;

    private @Inject NavigationProvider navigation;
    private @Inject ConversationProvider conversation;
    private @Inject Event<EntityMessage> event;
    private @Inject PersistenceUtils utils;

    private E search;
    private int page;
    private int pageSize = DEFAULT_PAGE_SIZE;
    private long count;
    private List<E> pageItems;
    
    public Object create() {
        conversation.begin();
        return navigation.create();
    }
    
    public void retrieve() {
        if (navigation.isPostback()) {
            return;
        }
        if (conversation.isTransient()) {
            conversation.begin();
        }
        if (id == null) {
            entity = this.search;
        } else {
            entity = getEntityDao().findBy(getId());
        }
    }
    
    @SuppressWarnings("unchecked")
    public Object update() {
        conversation.end();
        HomeOperation ops = null;
        try {
            getEntityDao().save(entity);
            if (id == null) {
                ops = CREATE;
                event.fire(EntityMessage.created(entity));
                return navigation.search();
            } else {
                ops = UPDATE;
                event.fire(EntityMessage.updated(entity));
                PK entityId = (PK) utils.primaryKeyValue(entity);
                return navigation.view(entityId);
            }
        } catch (Exception e) {
            event.fire(EntityMessage.failed(entity, ops, e));
            return navigation.exception();
        }
    }
    
    public Object delete() {
        conversation.end();
        try {
            getEntityDao().remove(getEntityDao().findBy(getId()));
            getEntityDao().flush();
            return navigation.search();
        } catch (Exception e) {
            event.fire(EntityMessage.failed(entity, DELETE, e));
            return navigation.exception();
        }
    }
    
    public void search() {
        this.page = 0;
    }
    
    public void paginate() {
        QueryResult<E> query = getQueryResult();
        count = query.count();
        query.firstResult(page * pageSize)
             .maxResults(pageSize);
        pageItems = query.getResultList();
    }
    
    protected abstract EntityDao<E, PK> getEntityDao();
    
    protected abstract QueryResult<E> getQueryResult();
    
    @PostConstruct
    @SuppressWarnings("unchecked")
    void init() {
        try {
            entityClass = (Class<E>) utils.entityClass(getClass());
            search = entityClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed initializing EntityHome", e);
        }
    }
    
    // ------------------------------------------------------------------------
    // ACCESSORS AND MUTATORS
    // ------------------------------------------------------------------------
    
    public PK getId() {
        return id;
    }

    public void setId(PK id) {
        this.id = id;
    }

    public E getEntity() {
        return entity;
    }

    public void setEntity(E entity) {
        this.entity = entity;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public E getSearch() {
        return search;
    }

    public void setSearch(E search) {
        this.search = search;
    }

    public long getCount() {
        return count;
    }

    public List<E> getPageItems() {
        return pageItems;
    }

}
