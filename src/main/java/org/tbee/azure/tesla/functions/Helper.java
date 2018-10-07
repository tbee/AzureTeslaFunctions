package org.tbee.azure.tesla.functions;

import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.noroomattheinn.tesla.Tesla;
import org.noroomattheinn.tesla.Vehicle;
import org.noroomattheinn.utils.Utils;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;

/**
 * 
 */
public class Helper {
	
    /**
     * @return a pair of either a vehicle,null on success or null,response on failure
     */
    public Pair<Vehicle, HttpResponseMessage> loginFindAndWakeVehicle(final HttpRequestMessage<Optional<String>> request, final ExecutionContext context) {
        context.getLogger().info("StartHVAC processed a request.");

        // Fetch environment
        String username = System.getenv("teslaUsername");
        String password = System.getenv("teslaPassword");
        String vin = System.getenv("teslaVin");
        if (username == null || password == null || vin == null) {
            return Pair.of(null, request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Not all environment variables are set").build());
        }
        
        // Login to Tesla
        Tesla tesla = new Tesla();
        if (!tesla.connect(username, password)) {
        	return Pair.of(null, request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed").build());
        }
        
        // Find the correct vehicle
        Vehicle vehicle = tesla.getVehicleByVIN(vin);
        if (vehicle == null) {
        	return Pair.of(null, request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("VIN not found").build());
        }
        
        // If the car is not awake, wake it up
        if (!vehicle.isAwake()) {
        	vehicle.wakeUp();
        	Utils.sleep(5000);            
        }
        
        // Done
        return Pair.of(vehicle, null);
    }
}
