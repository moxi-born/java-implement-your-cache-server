package com.moxib.client.validator;

import com.beust.jcommander.IParameterValidator2;
import com.beust.jcommander.ParameterDescription;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameterized;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

public class RegexValidator implements IParameterValidator2 {
  @Override
  public void validate(String name, String value) throws ParameterException {
  }

  @Override
  public void validate(String name, String value, ParameterDescription pd) throws ParameterException {
    Parameterized parameterized = pd.getParameterized();
    Class<? extends Parameterized> clazz = parameterized.getClass();
    try {
      Field declaredField = clazz.getDeclaredField("field");
      declaredField.setAccessible(true);
      Field paramField = (Field) declaredField.get(parameterized);
      Regex regex = paramField.getAnnotation(Regex.class);
      if (null == regex) return;
      String regexStr = regex.value();
      if (!Pattern.matches(regexStr, value)) {
        throw new ParameterException("Parameter " + name + " should match " + regexStr + " (found " + value + ")");
      }
    } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ignore) {
    }
  }
}
