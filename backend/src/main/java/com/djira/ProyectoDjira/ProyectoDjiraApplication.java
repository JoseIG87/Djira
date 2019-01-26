package com.djira.ProyectoDjira;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.djira.ProyectoDjira.Service.CalzadoService;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, 
		DataSourceTransactionManagerAutoConfiguration.class, 
		HibernateJpaAutoConfiguration.class})
@EnableAsync
@EnableWebSecurity
@EnableScheduling
@Configuration
@ComponentScan
public class ProyectoDjiraApplication extends SpringBootServletInitializer 
	implements CommandLineRunner, WebMvcConfigurer {
	
	@Autowired
	private CalzadoService servicio;
	
	public static void main(String[] args) {
		SpringApplication.run(ProyectoDjiraApplication.class, args);	
	}
	
	@Override
	protected SpringApplicationBuilder configure(
			SpringApplicationBuilder application) {
		return application.sources(ProyectoDjiraApplication.class);
	}

	@Bean
	public Module hibernate5Module() {
			
		return new Hibernate5Module()
				.disable(Hibernate5Module.Feature.USE_TRANSIENT_ANNOTATION);
		
	}

	@Override
	public void run(String... args) throws Exception {
		
		//servicio.cargarZapatillasUrbanasHombreEnCloud();
		//servicio.cargarZapatillasDeportivasHombreEnCloud();
		servicio.cargarZapatosEnCloud();
		
	}
	
	@Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .exposedHeaders("Access-Control-Allow-Origin",
                        "Access-Control-Allow-Methods",
                        "Access-Control-Allow-Headers",
                        "Access-Control-Max-Age",
                        "Access-Control-Request-Headers",
                        "Access-Control-Request-Method")
                .maxAge(3600);
    }
}
