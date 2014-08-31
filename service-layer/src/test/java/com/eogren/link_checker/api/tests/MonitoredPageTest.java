package com.eogren.link_checker.api.tests;

import com.eogren.link_checker.service_layer.api.Page;
import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.util.ArrayList;
import java.util.Set;

import static org.junit.Assert.*;

public class MonitoredPageTest {

    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        validator = vf.getValidator();
    }

    @Test
    public void testNullUrlDisallowed() {
        Page rp = createPage(null);

        Set<ConstraintViolation<Page>> violations = validator.validate(rp);
        assertEquals(1, violations.size());
    }

    protected Page createPage(String url) {
        return new Page(url, true);
    }
}
