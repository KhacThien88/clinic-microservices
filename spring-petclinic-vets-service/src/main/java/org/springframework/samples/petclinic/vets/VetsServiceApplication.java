/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.vets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.samples.petclinic.vets.system.VetsProperties;

/**
 * @author Maciej Szarlinski
 */
@EnableDiscoveryClient
@SpringBootApplication
@EnableConfigurationProperties(VetsProperties.class)
public class VetsServiceApplication {

	public static void main(String[] args) {
		System.out.println("Starting VetsServiceApplication652322244322322");
		SpringApplication.run(VetsServiceApplication.class, args);
	}
}
