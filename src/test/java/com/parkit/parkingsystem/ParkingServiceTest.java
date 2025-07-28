package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;
    

    @BeforeEach
    private void setUpPerTest() {
        try {
            lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            lenient().when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            lenient().when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest(){
    	
    	//GIVEN
    	when(ticketDAO.getNbTicket(anyString())).thenReturn(2);

    	//WHEN
        parkingService.processExitingVehicle();
        
        //THEN
        verify(ticketDAO, times(1)).getTicket(anyString());
        verify(ticketDAO,times(1)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }
    
    @Test
	public void testProcessIncomingVehicle() {

		// GIVEN
		when(inputReaderUtil.readSelection()).thenReturn(1); // selection a car
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
		when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

		// WHEN
		parkingService.processIncomingVehicle();

		// THEN
		verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
		verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO,times(1)).getNbTicket("ABCDEF"); // message for the regular clients

	}
    
    
    @Test
    public void processExitingVehicleTestUnableUpdate() {
    	
    	//GIVEN
    	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

    	//WHEN
        parkingService.processExitingVehicle();
        
        //THEN
        verify(ticketDAO,times(1)).updateTicket(any(Ticket.class));
        assertThat(ticketDAO.updateTicket(any(Ticket.class))).isEqualTo(false);
    }
    
    @Test
    public void testGetNextParkingNumberIfAvailable() {
    	//GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1); // 1 = CAR
    	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
    	//WHEN
    	ParkingSpot parkingSpot=parkingService.getNextParkingNumberIfAvailable();
    	//THEN
    	verify(parkingSpotDAO,times(1)).getNextAvailableSlot(any(ParkingType.class)); // check that the spot with ID equal to 1 is free
    	assertThat(parkingSpot.getId()).isEqualTo(1); // check that the spot obtained has an id equal to 1 
    	assertThat(parkingSpot.isAvailable()).isEqualTo(true);
    }
    
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
    	//GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1); // 1 = CAR
    	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);
    	//WHEN
    	parkingService.getNextParkingNumberIfAvailable();
    	//THEN
    	verify(parkingSpotDAO,times(1)).getNextAvailableSlot(any(ParkingType.class)); //check that the spots are unavailable;
    }
    
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
    	//GIVEN
    	when(inputReaderUtil.readSelection()).thenReturn(3); // valeur entree par l'utilisateur

    	//WHEN

    	parkingService.getNextParkingNumberIfAvailable();		

    	//THEN
    	assertThat(parkingService.getNextParkingNumberIfAvailable()).isNull();

    }
    

}
