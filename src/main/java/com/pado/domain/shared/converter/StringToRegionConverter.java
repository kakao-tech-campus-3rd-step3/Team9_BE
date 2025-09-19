package com.pado.domain.shared.converter;

import com.pado.domain.shared.entity.Region;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class StringToRegionConverter implements Converter<String, Region> {

    @Override
    public Region convert(String source) {
        if (!StringUtils.hasText(source)) {
            return null;
        }
        return Region.from(source.trim());
    }
}