package al.netty.apiserver;

import al.netty.apiserver.configuration.ApiServerConfig;
import al.netty.apiserver.domain.server.ApiServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class ApiServerMain {

	public static void main(String[] args) {
		try (AbstractApplicationContext springContext = new AnnotationConfigApplicationContext(ApiServerConfig.class)) {
			springContext.registerShutdownHook();

			ApiServer server = springContext.getBean(ApiServer.class);
			server.start();
		}
	}
}
