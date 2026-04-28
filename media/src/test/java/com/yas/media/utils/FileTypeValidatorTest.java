package com.yas.media.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import jakarta.validation.ConstraintValidatorContext;

class FileTypeValidatorTest {

    private static final String MESSAGE = "invalid file type";
    private FileTypeValidator validator;
    private ConstraintValidatorContext context;
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        validator = new FileTypeValidator();
        context = mock(ConstraintValidatorContext.class);
        violationBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(MESSAGE)).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);

        ValidFileType annotation = mock(ValidFileType.class);
        when(annotation.allowedTypes()).thenReturn(new String[] {"image/png", "image/jpeg"});
        when(annotation.message()).thenReturn(MESSAGE);
        validator.initialize(annotation);
    }

    @Test
    void isValid_whenFileIsNull_thenReturnFalseAndAddViolation() {
        boolean valid = validator.isValid(null, context);

        assertFalse(valid);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(MESSAGE);
    }

    @Test
    void isValid_whenContentTypeIsNull_thenReturnFalse() {
        MockMultipartFile file = new MockMultipartFile("file", "a.png", null, new byte[] {1});

        boolean valid = validator.isValid(file, context);

        assertFalse(valid);
    }

    @Test
    void isValid_whenContentTypeIsNotAllowed_thenReturnFalse() {
        MockMultipartFile file = new MockMultipartFile("file", "a.gif", "image/gif", new byte[] {1});

        boolean valid = validator.isValid(file, context);

        assertFalse(valid);
        verify(context).disableDefaultConstraintViolation();
    }

    @Test
    void isValid_whenAllowedTypeButInvalidImage_thenReturnFalse() {
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", "not-image".getBytes());

        boolean valid = validator.isValid(file, context);

        assertFalse(valid);
    }

    @Test
    void isValid_whenAllowedTypeAndValidImage_thenReturnTrue() throws Exception {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);

        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", output.toByteArray());

        boolean valid = validator.isValid(file, context);

        assertTrue(valid);
    }
}
