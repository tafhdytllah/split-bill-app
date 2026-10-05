package com.tafhdev.split_bill_app.shared.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.json.JsonMapper;

@Component
public class JacksonConfig {

    @Bean
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .withCoercionConfig(String.class, config -> {
                    config.setCoercion(
                            CoercionInputShape.Integer,
                            CoercionAction.Fail
                    );

                    config.setCoercion(
                            CoercionInputShape.Float,
                            CoercionAction.Fail
                    );

                    config.setCoercion(
                            CoercionInputShape.Boolean,
                            CoercionAction.Fail
                    );
                })
                .build();
    }
}
