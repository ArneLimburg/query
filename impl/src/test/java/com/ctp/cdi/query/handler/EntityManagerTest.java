package com.ctp.cdi.query.handler;

import java.util.List;

import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ctp.cdi.query.test.domain.Simple;
import com.ctp.cdi.query.test.service.SimpleDaoWithEntityManager;
import com.ctp.cdi.query.test.service.SimpleDaoWithOverriddenEntityManager;
import com.ctp.cdi.query.test.util.Deployments;

@RunWith(Arquillian.class)
public class EntityManagerTest {

    @Deployment
    public static Archive<?> deployment() {
        return Deployments.initDeployment()
                .addClasses(SimpleDaoWithEntityManager.class, 
                            SimpleDaoWithOverriddenEntityManager.class,
                            EntityManagerProducer.class)
                .addPackage(Simple.class.getPackage());
    }
    
    @Inject
    private SimpleDaoWithEntityManager daoWithAnnotation;
    
    @Inject
    private SimpleDaoWithOverriddenEntityManager daoWithInjection;
    
    @Test
    public void shouldUseQualifiedEntityManager() {
        // when
        List<Simple> result = daoWithAnnotation.findByName("testUseQualifiedEntityManager");
        
        // then
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }
    
    @Test
    public void shouldUseInjectedEntityManager() {
        // when
        List<Simple> result = daoWithInjection.findByName("testUseInjectedEntityManager");
        
        // then
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }
    
    @Test
    public void shouldInjectEntityManager() {
        // when
        List<Simple> result = daoWithInjection.findWithEm("testInjectEntityManager");
        
        // then
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

}
