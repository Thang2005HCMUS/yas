package com.yas.media.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.media.model.Media;
import com.yas.media.model.dto.MediaDto;
import com.yas.media.service.MediaService;
import com.yas.media.viewmodel.MediaPostVm;
import com.yas.media.viewmodel.MediaVm;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class MediaControllerTest {

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private MediaController mediaController;

    @Test
    void create_whenRequestIsValid_thenReturnNoFileMediaVm() {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "photo.png", "image/png", new byte[] {1});
        MediaPostVm request = new MediaPostVm("caption", multipartFile, "override.png");
        Media media = new Media();
        media.setId(10L);
        media.setCaption("caption");
        media.setFileName("override.png");
        media.setMediaType("image/png");
        when(mediaService.saveMedia(request)).thenReturn(media);

        ResponseEntity<Object> response = mediaController.create(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void delete_whenValidId_thenReturnNoContent() {
        ResponseEntity<Void> response = mediaController.delete(11L);

        verify(mediaService).removeMedia(11L);
        assertEquals(204, response.getStatusCode().value());
    }

    @Test
    void get_whenMediaFound_thenReturnOk() {
        MediaVm mediaVm = new MediaVm(1L, "cap", "a.png", "image/png", "/medias/1/file/a.png");
        when(mediaService.getMediaById(1L)).thenReturn(mediaVm);

        ResponseEntity<MediaVm> response = mediaController.get(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("a.png", response.getBody().getFileName());
    }

    @Test
    void get_whenMediaNotFound_thenReturnNotFound() {
        when(mediaService.getMediaById(2L)).thenReturn(null);

        ResponseEntity<MediaVm> response = mediaController.get(2L);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void getByIds_whenNotEmpty_thenReturnOk() {
        List<MediaVm> medias = List.of(new MediaVm(1L, "cap", "a.png", "image/png", "url"));
        when(mediaService.getMediaByIds(List.of(1L))).thenReturn(medias);

        ResponseEntity<List<MediaVm>> response = mediaController.getByIds(List.of(1L));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getByIds_whenEmpty_thenReturnNotFound() {
        when(mediaService.getMediaByIds(List.of(1L))).thenReturn(List.of());

        ResponseEntity<List<MediaVm>> response = mediaController.getByIds(List.of(1L));

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void getFile_whenMediaFound_thenReturnAttachmentResponse() throws Exception {
        byte[] content = "hello".getBytes();
        MediaDto mediaDto = MediaDto.builder()
            .content(new ByteArrayInputStream(content))
            .mediaType(MediaType.IMAGE_PNG)
            .build();
        when(mediaService.getFile(9L, "photo.png")).thenReturn(mediaDto);

        ResponseEntity<InputStreamResource> response = mediaController.getFile(9L, "photo.png");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("attachment; filename=\"photo.png\"", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
        assertEquals("hello", new String(response.getBody().getInputStream().readAllBytes()));
    }
}
