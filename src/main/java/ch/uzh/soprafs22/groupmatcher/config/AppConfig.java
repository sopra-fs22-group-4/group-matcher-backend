package ch.uzh.soprafs22.groupmatcher.config;

import org.apache.commons.lang3.ObjectUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setPropertyCondition(context -> ObjectUtils.isNotEmpty(context.getSource()))
                .setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }
}
