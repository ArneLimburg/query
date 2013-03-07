package org.apache.deltaspike.query.impl.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.deltaspike.query.test.TransactionalTestCase;
import org.apache.deltaspike.query.test.domain.Simple;
import org.apache.deltaspike.query.test.domain.Simple_;
import org.apache.deltaspike.query.test.service.ExtendedDaoInterface;
import org.apache.deltaspike.query.test.util.TestDeployments;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;

public class EntityDaoHandlerTest extends TransactionalTestCase {

    @Deployment
    public static Archive<?> deployment() {
        return TestDeployments.initDeployment()
                .addClasses(ExtendedDaoInterface.class)
                .addPackage(Simple.class.getPackage());
    }

    @Inject
    private ExtendedDaoInterface dao;

    @Produces
    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void should_save() throws Exception {
        // given
        Simple simple = new Simple("test");

        // when
        simple = dao.save(simple);

        // then
        assertNotNull(simple.getId());
    }

    @Test
    public void should_merge() throws Exception {
        // given
        Simple simple = createSimple("testMerge");
        Long id = simple.getId();

        // when
        final String newName = "testMergeUpdated";
        simple.setName(newName);
        simple = dao.save(simple);

        // then
        assertEquals(id, simple.getId());
        assertEquals(newName, simple.getName());
    }

    @Test
    public void should_save_and_flush() throws Exception {
        // given
        Simple simple = new Simple("test");

        // when
        simple = dao.saveAndFlush(simple);
        Simple fetch = (Simple) entityManager.createNativeQuery("select * from simple_table where id = ?", Simple.class)
                .setParameter(1, simple.getId())
                .getSingleResult();

        // then
        assertEquals(simple.getId(), fetch.getId());
    }

    @Test
    public void should_refresh() throws Exception {
        // given
        final String name = "testRefresh";
        Simple simple = createSimple(name);

        // when
        simple.setName("override");
        dao.refresh(simple);

        // then
        assertEquals(name, simple.getName());
    }

    @Test
    public void should_find_by_pk() throws Exception {
        // given
        Simple simple = createSimple("testFindByPk");

        // when
        Simple find = dao.findBy(simple.getId());

        // then
        assertEquals(simple.getName(), find.getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_find_by_example() throws Exception {
        // given
        Simple simple = createSimple("testFindByExample");

        // when
        List<Simple> find = dao.findBy(simple, Simple_.name);

        // then
        assertNotNull(find);
        assertFalse(find.isEmpty());
        assertEquals(simple.getName(), find.get(0).getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_find_by_example_with_start_and_max() throws Exception {
        // given
        Simple simple = createSimple("testFindByExample1", Integer.valueOf(10));
        createSimple("testFindByExample1", Integer.valueOf(10));

        // when
        List<Simple> find = dao.findBy(simple, 0, 1, Simple_.name, Simple_.counter);

        // then
        assertNotNull(find);
        assertFalse(find.isEmpty());
        assertEquals(1,find.size());
        assertEquals(simple.getName(), find.get(0).getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_find_by_example_with_no_attributes() throws Exception {
        // given
        Simple simple = createSimple("testFindByExample");
        SingularAttribute<Simple, ?>[] attributes = new SingularAttribute[] {};

        // when
        List<Simple> find = dao.findBy(simple, attributes);

        // then
        assertNotNull(find);
        assertFalse(find.isEmpty());
        assertEquals(simple.getName(), find.get(0).getName());
    }

    @Test
    public void should_find_by_all() {
        // given
        createSimple("testFindAll1");
        createSimple("testFindAll2");

        // when
        List<Simple> find = dao.findAll();

        // then
        assertEquals(2, find.size());
    }

    @Test
    public void should_find_by_all_with_start_and_max() {
        // given
        createSimple("testFindAll1");
        createSimple("testFindAll2");

        // when
        List<Simple> find = dao.findAll(0, 1);

        // then
        assertEquals(1, find.size());
    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void should_find_by_like() {
        // given
        createSimple("testFindAll1");
        createSimple("testFindAll2");
        Simple example = new Simple("test");

        // when
        List<Simple> find = dao.findByLike(example, Simple_.name);

        // then
        assertEquals(2, find.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_find_by_like_with_start_and_max() {
        // given
        createSimple("testFindAll1");
        createSimple("testFindAll2");
        Simple example = new Simple("test");

        // when
        List<Simple> find = dao.findByLike(example, 1, 10, Simple_.name);

        // then
        assertEquals(1, find.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_find_by_like_non_string() {
        // given
        createSimple("testFindAll1",1);
        createSimple("testFindAll2",2);
        Simple example = new Simple("test");
        example.setCounter(1);

        // when
        List<Simple> find = dao.findByLike(example, Simple_.name, Simple_.counter);

        // then
        assertEquals(1, find.size());
    }

    @Test
    public void should_count_all() {
        // given
        createSimple("testCountAll");

        // when
        Long result = dao.count();

        // then
        assertEquals(Long.valueOf(1), result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_count_with_attributes() {
        // given
        Simple simple = createSimple("testFindAll1", Integer.valueOf(55));
        createSimple("testFindAll2", Integer.valueOf(55));

        // when
        Long result = dao.count(simple, Simple_.name, Simple_.counter);

        // then
        assertEquals(Long.valueOf(1), result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_count_with_no_attributes() {
        // given
        Simple simple = createSimple("testFindAll1");
        createSimple("testFindAll2");
        SingularAttribute<Simple, Object>[] attributes = new SingularAttribute[] {};

        // when
        Long result = dao.count(simple, attributes);

        // then
        assertEquals(Long.valueOf(2), result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_count_by_like() {
        // given
        createSimple("testFindAll1");
        createSimple("testFindAll2");
        Simple example = new Simple("test");

        // when
        Long count = dao.countLike(example, Simple_.name);

        // then
        assertEquals(Long.valueOf(2), count);
    }

    @Test
    public void should_remove() {
        // given
        Simple simple = createSimple("testRemove");

        // when
        dao.remove(simple);
        dao.flush();
        Simple lookup = entityManager.find(Simple.class, simple.getId());

        // then
        assertNull(lookup);
    }

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    private Simple createSimple(String name) {
        return createSimple(name, null);
    }

    private Simple createSimple(String name, Integer counter) {
        Simple result = new Simple(name);
        result.setCounter(counter);
        entityManager.persist(result);
        entityManager.flush();
        return result;
    }

}