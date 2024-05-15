package ai.utiliti.bes.tasks;

import ai.utiliti.bes.interceptors.RateLimitInterceptor;
import ai.utiliti.bes.model.ListingStatus;
import ai.utiliti.bes.model.Marketplace;
import ai.utiliti.bes.model.opensea.MarketplaceListing;
import ai.utiliti.bes.model.opensea.data.Offer;
import ai.utiliti.bes.model.opensea.payloads.ItemListedPayload;
import ai.utiliti.bes.respository.MarketplaceListingRepository;
import com.google.gson.*;
import io.reactivex.annotations.Nullable;
import okhttp3.*;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
@Profile("opensea-data")
public class LoadOpenseaListingsTask {

    private final String listingsBaseUrl = "https://api.opensea.io/v2/listings/collection/%s/all?limit=%d";
    private final OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(new RateLimitInterceptor()).build();
    private final String openseaApiKey = System.getenv("OPENSEA_API_KEY");

    private final MarketplaceListingRepository marketplaceListingRepository;
    private final Gson gson;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(LoadOpenseaListingsTask.class);

    @Autowired
    public LoadOpenseaListingsTask(MarketplaceListingRepository marketplaceListingRepository, Gson gson) {
        this.marketplaceListingRepository = marketplaceListingRepository;
        this.gson = gson;
    }

    @PostConstruct
    public void loadOpenseaListings() {

        logger.info("Loading all collection listings!");

        List<String> collectionSlugs = this.marketplaceListingRepository.getAllCollections();

        for (String slug : collectionSlugs) {
            executorService.submit(new FetchCollectionListingsTask(slug));
        }

//        try {
//            long start = System.currentTimeMillis();
//            long end = System.currentTimeMillis();
//
//            System.out.println("Loaded " + collectionSlugs.size() + " collections listings in " + TimeUnit.MILLISECONDS.toSeconds((end - start)));
//            executorService.shutdown();
//
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }

    private void updateListings(List<JsonObject> listings, String slug) {
        for (JsonObject obj : listings) {
            ItemListedPayload itemListedPayload = gson.fromJson(obj, ItemListedPayload.class);
            MarketplaceListing listing = this.marketplaceListingRepository.findByMarketplaceAndOrderHash(Marketplace.OPENSEA, itemListedPayload.getOrderHash()).orElse(null);

            if (listing != null) continue;

            listing = new MarketplaceListing();

            listing.setOrderHash(itemListedPayload.getOrderHash());
            listing.setCollection(slug);
            listing.setEventName("item_listed");
            listing.setPayload(gson.toJson(obj));

            BigInteger amount = BigInteger.ZERO;

            Offer[] offers = itemListedPayload.getProtocolData().getParameters().getOffer();
            if (offers.length > 0) {
                amount = offers[0].getStartAmount();
                listing.setTokenId(offers[0].getIdentifierOrCriteria().toString());
            }

            listing.setListingAmount(amount);
            listing.setMarketplace(Marketplace.OPENSEA);
            listing.setStatus(ListingStatus.LISTED);

            marketplaceListingRepository.save(listing);
        }
    }

    class FetchCollectionListingsTask implements Callable<List<JsonObject>> {

        private final String slug;

        public FetchCollectionListingsTask(String slug) {
            this.slug = slug;
        }

        @Override
        public List<JsonObject> call() {
            List<JsonObject> totalListings = new ArrayList<>();
            Request initialRequest = createRequest(slug, null);

            if (initialRequest == null) throw new RuntimeException("Cannot load collection with slug " + slug + "! Initial request returned null!");

            try (Response initialResponse = httpClient.newCall(initialRequest).execute()) {
                if (!initialResponse.isSuccessful()) {
                    throw new ResponseStatusException(HttpStatus.valueOf(initialResponse.code()), "Could not get listings for collection " + slug);
                }

                if (initialResponse.body() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not get listings for collection " + slug + ". Body is null");
                }

                String jsonString = initialResponse.body().string();

                JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);
                JsonArray listings = jsonObject.getAsJsonArray("listings");

                if (listings == null) {
                    return totalListings;
                }

                totalListings.addAll(listings.asList().stream().map(JsonElement::getAsJsonObject).collect(Collectors.toList()));

                JsonElement next = jsonObject.get("next");
                int count = listings.size();

                while (next != null && !(next instanceof JsonNull)) {
                    Request request = createRequest(slug, next.getAsString());

                    logger.info("Still loading {}", slug);

                    try (Response r = httpClient.newCall(request).execute()) {
                        if (!r.isSuccessful() || r.body() == null) break;

                        jsonString = r.body().string();

                        jsonObject = gson.fromJson(jsonString, JsonObject.class);
                        listings = jsonObject.getAsJsonArray("listings");

                        totalListings.addAll(listings.asList().stream().map(JsonElement::getAsJsonObject).collect(Collectors.toList()));

                        count += listings.size();
                        next = jsonObject.get("next");
                    }
                }
                logger.info("Loaded {} listings for {}", count, slug);

                updateListings(totalListings, slug);

                return totalListings;
            } catch (Exception e) {
                logger.error("Could not load listings for collection " + slug + " encountered an error:", e);
                e.printStackTrace();
            }
            
            return totalListings;
        }

        private final Request createRequest(String slug, @Nullable String next) {

            try {

                String initialRequestUrl = String.format(listingsBaseUrl, slug, 100);

                if (next != null) {
                    initialRequestUrl += "&next=" + next;
                }

                return new Request.Builder()
                .url(new URL(initialRequestUrl))
                .addHeader("Content-Type", "application/json")
                .addHeader("X-API-KEY", openseaApiKey)
                .get()
                .build();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
