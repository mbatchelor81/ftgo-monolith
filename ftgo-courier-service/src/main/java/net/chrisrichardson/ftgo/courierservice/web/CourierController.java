package net.chrisrichardson.ftgo.courierservice.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.chrisrichardson.ftgo.common.errors.EntityNotFoundException;
import net.chrisrichardson.ftgo.common.errors.ErrorCode;
import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierRequest;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierResponse;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@Tag(name = "Couriers", description = "Register couriers, update availability, and inspect delivery plans.")
public class CourierController {

  private CourierService courierService;

  public CourierController(CourierService courierService) {
    this.courierService = courierService;
  }

  @Operation(summary = "Register a new courier")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Courier created"),
      @ApiResponse(responseCode = "400", description = "Request validation failed")
  })
  @RequestMapping(path="/couriers", method= RequestMethod.POST)
  public ResponseEntity<CreateCourierResponse> create(@Valid @RequestBody CreateCourierRequest request) {
    Courier courier = courierService.createCourier(request.getName(), request.getAddress());
    return new ResponseEntity<>(new CreateCourierResponse(courier.getId()), HttpStatus.OK);
  }

  @Operation(summary = "Update courier availability",
      description = "Marks a courier as available or unavailable for new deliveries.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Availability updated"),
      @ApiResponse(responseCode = "404", description = "Courier not found")
  })
  @RequestMapping(path="/couriers/{courierId}/availability", method= RequestMethod.POST)
  public ResponseEntity<String> updateCourierLocation(
      @Parameter(description = "Courier identifier") @PathVariable long courierId,
      @Valid @RequestBody CourierAvailability availability) {
    courierService.updateAvailability(courierId, availability.isAvailable());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Get a courier by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Courier found"),
      @ApiResponse(responseCode = "404", description = "No courier with the given ID")
  })
  @RequestMapping(path="/couriers/{courierId}", method= RequestMethod.GET)
  public Courier get(
      @Parameter(description = "Courier identifier") @PathVariable long courierId) {
    Courier courier = courierService.findCourierById(courierId);
    if (courier == null) {
      // GlobalExceptionHandler maps this to 404 + FTGO-CRR-001.
      throw new EntityNotFoundException(ErrorCode.COURIER_NOT_FOUND,
          "Courier not found: " + courierId);
    }
    return courier;
  }

}
