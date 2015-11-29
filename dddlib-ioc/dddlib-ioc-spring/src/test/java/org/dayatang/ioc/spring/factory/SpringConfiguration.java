package org.dayatang.ioc.spring.factory;

import org.dayatang.ioc.spring.beans.ServiceFactory;
import org.dayatang.ioc.test.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfiguration {
	
	@Bean(name = "service1")
	public Service service1() {
        try {
            return new ServiceFactory().getObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
	
	@Bean(name = "service2")
	public Service service2() {
		return new MyService2();
	}
	
	@Bean(name = "service3")
	public Service service3() {
		return new MyService3();
	}

    @Bean
    public Service2 service2_2() {
         return new MyService21();
    }
}
