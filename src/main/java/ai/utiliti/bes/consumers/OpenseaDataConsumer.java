package ai.utiliti.bes.consumers;

import ai.utiliti.bes.model.ListingStatus;
import ai.utiliti.bes.model.Marketplace;
import ai.utiliti.bes.model.converters.DateDeserializer;
import ai.utiliti.bes.model.converters.ZonedDateTimeJsonDeserializer;
import ai.utiliti.bes.model.opensea.MarketplaceListing;
import ai.utiliti.bes.model.opensea.OpenseaEventType;
import ai.utiliti.bes.model.opensea.OpenseaWebsocketMessage;
import ai.utiliti.bes.model.opensea.data.Collection;
import ai.utiliti.bes.model.opensea.data.Offer;
import ai.utiliti.bes.model.opensea.payloads.ItemListedPayload;
import ai.utiliti.bes.model.opensea.payloads.ItemReceivedBidPayload;
import ai.utiliti.bes.model.opensea.payloads.ItemReceivedOfferPayload;
import ai.utiliti.bes.model.opensea.payloads.ItemSoldPayload;
import ai.utiliti.bes.respository.MarketplaceListingRepository;
import ai.utiliti.bes.services.EmailService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.web3j.utils.Convert;
import org.web3j.utils.Convert.Unit;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;

@Component
@Profile("marketplace-data")
public class OpenseaDataConsumer {

    private final Logger logger = LoggerFactory.getLogger(OpenseaDataConsumer.class);

