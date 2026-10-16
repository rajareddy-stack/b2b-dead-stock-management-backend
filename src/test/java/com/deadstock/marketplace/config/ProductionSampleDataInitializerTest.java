package com.deadstock.marketplace.config;

import com.deadstock.marketplace.model.Category;
import com.deadstock.marketplace.model.Listing;
import com.deadstock.marketplace.model.User;
import com.deadstock.marketplace.repository.BusinessProfileRepository;
import com.deadstock.marketplace.repository.CategoryRepository;
import com.deadstock.marketplace.repository.ListingRepository;
import com.deadstock.marketplace.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductionSampleDataInitializerTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final BusinessProfileRepository businessProfileRepository = mock(BusinessProfileRepository.class);
    private final CategoryRepository categoryRepository = mock(CategoryRepository.class);
    private final ListingRepository listingRepository = mock(ListingRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    @Test
    void seedsClearlyMarkedInventoryWithDisabledSyntheticSupplier() {
        when(userRepository.existsByEmail("sample-supplier@deadstockmarketplace.invalid")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-random-password");
        when(categoryRepository.findByTitle(anyString())).thenAnswer(invocation -> Optional.of(
                new Category(invocation.getArgument(0), "Sample category", "sample")));
        when(listingRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ProductionSampleDataInitializer initializer = new ProductionSampleDataInitializer(
                true, userRepository, businessProfileRepository, categoryRepository, listingRepository, passwordEncoder);
        initializer.run();

        var userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertFalse(userCaptor.getValue().isEnabled());
        var listingsCaptor = org.mockito.ArgumentCaptor.forClass(Iterable.class);
        verify(listingRepository).saveAll(listingsCaptor.capture());
        List<?> listings = (List<?>) listingsCaptor.getValue();
        assertEquals(5, listings.size());
        Listing expiryLot = (Listing) listings.get(4);
        assertEquals("DEMO-CHEM-005", expiryLot.getSku());
        assertNotNull(expiryLot.getExpiryDate());
        assertEquals(true, expiryLot.getTags().contains("Demo sample"));
    }

    @Test
    void doesNotDuplicateExistingSampleSupplier() {
        when(userRepository.existsByEmail("sample-supplier@deadstockmarketplace.invalid")).thenReturn(true);
        new ProductionSampleDataInitializer(
                true, userRepository, businessProfileRepository, categoryRepository, listingRepository, passwordEncoder).run();
        verify(userRepository, never()).save(any(User.class));
        verify(listingRepository, never()).saveAll(any());
    }
}
