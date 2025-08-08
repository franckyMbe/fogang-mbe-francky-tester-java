package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {
	
	
	/*
	 * implementation of the free parking feature for durations of less than 30 minutes 
	 */
	
	private void freeParkingService(double temps, Ticket ticket) {
		
		if(temps<=(30.0/60)) ticket.setPrice(0); // initialisation du prix du ticket a zero 
		temps=0;
	}
	
    /*
	 *ticket de reduction pour les vehicule recurrent 
	 */
	private void priceReduction(double price,Ticket ticket,boolean discount) {
		if (discount == true) {
			System.out.println("5% discount applied on ticket!");
			double newPrice = (price*(0.95));
			ticket.setPrice(newPrice);
		}
	}

	
	
    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        double inHour =  ticket.getInTime().getTime();
        double outHour = ticket.getOutTime().getTime();
        
        
        // obtention de la duree en heure 
        double duration = ((outHour - inHour)/3600000);


        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                freeParkingService(duration, ticket);               // 30 minute de parking gratuit pour les voitures
                priceReduction(ticket.getPrice(), ticket,discount); //  reduction du prix de 5%
                break;
            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                freeParkingService(duration, ticket);        // 30 minute de parking gratuit pour les motos
                priceReduction(ticket.getPrice(), ticket,discount); //  reduction du prix de 5%
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
	
	
	public void calculateFare(Ticket ticket) {
		boolean discount=false;
		this.calculateFare(ticket, discount);
	}
	
}