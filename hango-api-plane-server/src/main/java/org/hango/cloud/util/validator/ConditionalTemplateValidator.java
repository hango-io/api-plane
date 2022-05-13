package org.hango.cloud.util.validator;

import org.hango.cloud.meta.template.NsfExtra;
import org.hango.cloud.meta.template.ServiceMeshTemplate;
import org.hango.cloud.util.validator.annotation.ConditionalTemplate;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Objects;

/**
 * 用于校验，当模板为template时，所必需的的字段为required数组中的值
 *
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/6/21
 **/
public class ConditionalTemplateValidator implements ConstraintValidator<ConditionalTemplate, Object> {

    private static final Logger logger = LoggerFactory.getLogger(ConditionalTemplateValidator.class);

    private String template;

    private String[] required;

    @Override
    public void initialize(ConditionalTemplate constraintAnnotation) {
        this.template = constraintAnnotation.templateName();
        this.required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {

        boolean valid = true;

        try {
            if (!template.equals(BeanUtils.getProperty(value, "nsfTemplate"))) return true;
            if (value instanceof ServiceMeshTemplate) {
                ServiceMeshTemplate template = (ServiceMeshTemplate) value;
                for (String req : required) {
                    Field field = NsfExtra.class.getDeclaredField(req);
                    field.setAccessible(true);
                    Object requiredProp = field.get(template.getNsfExtra());
                    valid = Objects.nonNull(requiredProp) && !StringUtils.isEmpty(requiredProp) && !isObjectEmpty(requiredProp);
                    if (!valid) {
                        context.disableDefaultConstraintViolation();
                        context.buildConstraintViolationWithTemplate(String.format("parameter %s is missing", req))
                                .addConstraintViolation();
                        return false;
                    }
                }
            }

        } catch (IllegalAccessException e) {
            logger.error("Accessor method is not available for class : {}, exception : {}", value.getClass().getName(), e);
            return false;
        } catch (InvocationTargetException e) {
            logger.error("Field or method is not present on class : {}, exception : {}", value.getClass().getName(), e);
            return false;
        } catch (NoSuchMethodException e) {
            logger.error("An exception occurred while accessing class : {}, exception : {}", value.getClass().getName(), e);
            return false;
        } catch (NoSuchFieldException e) {
            logger.error("Field is not present on class : {}, exception : {}", value.getClass().getName(), e);
            return false;
        }

        return valid;
    }

    private boolean isObjectEmpty(Object o) {

        if (o instanceof Collection) {
            Collection coll = (Collection) o;
            return coll.size() == 0;
        }
        return false;
    }
}
