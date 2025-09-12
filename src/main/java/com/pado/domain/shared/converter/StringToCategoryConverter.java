package com.pado.domain.shared.converter;

import com.pado.domain.shared.entity.Category;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class StringToCategoryConverter implements Converter<String, Category> {

    @Override
    public Category convert(String source) {
        if (!StringUtils.hasText(source)) {
            return null;
        }
        return Category.from(source.trim());
    }
}