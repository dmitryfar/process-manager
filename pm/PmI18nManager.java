package org.activiti.pm;

import java.io.Serializable;
import java.util.Locale;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Farafonov
 */
@Component
public class PmI18nManager implements Serializable {

  @Autowired
  protected Environment environment;

  private static final long serialVersionUID = 1L;
  protected MessageSource messageSource;
  protected Locale locale;

  public String getMessage(String key) {
    checkLocale();
    try {
    return messageSource.getMessage(key, null, locale);
    } catch (NoSuchMessageException e) {
      return key;
    }
  }

  public String getMessage(String key, Object... arguments) {
    checkLocale();
    return messageSource.getMessage(key, arguments, locale);
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  protected void checkLocale() {
    ((ResourceBundleMessageSource)messageSource).setCacheSeconds(0);

    if (locale == null) {
      String localeProperty = environment.getProperty("processmanager.locale", String.class);
      if (StringUtils.isNoneEmpty(localeProperty)) {
        locale = LocaleUtils.toLocale(localeProperty);
      } else
      {
        locale = Locale.getDefault();
      }
    }
  }

  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }
}
