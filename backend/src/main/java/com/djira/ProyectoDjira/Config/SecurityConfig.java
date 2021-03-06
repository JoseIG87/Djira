package com.djira.ProyectoDjira.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsUtils;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class SecurityConfig extends WebSecurityConfigurerAdapter {
   
   @Override
   public void configure(WebSecurity web) throws Exception {
	   web.ignoring().requestMatchers(CorsUtils::isPreFlightRequest);
   }
   
   @Override
   protected void configure(HttpSecurity http) throws Exception
   {
        http
       .csrf().disable()
       .authorizeRequests()
         .antMatchers(HttpMethod.OPTIONS,"http://localhost:4200").permitAll()
         .antMatchers("/resources/**").permitAll()
         .antMatchers("/masculino/calzado/**").permitAll()
         .anyRequest().authenticated()
       .and()
       .formLogin()
       .and()
       .httpBasic();
   }
	
}
