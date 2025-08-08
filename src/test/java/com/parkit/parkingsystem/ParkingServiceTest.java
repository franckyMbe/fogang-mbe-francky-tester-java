package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @Mock
    private ParkingSpot parkingSpot;
    @Mock
    private FareCalculatorService fareCalculatorService;
    @Mock
    private Ticket ticket;
    

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
            // stubbing sur la methode calculateFare
            lenient().doAnswer(invocation->{
            	Ticket t=invocation.getArgument(0);
            	boolean discount=invocation.getArgument(1);
            	if(discount) {
            		t.setPrice(1.425);
            	}else{
            		t.setPrice(1.5);
            	}
            	return null;
            }).when(fareCalculatorService).calculateFare(any(Ticket.class), anyBoolean());
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    /*
     * different process Exiting Vehicle
     */
    
    @Test
    public void processExitingVehicle_WithFivePercentDiscountTest(){
    	
    	//GIVEN
    	when(ticketDAO.getNbTicket(anyString())).thenReturn(2);

    	//WHEN
        parkingService.processExitingVehicle();
        Ticket modifiedTicket= ticketDAO.getTicket("ABCDEF");
        
        //THEN
        verify(ticketDAO,times(1)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        // verify that the discound is applied
        assertEquals(modifiedTicket.getPrice(),1.425,0.00001);
    }
    
    @Test
    public void processExitingVehicle_WithFivePercentDiscount_NotFoundTicketTest(){
    	
    	//GIVEN
    	when(ticketDAO.getNbTicket(anyString())).thenReturn(2);
    	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

    	//WHEN	
        parkingService.processExitingVehicle();
        Ticket modifiedTicket= ticketDAO.getTicket("ABCDEF");
        //THEN
        verify(ticketDAO,times(1)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        // verify that the discound is applied
      assertEquals(modifiedTicket.getPrice(),1.425,0.00001);
    }
    
    
    @Test
    public void processExitingVehicle_WithoutFivePercentDiscountTest(){
    	
    	//GIVEN
    	when(ticketDAO.getNbTicket(anyString())).thenReturn(1);


    	//WHEN
        parkingService.processExitingVehicle();
        Ticket modifiedTicket= ticketDAO.getTicket("ABCDEF");
        
        //THEN
        verify(ticketDAO,times(1)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        // verify that the discound is applied
        assertEquals(modifiedTicket.getPrice(),1.5,0.00001);
    }
    
    @Test
    public void processExitingVehicle_WithoutFivePercentDiscount_NotFoundTicketTest(){
    	
    	//GIVEN
    	when(ticketDAO.getNbTicket(anyString())).thenReturn(1);
    	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

    	//WHEN
    	
        parkingService.processExitingVehicle();
        Ticket modifiedTicket= ticketDAO.getTicket("ABCDEF");
        //THEN
        verify(ticketDAO,times(1)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        // verify that the discound is applied
      assertEquals(modifiedTicket.getPrice(),1.5,0.00001);
    }

    
    
    /*
     * Process Incoming Vehicle test
     */
    
    @Test
	public void testProcessIncomingVehicle() {

		// GIVEN
		when(inputReaderUtil.readSelection()).thenReturn(1); // selection a car
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
		when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

		// WHEN
		parkingService.processIncomingVehicle();
		Ticket t =ticketDAO.getTicket("ABCDEF");

		// THEN
		verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
		verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO,times(1)).getNbTicket("ABCDEF"); // message for the regular clients
        //Check if the vehicle has been registered in BD
        assertThat(t.getId()).isNotNull();
        assertThat(t.getVehicleRegNumber()).isEqualTo("ABCDEF");

	}
     
    /*
     * process Exiting Vehicle Test UnableUpdate
     */
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
    
    /*
     * Get Next ParkingNumber If Available
     */
    @Test
    public void getNextParkingNumberIfAvailableTest() {
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
    
    /*
     * Get Next Parking Number If Available Parking Number Not Found test
     */
    @Test
    public void getNextParkingNumberIfAvailableParkingNumberNotFoundTest() {
    	//GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1); // 1 = CAR
    	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);
    	//WHEN
    	ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();
    	//THEN
    	verify(parkingSpotDAO,times(1)).getNextAvailableSlot(any(ParkingType.class)); //check that the spots are unavailable;
    	assertThat(result).isNull();
    }
    
    /*
     * Get Next Parking Number If Available Parking Number WrongArgument test
     */
    @Test
    public void getNextParkingNumberIfAvailableParkingNumberWrongArgumentTest() {
    	//GIVEN
    	when(inputReaderUtil.readSelection()).thenReturn(3); // valeur entree par l'utilisateur

    	//WHEN
    	parkingService.getNextParkingNumberIfAvailable();		
    	
    	//THEN
    	assertThat(parkingService.getNextParkingNumberIfAvailable()).isNull();

    }
    

}
