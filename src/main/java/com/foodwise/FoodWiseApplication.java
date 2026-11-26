package com.foodwise;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point. Run with {@code ./mvnw} and open http://localhost:8080.
 * The theme lives in src/main/frontend/themes/foodwise.
 */
@SpringBootApplication
@EnableScheduling
@Theme("foodwise")
public class FoodWiseApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(FoodWiseApplication.class, args);
    }

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.addLink("preconnect", "https://fonts.googleapis.com");
        settings.addLink("stylesheet",
                "https://fonts.googleapis.com/css2?family=DM+Sans:opsz,wght@9..40,400;9..40,500;9..40,600;9..40,700&display=swap");
        settings.addLink("icon", "icons/leaf.svg");
    }
}
