package ru.practicum.shareit.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "shareit-server")
public class ShareItGateWayProperties {

    String url;
}
