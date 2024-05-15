package ai.utiliti.bes.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Functions;

public enum MarketplaceCollections {

    HERO_GALAXY_HEROES("hero-galaxy-heroes"),
    MUTANT_APE_YACHT_CLUB("mutant-ape-yacht-club"),
    VV_CHECKS("vv-checks"),
    NAKAMIGOS("nakamigos"),
    ;

    private String slug;

    MarketplaceCollections(String slug) {
        this.slug = slug;
    }

    public String getSlug() {
        return slug;
    }

    private static Map<String, MarketplaceCollections> slugMap = Arrays.asList(MarketplaceCollections.values()).stream().collect(Collectors.toMap(MarketplaceCollections::getSlug, Functions.identity()));

    public static MarketplaceCollections bySlug(String slug) {
        return slugMap.get(slug);
    }
}
