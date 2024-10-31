package com.samsung.ciam.repositories;

import com.samsung.ciam.models.Channels;
import com.samsung.ciam.models.CisCountry;
import com.samsung.ciam.models.SecServingCountry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SecServingCountryRepository extends JpaRepository<SecServingCountry, Long>{

    //List<SecServingCountry> findByChannelLikeAndCountryNotAndCountryCodeInOrderByCountryAsc(String channel, String country, List<String> countryCodes);
    //List<SecServingCountry> findByChannelAndSubsidiaryNotOrderByCountryAsc(String channel, String subsidiary);
    //Optional<SecServingCountry> findFirstByChannelContainingIgnoreCaseAndCountryContainingIgnoreCase(String channel, String country);
    @Query(value = "SELECT * FROM sec_serving_countries WHERE channel LIKE :channel AND country != 'CLIMATE SOLUTIONS' AND country_code IN (:countryCodes) ORDER BY country ASC", nativeQuery = true)
    List<SecServingCountry> selectCountryCodeIn(@Param("channel") String channel, @Param("countryCodes") List<String> countryCodes);

//    @Query(value = "SELECT * FROM sec_serving_countries WHERE channel LIKE :channel AND subsidiary NOT LIKE 'SEACE' ORDER BY country asc", nativeQuery = true)
    @Query(value = "SELECT * FROM sec_serving_countries WHERE channel LIKE :channel AND (subsidiary IS NULL OR subsidiary NOT LIKE 'SEACE') ORDER BY country ASC", nativeQuery = true)
    List<SecServingCountry> selectCounrtyLikeList(@Param("channel") String channel);

    @Query(value = "SELECT * FROM sec_serving_countries WHERE channel LIKE :channel AND country ILIKE :country LIMIT 1", nativeQuery = true)
    List<SecServingCountry> selectCountryList(@Param("channel") String channel, @Param("country") String country);

    @Query(value = "SELECT * FROM sec_serving_countries WHERE country_code = :countryCode AND channel = :secChannel AND subsidiary != 'SEACE' LIMIT 1", nativeQuery = true)
    Optional<SecServingCountry> findByCountryCodeAndChannelAndSubsidiaryNot(@Param("countryCode") String countryCode, @Param("secChannel") String secChannel);

    @Query(value = "SELECT * FROM sec_serving_countries WHERE channel LIKE :channel ORDER BY country ASC", nativeQuery = true)
    List<SecServingCountry> selectCountriesByChannel(@Param("channel") String channel);

    @Query(value = "SELECT DISTINCT language_name, language FROM sec_serving_countries WHERE channel LIKE :channel AND language IN ('ko_KR', 'en_US') GROUP BY language, language_name ORDER BY language_name", nativeQuery = true)
    List<Object[]> selectLanguagesByChannel(@Param("channel") String channel);

    @Query(value = "SELECT * FROM sec_serving_countries WHERE channel LIKE :channel AND subsidiary NOT LIKE 'SEACE' ORDER BY country ASC", nativeQuery = true)
    List<SecServingCountry> selectCountriesByChannelExcludingSubsidiary(@Param("channel") String channel);

    @Query(value = "SELECT * FROM sec_serving_countries WHERE channel LIKE :channel AND country_code = :countryCode LIMIT 1", nativeQuery = true)
    Optional<SecServingCountry> selectCountryByChannelAndCountryCode(@Param("channel") String channel, @Param("countryCode") String countryCode);

    @Query(value = "SELECT * FROM sec_serving_countries WHERE id = :countryCode LIMIT 1", nativeQuery = true)
    Optional<SecServingCountry> findByCountryCode(@Param("countryCode") Long countryCode);

    @Query(value = "SELECT id FROM sec_serving_countries WHERE channel LIKE :channel AND country_code ILIKE :countryCode LIMIT 1", nativeQuery = true)
    String selectCountrySsoCode(@Param("channel") String channel, @Param("countryCode") String countryCode);

    @Query(value = "SELECT subsidiary FROM sec_serving_countries WHERE channel LIKE :channel AND country_code ILIKE :countryCode LIMIT 1", nativeQuery = true)
    String selectSubsidiary(@Param("channel") String channel, @Param("countryCode") String countryCode);

    @Query(value = "SELECT country_code FROM sec_serving_countries WHERE channel = :channel " +
            "AND subsidiary IS NOT NULL " +
            "AND subsidiary = :subsidiary ", nativeQuery = true)
    List<String> selectCountryCodeList(@Param("channel") String channel, @Param("subsidiary") String subsidiary);

    @Query(value = "SELECT * FROM sec_serving_countries WHERE channel LIKE :channel ORDER BY country", nativeQuery = true)
    List<SecServingCountry> selectCountryCodeChannel(@Param("channel") String channel);

    @Query(value = "SELECT country FROM sec_serving_countries WHERE channel LIKE :channel AND country_code ILIKE :countryCode LIMIT 1", nativeQuery = true)
    String selectCountryName(@Param("channel") String channel, @Param("countryCode") String countryCode);

    @Query(value = "SELECT * FROM sec_serving_countries WHERE channel LIKE :channel AND subsidiary ILIKE :subsidiary", nativeQuery = true)
    List<SecServingCountry> selectCountriesBySubsidiaryCodes(@Param("channel") String channel, @Param("subsidiary") String subsidiary);

    @Query(value = "SELECT * FROM sec_serving_countries WHERE channel LIKE :channel AND subsidiary  IN (:subsidiaryList)", nativeQuery = true)
    List<SecServingCountry> selectCountriesBySubsidiaryCodes(@Param("channel") String channel, @Param("subsidiaryList") List<String> subsidiaryList);
}

