package ai.utiliti.bes.respository;

import ai.utiliti.bes.model.ListingStatus;
import ai.utiliti.bes.model.Marketplace;
import ai.utiliti.bes.model.opensea.MarketplaceListing;
import ai.utiliti.bes.model.opensea.data.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.yaml.snakeyaml.error.Mark;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarketplaceListingRepository extends JpaRepository<MarketplaceListing, UUID> {

    Optional<MarketplaceListing> findByMarketplaceAndOrderHashAndStatus(Marketplace marketplace, String orderHash, ListingStatus status);

    Optional<MarketplaceListing> findByMarketplaceAndOrderHash(Marketplace marketplace, String orderHash);


    //TODO change params to string instead of objects (bad serialization)
    @Query(value = "SELECT MIN(listing_amount) FROM marketplace_listings " +
            "WHERE collection=cast(:collection AS text) AND marketplace=(:marketplace) AND status=(:status) AND expiration_date>=(:now)", nativeQuery = true)
    BigInteger findFloorPrice(@Param("collection") String collection, @Param("marketplace") String marketplace, @Param("status") String status, @Param("now") Date now);

    @Query(value = "SELECT DISTINCT collection FROM marketplace_listings", nativeQuery = true)
    List<String> getAllCollections();
}