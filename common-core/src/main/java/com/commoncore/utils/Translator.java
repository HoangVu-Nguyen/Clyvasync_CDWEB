package com.commoncore.utils;



import com.commoncore.exception.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Translator {
    private final MessageSource messageSource;

    public String toLocale(ResultCode resultCode) {
        try {
            return messageSource.getMessage(resultCode.name(), null, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            // Dòng này sẽ cho mày biết TẠI SAO Spring không tìm thấy key
            System.out.println("DEBUG I18N: " + e.getMessage());
            return resultCode.name();
        }
    }
}