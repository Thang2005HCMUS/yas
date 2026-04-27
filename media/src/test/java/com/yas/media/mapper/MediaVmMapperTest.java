package com.yas.media.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.yas.media.model.Media;
import com.yas.media.viewmodel.MediaVm;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class MediaVmMapperTest {

    private final MediaVmMapper mapper = Mappers.getMapper(MediaVmMapper.class);

    @Test
    void toVm_whenMediaProvided_thenMapSharedFields() {
        Media media = new Media();
        media.setId(1L);
        media.setCaption("caption");
        media.setFileName("file.png");
        media.setMediaType("image/png");
        media.setFilePath("internal/path");

        MediaVm vm = mapper.toVm(media);

        assertEquals(1L, vm.getId());
        assertEquals("caption", vm.getCaption());
        assertEquals("file.png", vm.getFileName());
        assertEquals("image/png", vm.getMediaType());
        assertNull(vm.getUrl());
    }

    @Test
    void toModel_whenViewModelProvided_thenMapSharedFields() {
        MediaVm vm = new MediaVm(2L, "caption2", "file2.png", "image/jpeg", "http://localhost/file2.png");

        Media media = mapper.toModel(vm);

        assertEquals(2L, media.getId());
        assertEquals("caption2", media.getCaption());
        assertEquals("file2.png", media.getFileName());
        assertEquals("image/jpeg", media.getMediaType());
        assertNull(media.getFilePath());
    }

    @Test
    void partialUpdate_whenNullFieldsPresent_thenIgnoreNullValues() {
        Media media = new Media();
        media.setCaption("old");
        media.setFileName("old.png");
        media.setMediaType("image/png");

        MediaVm patchVm = new MediaVm(null, null, "new.png", null, null);
        mapper.partialUpdate(media, patchVm);

        assertEquals("old", media.getCaption());
        assertEquals("new.png", media.getFileName());
        assertEquals("image/png", media.getMediaType());
    }
}
