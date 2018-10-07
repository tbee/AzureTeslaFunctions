package org.tbee.azure.tesla.functions;

import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.noroomattheinn.tesla.Vehicle;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

/**
 * Azure Functions with HTTP Trigger.
 */
public class EndHVAC {
    /**
     * 
     */
    @FunctionName("EndHVAC")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = { HttpMethod.POST }, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request, final ExecutionContext context) {
        context.getLogger().info("EndHVAC processed a request.");
        Helper helper = new Helper();

        // Get the vehicle
        Pair<Vehicle, HttpResponseMessage> vehicleOrResponse = helper.loginFindAndWakeVehicle(request, context);
        if (vehicleOrResponse.getRight() != null) {
        	return vehicleOrResponse.getRight();
        }
        Vehicle vehicle = vehicleOrResponse.getLeft();
        
        // Get the current state
        String shiftState = vehicle.queryDrive().shiftState;
        if (!"P".equals(shiftState)) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(vehicle.getVIN() + ": HVAC not stopped because car is not in park (" + shiftState + ").").build();
        }
        
        // Do it
        vehicle.stopAC();
        return request.createResponseBuilder(HttpStatus.OK).body(vehicle.getVIN() + ": HVAC stopped.").build();
    }
}
