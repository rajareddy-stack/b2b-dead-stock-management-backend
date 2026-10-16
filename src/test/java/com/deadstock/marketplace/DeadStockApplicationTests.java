package com.deadstock.marketplace;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.deadstock.marketplace.repository.CategoryRepository;
import com.deadstock.marketplace.repository.ListingReportRepository;
import com.deadstock.marketplace.repository.ListingRepository;
import com.deadstock.marketplace.model.BusinessProfile;
import com.deadstock.marketplace.model.Listing;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class DeadStockApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private ListingReportRepository listingReportRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void homePageRendersListingImagesWithLocalFallbacks() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/images/listing-placeholder.svg")))
                .andExpect(content().string(containsString("class=\"listing-meta")));
    }

    @Test
    @Transactional
    void publicListingPageDoesNotExposeSellerLegalOrDirectContactDetails() throws Exception {
        Listing listing = listingRepository.findAll().get(0);
        BusinessProfile profile = listing.getSeller().getBusinessProfile();
        mockMvc.perform(get("/listings/{id}", listing.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(profile.getCompanyName().replace("&", "&amp;"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString(profile.getTaxId()))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString(profile.getAddress()))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString(profile.getPhone()))));
    }

    @Test
    void publicApiProvidesMarketplaceOverview() throws Exception {
        mockMvc.perform(get("/api/public/overview"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"featuredListings\"")))
                .andExpect(content().string(containsString("\"categories\"")))
                .andExpect(jsonPath("$.featuredListings[0].seller.businessProfile.companyName").exists())
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("\"taxId\""))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("\"postalCode\""))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("\"phone\""))));
    }

    @Test
    void publicApiSearchesListingsWithoutFilters() throws Exception {
        mockMvc.perform(get("/api/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].seller.businessProfile.companyName").exists())
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("\"email\""))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("\"taxId\""))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("\"address\""))));
    }

    @Test
    void publicListingDetailOmitsSellerContactAndLegalInformation() throws Exception {
        Long listingId = listingRepository.findAll().get(0).getId();
        mockMvc.perform(get("/api/listings/{id}", listingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seller.businessProfile.companyName").exists())
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("\"email\""))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("\"taxId\""))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("\"address\""))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("\"phone\""))));
    }

    @Test
    void publicApiSearchesListingsByCityOrState() throws Exception {
        mockMvc.perform(get("/api/listings").param("location", "Maharashtra"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nashik")));
    }

    @Test
    @WithUserDetails("seller@metrosteel.com")
    void listingTagsCanBeCreatedAndFoundByKeyword() throws Exception {
        Long categoryId = categoryRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Tag search regression lot",
                                  "description": "Surplus material for test coverage.",
                                  "categoryId": %d,
                                  "originalPrice": 200,
                                  "price": 100,
                                  "quantity": 5,
                                  "unit": "Pieces",
                                  "condition": "BRAND_NEW_SURPLUS",
                                  "location": "Pune, Maharashtra",
                                  "tags": ["regression-tag-2026"]
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/listings").param("query", "regression-tag-2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Tag search regression lot"))
                .andExpect(jsonPath("$[0].tags[0]").value("Regression-tag-2026"));
    }

    @Test
    @WithUserDetails("seller@metrosteel.com")
    void bulkIntakeRejectsDuplicateSkusBeforeCreatingAnyRows() throws Exception {
        Long categoryId = categoryRepository.findAll().get(0).getId();
        long existingListings = listingRepository.count();
        String payload = """
                [
                  {
                    "sku":"BULK-DUP-01","title":"Bulk lot one","description":"Validated lot",
                    "categoryId":%d,"originalPrice":100,"price":75,"quantity":2,"unit":"Units",
                    "condition":"BRAND_NEW_SURPLUS","location":"Pune, Maharashtra"
                  },
                  {
                    "sku":"bulk-dup-01","title":"Bulk lot two","description":"Duplicate SKU",
                    "categoryId":%d,"originalPrice":100,"price":75,"quantity":2,"unit":"Units",
                    "condition":"BRAND_NEW_SURPLUS","location":"Pune, Maharashtra"
                  }
                ]
                """.formatted(categoryId, categoryId);
        mockMvc.perform(post("/api/listings/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Duplicate SKU")));
        org.junit.jupiter.api.Assertions.assertEquals(existingListings, listingRepository.count());
    }

    @Test
    @WithUserDetails("seller@metrosteel.com")
    void listingIntakeRejectsUnsafeImageSchemes() throws Exception {
        Long categoryId = categoryRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Unsafe image URL lot",
                                  "description": "The image scheme must be restricted.",
                                  "categoryId": %d,
                                  "originalPrice": 200,
                                  "price": 100,
                                  "quantity": 5,
                                  "unit": "Pieces",
                                  "condition": "BRAND_NEW_SURPLUS",
                                  "location": "Pune, Maharashtra",
                                  "imageUrl": "javascript:alert(1)"
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("buyer@apexbuild.com")
    void buyerCanReportAnotherBusinessesActiveListing() throws Exception {
        Long listingId = listingRepository.findAll().get(0).getId();
        String report = """
                {"reason":"The listing details do not match the photos."}
                """;
        mockMvc.perform(post("/api/listings/{id}/reports", listingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(report))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(post("/api/listings/{id}/reports", listingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(report))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("pending report")));

        Long reportId = listingReportRepository.findAll().get(0).getId();
        mockMvc.perform(get("/api/admin/reports").with(user("moderator").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reason").value("The listing details do not match the photos."));
        mockMvc.perform(put("/api/admin/reports/{id}/dismiss", reportId)
                        .with(user("moderator").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void registrationDoesNotAllowAdminRole() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "untrusted-admin@example.com",
                                  "password": "safe-password",
                                  "fullName": "Untrusted Admin",
                                  "role": "ADMIN",
                                  "companyName": "Example",
                                  "taxId": "EXAMPLE-123",
                                  "address": "1 Main Street",
                                  "city": "Mumbai",
                                  "state": "Maharashtra",
                                  "phone": "1234567890"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Only buyer and seller accounts can be registered")));
    }

    @Test
    @WithUserDetails("seller@metrosteel.com")
    void sellerDashboardRendersTheInventoryTable() throws Exception {
        mockMvc.perform(get("/seller/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/dashboard"))
                .andExpect(content().string(containsString("class=\"table-responsive\"")))
                .andExpect(content().string(containsString("data-value=")));
    }

    @Test
    @WithUserDetails("buyer@apexbuild.com")
    void buyerDashboardRendersOfferTotals() throws Exception {
        mockMvc.perform(get("/buyer/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("buyer/dashboard"))
                .andExpect(content().string(containsString("My Purchase Offers")))
                .andExpect(content().string(containsString("Confirmed Deals")))
                .andExpect(content().string(containsString("₹")));
    }
}
