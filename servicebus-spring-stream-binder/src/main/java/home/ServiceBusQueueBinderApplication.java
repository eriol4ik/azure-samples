// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package home;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServiceBusQueueBinderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceBusQueueBinderApplication.class, args);
    }
}