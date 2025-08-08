package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;
    private static boolean discount;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeAll
    private static void setDiscount() {
    	discount = false;
    }
    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    @Test
    public void calculateFareCar(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR);
 
    }

    @Test
    public void calculateFareBike(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareUnkownType(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithFutureInTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() + (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        double price=0.75 * Fare.BIKE_RATE_PER_HOUR;
        assertEquals(price, ticket.getPrice() );
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        double price=0.75 * Fare.CAR_RATE_PER_HOUR;
        assertEquals( price, ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  24 * 60 * 60 * 1000) );//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        double price=24 * Fare.CAR_RATE_PER_HOUR;
        assertEquals( price , ticket.getPrice());
    }

    
    

    /*
     * test de parking gratuit pour les vehicules de moins ou egale a 30 minutes
     */
    
    
    
    @Test
    public void calculateFareCarWithLessThan30minutesParkingTime() {
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (30*60*1000) );//30 min parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( 0.0, ticket.getPrice());
    }
    
    @Test
    public void calculateFareBikeWithLessThan30minutesParkingTime() {
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (30*60*1000) );//30 min parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( 0.0, ticket.getPrice());
    }
    
    
    /*
     *  Testes sur les  5% de remises pour les utilisateur recurrent 
     */
    
    
    // cas d'une voiture  
    @Test
    public void calculateFareCarWithDiscount() {
    	Date intTime = new Date();
    	intTime.setTime(System.currentTimeMillis() - (50*60*1000));// heure d'entree 
    	Date outTime = new Date();
    	ParkingSpot parkingSpot = new ParkingSpot(1,ParkingType.CAR,false);
        ticket.setInTime(intTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        discount=true;
        fareCalculatorService.calculateFare(ticket,discount);   
        //                    |prix normale a payer          | moins | 5% du prix initiale du ticket
        double ticketDiscount= ((50.0/60) * Fare.CAR_RATE_PER_HOUR*0.95);
        assertEquals(ticketDiscount, ticket.getPrice());
    }
    
	// cas d'moto
    @Test
    public void calculateFareBikeWithDiscount() {
    	Date intTime = new Date();
    	intTime.setTime(System.currentTimeMillis() - (50*60*1000));// heure d'entree 
    	Date outTime = new Date();
    	ParkingSpot parkingSpot = new ParkingSpot(1,ParkingType.BIKE,false);
        ticket.setInTime(intTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        discount=true;
        fareCalculatorService.calculateFare(ticket,discount); 
        //ticket de reduction =|prix normale a payer          | moins | 5% du prix initiale du ticket
        double ticketDiscount=((50.0/60) * Fare.BIKE_RATE_PER_HOUR*0.95);
        assertEquals(ticketDiscount, ticket.getPrice());
    }
}
