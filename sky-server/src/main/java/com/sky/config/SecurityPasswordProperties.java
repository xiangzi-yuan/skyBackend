package com.sky.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.password")
public class SecurityPasswordProperties {
    private int bcryptCost = 12;
    public int getBcryptCost() { return bcryptCost; }
    public void setBcryptCost(int bcryptCost) { this.bcryptCost = bcryptCost; }
}
