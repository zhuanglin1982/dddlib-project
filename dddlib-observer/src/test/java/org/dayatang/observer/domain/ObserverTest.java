package org.dayatang.observer.domain;

import org.dayatang.domain.AbstractEntity;
import org.dayatang.domain.InstanceFactory;
import org.dayatang.observer.HibernateUtils;
import org.dayatang.persistence.hibernate.EntityRepositoryHibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.dayatang.persistence.hibernate.SessionProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ObserverTest {

    private static SessionFactory sessionFactory;

    private Session session;

    private Transaction tx;

    private static EntityRepositoryHibernate repository;

    private MotherObserver observer_1;
    private FatherObserver observer_2;

    @BeforeClass
    public static void setUpClass() throws Exception {
        sessionFactory = HibernateUtils.getSessionFactory();
        InstanceFactory.bind(SessionFactory.class, sessionFactory);
    }

    @AfterClass
    public static void tearDownClass() {
        sessionFactory.close();
    }

    @Before
    public void setUp() {
        SessionProvider sessionProvider = new SessionProvider(sessionFactory);
        session = sessionProvider.getSession();
        tx = session.beginTransaction();
        repository = new EntityRepositoryHibernate(sessionFactory);
        AbstractEntity.setRepository(repository);
        observer_1 = createMotherObserver(false);
        observer_1.setSubjectKeys(Collections.singleton("BABY-SUBJECT"));
        observer_2 = createFatherObserver(false);
        observer_2.setSubjectKeys(Collections.singleton("BABY-SUBJECT"));
    }

    @After
    public void tearDown() {
        tx.rollback();
        if (session.isOpen()) {
            session.close();
        }
        AbstractEntity.setRepository(null);
    }

    private FatherObserver createFatherObserver(boolean startCar) {
        FatherObserver result = new FatherObserver();
        result.setStartCar(startCar);
        result.save();
        return result;
    }

    private MotherObserver createMotherObserver(boolean buyFood) {
        MotherObserver result = new MotherObserver();
        result.setBuyFood(buyFood);
        result.save();
        return result;
    }

    @Test
    public void process() {
        Baby baby = new Baby();
        baby.cry();

        //MotherObserver observer_1 = (MotherObserver) Observer.get(1L);
        assertTrue(observer_1.getBuyFood());
        //FatherObserver observer_2 = (FatherObserver) Observer.get(2L);
        assertTrue(observer_2.getStartCar());
    }

    @Test
    public void getKeys() {
        //MotherObserver observer_1 = (MotherObserver) Observer.get(1L);
        Set<String> keys = observer_1.getSubjectKeys();

        assertEquals("BABY-SUBJECT", keys.iterator().next());

        keys = new HashSet<String>();
        keys.add("1");
        observer_1.setSubjectKeys(keys);
        observer_1.save();

        keys = observer_1.getSubjectKeys();

        assertEquals("1", keys.iterator().next());

    }
}
