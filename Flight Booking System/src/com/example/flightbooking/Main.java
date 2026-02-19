package com.example.flightbooking;

import java.util.ArrayList;
import java.util.List;

public class Main {
    static class Flight {
        final String id;
        final String origin;
        final String destination;
        int seatsAvailable;
        final double price;

        Flight(String id, String origin, String destination, int seatsAvailable, double price) {
            this.id = id;
            this.origin = origin;
            this.destination = destination;
            this.seatsAvailable = seatsAvailable;
            this.price = price;
        }
    }

    static class BookingResult {
        final Flight flight;
        final String seat;
        final double amount;

        BookingResult(Flight flight, String seat, double amount) {
            this.flight = flight;
            this.seat = seat;
            this.amount = amount;
        }
    }

    static class BookingService {
        final List<Flight> flights = new ArrayList<>();

        BookingService() {
            flights.add(new Flight("AI101", "Mumbai", "Delhi", 12, 5999.0));
            flights.add(new Flight("UK202", "Pune", "Bengaluru", 8, 4499.0));
            flights.add(new Flight("6E303", "Delhi", "Goa", 5, 6999.0));
        }

        List<Flight> listFlights() {
            return flights;
        }

        BookingResult bookFirstAvailable(String seat) {
            Flight f = flights.stream().filter(x -> x.seatsAvailable > 0).findFirst().orElse(null);
            if (f == null) return null;
            f.seatsAvailable -= 1;
            return new BookingResult(f, seat, f.price);
        }
    }

    public static void main(String[] args) {
        BookingService service = new BookingService();
        System.out.println("Available flights:");
        for (Flight f : service.listFlights()) {
            System.out.println(f.id + " " + f.origin + " -> " + f.destination + " seats:" + f.seatsAvailable + " price:" + f.price);
        }
        BookingResult result = service.bookFirstAvailable("12A");
        if (result == null) {
            System.out.println("No seats available.");
            return;
        }
        System.out.println("Booking confirmed:");
        System.out.println("Flight: " + result.flight.id);
        System.out.println("Route: " + result.flight.origin + " -> " + result.flight.destination);
        System.out.println("Seat: " + result.seat);
        System.out.println("Amount: " + result.amount);
    }
}
