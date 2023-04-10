package com.skypro.simpleaviasales.repository.specification;

import com.skypro.simpleaviasales.model.Airline;
import com.skypro.simpleaviasales.model.Airport;
import com.skypro.simpleaviasales.model.Flight;
import liquibase.repackaged.org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.util.StringUtils;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class FlightSpecifications {
    public static Specification<Flight> byAirline(String airlineName){
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(airlineName)  ) {
                return criteriaBuilder.conjunction();
            }
            Join<Flight, Airline> airline = root.join("airline");
            return criteriaBuilder.equal(airline.get("name"), airlineName);
        };
    }

    public static Specification<Flight> byAirportName(String airportName){
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(airportName)) {
                return criteriaBuilder.conjunction();
            }
            Join<Flight, Airport> airportDeparture = root.join("departureAirport");
            Join<Flight, Airport> airportArrival = root.join("arrivalAirport");
            Predicate predicateDep = criteriaBuilder.equal(airportDeparture.get("name"), airportName);
            Predicate predicateArr = criteriaBuilder.equal(airportArrival.get("name"), airportName);
            return criteriaBuilder.or(predicateDep, predicateArr);
        };
    }

    public static Specification<Flight> byCityName(String cityName) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(cityName)) {
                return criteriaBuilder.conjunction();
            }
            Join<Flight, Airport> airportDeparture = root.join("departureAirport");
            Join<Flight, Airport> airportArrival = root.join("arrivalAirport");
            Predicate predicateDep = criteriaBuilder.equal(airportDeparture.get("city"), cityName);
            Predicate predicateArr = criteriaBuilder.equal(airportArrival.get("city"), cityName);
            return criteriaBuilder.or(predicateDep, predicateArr);
        };
    }

    private static Pair<Instant, Instant> getDateCut(LocalDate localDate) {
        Instant start = localDate.atStartOfDay(ZoneId.of("UTC")).toInstant();
        Instant finish = localDate.atTime(23,59,59).atZone(ZoneId.of("UTC")).toInstant();
        return Pair.of(start, finish);
    }

    public static Specification<Flight> byArrivalDate(LocalDate arrivalDate) {
        return (root, query, criteriaBuilder) -> {
            if (arrivalDate == null) {
                return criteriaBuilder.conjunction();
            }
            Pair<Instant,Instant> dateCut = getDateCut(arrivalDate);
            return criteriaBuilder.between(root.get("arrivalDate"),dateCut.getLeft(), dateCut.getRight());
        };
    }

    public static Specification<Flight> byDepartureDate(LocalDate departureDate) {
        return ((root, query, criteriaBuilder) ->{
            if (departureDate == null) {
                return criteriaBuilder.conjunction();
            }
            Pair<Instant, Instant> dayCut = getDateCut(departureDate);
            return criteriaBuilder.between(root.get("departureDate"),dayCut.getLeft(), dayCut.getRight());
        });
    }
}
