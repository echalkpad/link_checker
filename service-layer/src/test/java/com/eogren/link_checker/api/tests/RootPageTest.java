package com.eogren.link_checker.api.tests;

import com.eogren.link_checker.service_layer.api.RootPage;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.util.Set;

import static org.junit.Assert.*;

public class RootPageTest {

    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        validator = vf.getValidator();
    }

    @Test
    public void testNullUrlDisallowed() {
        RootPage rp = new RootPage(null);

        Set<ConstraintViolation<RootPage>> violations = validator.validate(rp);
        assertEquals(1, violations.size());
    }
}