    private final Gson gson = new GsonBuilder().setLenient()
            .registerTypeAdapter(Date.class, new DateDeserializer())
            .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeJsonDeserializer())
            .create();

    private final MarketplaceListingRepository marketplaceListingRepository;
    private final EmailService emailService;

    @Autowired
    public OpenseaDataConsumer(MarketplaceListingRepository marketplaceListingRepository, EmailService emailService) {
        this.marketplaceListingRepository = marketplaceListingRepository;
        this.emailService = emailService;
    }

    public void handle(OpenseaWebsocketMessage message) {
        JsonObject eventPayload = message.getPayload().getAsJsonObject("payload");


        try {
            OpenseaEventType type = OpenseaEventType.lookupInsensitive(message.getEvent());

            if (type == null) {
                return;
            }


            switch (type) {
                case ITEM_LISTED:

                    ItemListedPayload itemListedPayload = gson.fromJson(eventPayload, ItemListedPayload.class);

                    BigInteger currentFloor = getCurrentFloor(itemListedPayload.getCollection());

                    MarketplaceListing listing = new MarketplaceListing();
                    listing.setOrderHash(itemListedPayload.getOrderHash());
                    listing.setCollection(itemListedPayload.getCollection().getSlug());
                    listing.setEventName(message.getEvent());
                    listing.setPayload(gson.toJson(eventPayload));
                    listing.setListingAmount(new BigInteger(itemListedPayload.getBasePrice()));

                    if (itemListedPayload.getProtocolData() == null) return;

                    Offer[] offers = itemListedPayload.getProtocolData().getParameters().getOffer();
                    if (offers.length > 0) {
                        listing.setTokenId(offers[0].getIdentifierOrCriteria().toString());
                    }

                    // listing.setListingAmount(amount);
                    listing.setMarketplace(Marketplace.OPENSEA);
                    listing.setExpirationDate(itemListedPayload.getExpirationDate());
                    listing.setStatus(ListingStatus.LISTED);

                    marketplaceListingRepository.save(listing);

                    if (listing.getListingAmount().compareTo(currentFloor) < 0) {

                        BigDecimal currentFloorValueEth = Convert.fromWei(currentFloor.toString(), Unit.ETHER).setScale(4, RoundingMode.HALF_UP);;
                        BigDecimal newValueEth = Convert.fromWei(listing.getListingAmount().toString(), Unit.ETHER).setScale(4, RoundingMode.HALF_UP);;

                        double percentageChange = currentFloorValueEth.subtract(newValueEth).divide(currentFloorValueEth, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
                        // We have a new floor
                        logger.info("Collection {} has had a floor change from {} to {}. {}% ", listing.getCollection(), currentFloorValueEth.setScale(4), newValueEth.setScale(4), percentageChange);
                    
                        emailService.sendEmail("imatthd01@gmail.com", "Floor Change Notification - " + listing.getCollection(), "Hello, a floor change of " + percentageChange + " has been detected on collection " + listing.getCollection() + " at " + Date.from(Instant.now()) + "\n The new floor value in ETH is: " + newValueEth.setScale(4).toString());
                    }

                    break;
                case ITEM_SOLD:

                    ItemSoldPayload itemSoldPayload = gson.fromJson(eventPayload, ItemSoldPayload.class);

                    currentFloor = getCurrentFloor(itemSoldPayload.getCollection());

                    MarketplaceListing listingSold = marketplaceListingRepository.findByMarketplaceAndOrderHashAndStatus(
                            Marketplace.OPENSEA, itemSoldPayload.getOrderHash(), ListingStatus.LISTED
                    ).orElse(null);

                    if (listingSold == null) {
                        break;
                    }

                    BigDecimal salePriceInEth = Convert.fromWei(itemSoldPayload.getSalePrice(), Unit.ETHER);

                    logger.info("{} has just sold for {} in collection {}", itemSoldPayload.getItem().getMetadata().getName(), salePriceInEth.doubleValue(), itemSoldPayload.getCollection());

                    listingSold.setStatus(ListingStatus.SOLD);
                    listingSold.setListingAmount(new BigInteger(itemSoldPayload.getSalePrice()));

                    marketplaceListingRepository.save(listingSold);

                    BigInteger floorAfterSale = getCurrentFloor(itemSoldPayload.getCollection());
                    BigDecimal floorAfterSaleEth = Convert.fromWei(floorAfterSale.toString(), Unit.ETHER).setScale(4, RoundingMode.HALF_UP);

                    int comparison = floorAfterSale.compareTo(currentFloor);

                    if (comparison == 0) {
                        break;
                    }

                    BigDecimal currentEthValue = Convert.fromWei(currentFloor.toString(), Unit.ETHER).setScale(4, RoundingMode.HALF_UP);;
                    double percentageChange = 0.00;

                    if (comparison > 0) {
                        BigDecimal increase = salePriceInEth.subtract(currentEthValue);
                        percentageChange = increase.divide(currentEthValue, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
                    } else {
                        percentageChange = currentEthValue.subtract(salePriceInEth).divide(currentEthValue, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
                    }
                    logger.info("Collection {} has had a floor change from {} to {}. {}% ", listingSold.getCollection(), currentEthValue, floorAfterSaleEth, percentageChange);
                    emailService.sendEmail("imatthd01@gmail.com", "Floor Change Notification - " + listingSold.getCollection(), "Hello, a floor change of " + percentageChange + " has been detected on collection " + listingSold.getCollection() + " at " + Date.from(Instant.now()) + "\n The new floor value in ETH is: " + floorAfterSaleEth.toString());
                
                    break;
                case ITEM_TRANSFERRED:
                    break;
                case ITEM_RECEIVED_OFFER:
                    break;
                case ITEM_RECEIVED_BID:
                    break;
                case COLLECTION_OFFER:
                    break;
                case TRAIT_OFFER:
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            logger.error("Error processing payload", e);
        }
    }

    private BigInteger getCurrentFloor(Collection collection) {
        BigInteger floor = this.marketplaceListingRepository.findFloorPrice(collection.getSlug(), Marketplace.OPENSEA.name(), ListingStatus.LISTED.name(), Date.from(Instant.now()));

        if (floor == null) {
            return BigInteger.ZERO;
        }
        return floor;
    }
}