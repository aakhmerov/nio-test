package com.aakhmerov.test.flux;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;


/**
 * @author Askar Akhmerov
 */
@Configuration
@ComponentScan(basePackages = "com.aakhmerov.test.flux")
@EnableWebFlux
public class FluxConfig {
}
