package com.bachat.inventory.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Inventory Management Backend API",
                version = "v1",
                description = "Backend APIs for products, inventory, customers, orders, invoices, expenses and business reports.",
                contact = @Contact(name = "Inventory Backend"),
                license = @License(name = "Proprietary")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local"),
                @Server(url = "https://inventory-management-production-995e.up.railway.app", description = "Production"),
                @Server(url = "http://68.178.164.161:8080", description = "Production")
        }
)
public class OpenApiConfig {
}
