package org.dayatang.ioc.test;

import javax.inject.Named;

@Named("service3")
@TheAnnotation
public class MyService3 implements Service {
	@Override
	public String sayHello() {
		return "I am Service 3";
	}
}
