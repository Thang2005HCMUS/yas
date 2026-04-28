package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Brand;
import com.yas.product.model.Category;
import com.yas.product.model.Product;
import com.yas.product.model.ProductCategory;
import com.yas.product.model.ProductImage;
import com.yas.product.model.ProductOption;
import com.yas.product.model.ProductOptionCombination;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.ProductDetailInfoVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductDetailServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MediaService mediaService;

    @Mock
    private ProductOptionCombinationRepository productOptionCombinationRepository;

    @InjectMocks
    private ProductDetailService productDetailService;

    @Test
    void getProductDetailById_whenProductMissing_thenThrowNotFoundException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productDetailService.getProductDetailById(1L));
    }

    @Test
    void getProductDetailById_whenProductWithoutOptions_thenReturnDetail() {
        Product product = buildPublishedProduct(10L, false);
        product.setThumbnailMediaId(null);
        product.setProductImages(List.of());
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        ProductDetailInfoVm vm = productDetailService.getProductDetailById(10L);

        assertEquals(10L, vm.getId());
        assertEquals("Macbook", vm.getName());
        assertEquals("Apple", vm.getBrandName());
        assertEquals(1, vm.getCategories().size());
        assertNull(vm.getThumbnail());
        assertEquals(0, vm.getVariations().size());
    }

    @Test
    void getProductDetailById_whenProductHasOptions_thenReturnVariationsAndMedia() {
        Product mainProduct = buildPublishedProduct(20L, true);
        mainProduct.setThumbnailMediaId(111L);
        mainProduct.setProductImages(List.of(ProductImage.builder().imageId(112L).product(mainProduct).build()));

        Product variation = buildPublishedProduct(21L, false);
        variation.setParent(mainProduct);
        variation.setThumbnailMediaId(113L);
        variation.setProductImages(List.of(ProductImage.builder().imageId(114L).product(variation).build()));
        mainProduct.setProducts(List.of(variation));

        ProductOption option = new ProductOption();
        option.setId(999L);
        ProductOptionCombination combination = ProductOptionCombination.builder()
            .product(variation)
            .productOption(option)
            .value("Black")
            .build();

        when(productRepository.findById(20L)).thenReturn(Optional.of(mainProduct));
        when(productOptionCombinationRepository.findAllByProduct(variation)).thenReturn(List.of(combination));
        when(mediaService.getMedia(111L)).thenReturn(new NoFileMediaVm(111L, "", "", "", "https://img/111"));
        when(mediaService.getMedia(112L)).thenReturn(new NoFileMediaVm(112L, "", "", "", "https://img/112"));
        when(mediaService.getMedia(113L)).thenReturn(new NoFileMediaVm(113L, "", "", "", "https://img/113"));
        when(mediaService.getMedia(114L)).thenReturn(new NoFileMediaVm(114L, "", "", "", "https://img/114"));

        ProductDetailInfoVm vm = productDetailService.getProductDetailById(20L);

        assertNotNull(vm.getThumbnail());
        assertEquals("https://img/111", vm.getThumbnail().url());
        assertEquals(1, vm.getProductImages().size());
        assertEquals(1, vm.getVariations().size());
        assertEquals("https://img/113", vm.getVariations().getFirst().thumbnail().url());
        assertEquals("Black", vm.getVariations().getFirst().options().get(999L));
    }

    private Product buildPublishedProduct(Long id, boolean hasOptions) {
        Product product = new Product();
        product.setId(id);
        product.setName("Macbook");
        product.setSlug("macbook");
        product.setSku("sku");
        product.setGtin("gtin");
        product.setPrice(1000D);
        product.setAllowedToOrder(true);
        product.setPublished(true);
        product.setFeatured(false);
        product.setVisibleIndividually(true);
        product.setStockTrackingEnabled(true);
        product.setHasOptions(hasOptions);

        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("Apple");
        product.setBrand(brand);

        Category category = new Category();
        category.setId(2L);
        category.setName("Laptop");
        product.setProductCategories(List.of(ProductCategory.builder().product(product).category(category).build()));

        return product;
    }
}
