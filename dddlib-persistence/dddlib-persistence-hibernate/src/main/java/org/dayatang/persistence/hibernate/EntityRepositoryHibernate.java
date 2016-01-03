package org.dayatang.persistence.hibernate;

import org.dayatang.domain.*;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 通用仓储接口的Hibernate实现。
 * <p> EntityRepositoryHibernate通过SessionProvider获取Session，以保证在当前线程和事务中
 * 对数据库的多次访问都是由同一个Session来进行，防止出现“会话已关闭”异常。
 * @author yyang (<a href="mailto:gdyangyu@gmail.com">gdyangyu@gmail.com</a>)
 */
@SuppressWarnings({"unchecked"})
public class EntityRepositoryHibernate implements EntityRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityRepositoryHibernate.class);

    private final SessionProvider sessionProvider;

    public EntityRepositoryHibernate() {
        sessionProvider = new SessionProvider();
    }

    public EntityRepositoryHibernate(SessionFactory sessionFactory) {
        this.sessionProvider = new SessionProvider(sessionFactory);
    }

    public EntityRepositoryHibernate(Session session) {
        this.sessionProvider = new SessionProvider(session);
    }


    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#save(org.dayatang.domain.Entity)
     */
    @Override
    public <T extends Entity> T save(T entity) {
        if (entity.notExisted()) {
            getSession().save(entity);
            LOGGER.info("create a entity: " + entity.getClass() + "/" + entity.getId() + ".");
            return entity;
        }
        getSession().update(entity);
        LOGGER.info("update a entity: " + entity.getClass() + "/" + entity.getId() + ".");
        return entity;
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#remove(org.dayatang.domain.Entity)
     */
    @Override
    public void remove(Entity entity) {
        getSession().delete(entity);
        LOGGER.info("remove a entity: " + entity.getClass() + "/" + entity.getId() + ".");
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#exists(java.lang.Class, java.io.Serializable)
     */
    @Override
    public <T extends Entity> boolean exists(final Class<T> clazz, final Serializable id) {
        return get(clazz, id) != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dayatang.domain.EntityRepository#get(java.io.Serializable)
     */
    @Override
    public <T extends Entity> T get(final Class<T> clazz, final Serializable id) {
        return (T) getSession().get(clazz, id);
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#load(java.lang.Class, java.io.Serializable)
     */
    @Override
    public <T extends Entity> T load(final Class<T> clazz, final Serializable id) {
        return (T) getSession().load(clazz, id);
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#getUnmodified(java.lang.Class, org.dayatang.domain.Entity)
     */
    @Override
    public <T extends Entity> T getUnmodified(Class<T> clazz, T entity) {
        getSession().evict(entity);
        return get(clazz, entity.getId());
    }

    @Override
    public <T extends Entity> T getByBusinessKeys(Class<T> clazz, NamedParameters keyValues) {
        List<T> results = findByProperties(clazz, keyValues);
        return results.isEmpty() ? null : results.get(0);
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#findAll(java.lang.Class)
     */
    @Override
    public <T extends Entity> List<T> findAll(Class<T> clazz) {
        return getSession().createCriteria(clazz).list();
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#createCriteriaQuery(java.lang.Class)
     */
    @Override
    public <T extends Entity> CriteriaQuery createCriteriaQuery(Class<T> entityClass) {
        return new CriteriaQuery(this, entityClass);
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#find(org.dayatang.domain.CriteriaQuery)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public <T> List<T> find(CriteriaQuery criteriaQuery) {
        Query query = getSession().createQuery(criteriaQuery.getQueryString());
        processQuery(query, criteriaQuery.getParameters(),
                criteriaQuery.getFirstResult(), criteriaQuery.getMaxResults());
        return query.list();
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#getSingleResult(org.dayatang.domain.CriteriaQuery)
     */
    @Override
    public <T> T getSingleResult(CriteriaQuery dddQuery) {
        List<T> results = find(dddQuery);
        return results == null || results.isEmpty() ? null : results.get(0);
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#createJpqlQuery(java.lang.String)
     */
    @Override
    public JpqlQuery createJpqlQuery(String jpql) {
        return new JpqlQuery(this, jpql);
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#find(org.dayatang.domain.JpqlQuery)
     */
    @Override
    public <T> List<T> find(JpqlQuery jpqlQuery) {
        return getQuery(jpqlQuery).list();
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#getSingleResult(org.dayatang.domain.JpqlQuery)
     */
    @Override
    public <T> T getSingleResult(JpqlQuery jpqlQuery) {
        return (T) getQuery(jpqlQuery).uniqueResult();
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#executeUpdate(org.dayatang.domain.JpqlQuery)
     */
    @Override
    public int executeUpdate(JpqlQuery jpqlQuery) {
        return getQuery(jpqlQuery).executeUpdate();
    }

    private Query getQuery(JpqlQuery jpqlQuery) {
        Query query = getSession().createQuery(jpqlQuery.getJpql());
        processQuery(query, jpqlQuery);
        return query;
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#createNamedQuery(java.lang.String)
     */
    @Override
    public NamedQuery createNamedQuery(String queryName) {
        return new NamedQuery(this, queryName);
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#find(org.dayatang.domain.NamedQuery)
     */
    @Override
    public <T> List<T> find(NamedQuery namedQuery) {
        return getQuery(namedQuery).list();
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#getSingleResult(org.dayatang.domain.NamedQuery)
     */
    @Override
    public <T> T getSingleResult(NamedQuery namedQuery) {
        return (T) getQuery(namedQuery).uniqueResult();
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#executeUpdate(org.dayatang.domain.NamedQuery)
     */
    @Override
    public int executeUpdate(NamedQuery namedQuery) {
        return getQuery(namedQuery).executeUpdate();
    }

    private Query getQuery(NamedQuery namedQuery) {
        Query query = getSession().getNamedQuery(namedQuery.getQueryName());
        processQuery(query, namedQuery);
        return query;
    }

    @Override
    public SqlQuery createSqlQuery(String sql) {
        return new SqlQuery(this, sql);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <T> List<T> find(SqlQuery sqlQuery) {
        return getQuery(sqlQuery).list();
    }

    @Override
    public <T> T getSingleResult(SqlQuery sqlQuery) {
        return (T) getQuery(sqlQuery).uniqueResult();
    }

    @Override
    public int executeUpdate(SqlQuery sqlQuery) {
        return getQuery(sqlQuery).executeUpdate();
    }

    private Query getQuery(SqlQuery sqlQuery) {
        SQLQuery query = getSession().createSQLQuery(sqlQuery.getSql());
        processQuery(query, sqlQuery);
        Class resultEntityClass = sqlQuery.getResultEntityClass();
        if (resultEntityClass != null) {
            query.addEntity(resultEntityClass);
        }
        return query;
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#findByExample(org.dayatang.domain.Entity, org.dayatang.domain.ExampleSettings)
     */
    @Override
    public <T extends Entity, E extends T> List<T> findByExample(final E example, final ExampleSettings<T> settings) {
        Example theExample = Example.create(example);
        if (settings.isLikeEnabled()) {
            theExample.enableLike(MatchMode.ANYWHERE);
        }
        if (settings.isIgnoreCaseEnabled()) {
            theExample.ignoreCase();
        }
        if (settings.isExcludeNone()) {
            theExample.excludeNone();
        }
        if (settings.isExcludeZeroes()) {
            theExample.excludeZeroes();
        }
        for (String propName : settings.getExcludedProperties()) {
            theExample.excludeProperty(propName);
        }
        return getSession().createCriteria(settings.getEntityClass()).add(theExample).list();
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#findByProperty(java.lang.Class, java.lang.String, java.lang.Object)
     */
    @Override
    public <T extends Entity> List<T> findByProperty(Class<T> clazz, String propertyName, Object propertyValue) {
        return find(new CriteriaQuery(this, clazz).eq(propertyName, propertyValue));
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#findByProperties(java.lang.Class, java.util.Map)
     */
    @Override
    public <T extends Entity> List<T> findByProperties(Class<T> clazz, NamedParameters properties) {
        CriteriaQuery criteriaQuery = new CriteriaQuery(this, clazz);
        for (Map.Entry<String, Object> each : properties.getParams().entrySet()) {
            criteriaQuery = criteriaQuery.eq(each.getKey(), each.getValue());
        }
        return find(criteriaQuery);
    }

    @Override
    public String getQueryStringOfNamedQuery(String queryName) {
        Query query = getSession().getNamedQuery(queryName);
        return query.getQueryString();
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#flush()
     */
    @Override
    public void flush() {
        getSession().flush();
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#refresh(org.dayatang.domain.Entity)
     */
    @Override
    public void refresh(Entity entity) {
        getSession().refresh(entity);
    }

    /*
     * (non-Javadoc)
     * @see org.dayatang.domain.EntityRepository#clear()
     */
    @Override
    public void clear() {
        getSession().clear();
    }

    Session getSession() {
        return sessionProvider.getSession();
    }

    private void processQuery(Query query, BaseQuery originQuery) {
        processQuery(query, originQuery.getParameters(), originQuery.getFirstResult(),
                originQuery.getMaxResults());
        fillParameters(query, originQuery.getParameters());
        query.setFirstResult(originQuery.getFirstResult());
        if (originQuery.getMaxResults() > 0) {
            query.setMaxResults(originQuery.getMaxResults());
        }
    }

    private void processQuery(Query query, QueryParameters parameters,
                              int firstResult, int maxResults) {
        fillParameters(query, parameters);
        query.setFirstResult(firstResult);
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
    }

    private void fillParameters(Query query, QueryParameters params) {
        if (params == null) {
            return;
        }
        if (params instanceof PositionalParameters) {
            fillParameters(query, (PositionalParameters) params);
        } else if (params instanceof NamedParameters) {
            fillParameters(query, (NamedParameters) params);
        } else {
            throw new UnsupportedOperationException("不支持的参数形式");
        }
    }

    private void fillParameters(Query query, PositionalParameters params) {
        Object[] paramArray = params.getParams();
        for (int i = 0; i < paramArray.length; i++) {
            query = query.setParameter(i, paramArray[i]);
        }
    }

    private void fillParameters(Query query, NamedParameters params) {
        for (Map.Entry<String, Object> entry : params.getParams().entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Collection) {
                query.setParameterList(entry.getKey(), (Collection) value);
            } else if (value.getClass().isArray()) {
                query.setParameterList(entry.getKey(), (Object[]) value);
            } else {
                query.setParameter(entry.getKey(), value);
            }
        }
    }
}