package net.chrisrichardson.ftgo.restaurantservice.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantService;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/restaurants")
@Tag(name = "Restaurants", description = "Create restaurants and look up restaurant profiles and menus.")
public class RestaurantController {

  @Autowired
  private RestaurantService restaurantService;

  @Operation(summary = "Create a new restaurant")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Restaurant created"),
      @ApiResponse(responseCode = "400", description = "Request validation failed")
  })
  @RequestMapping(method = RequestMethod.POST)
  public CreateRestaurantResponse create(@RequestBody CreateRestaurantRequest request) {
    Restaurant r = restaurantService.create(request);
    return new CreateRestaurantResponse(r.getId());
  }

  @Operation(summary = "Get a restaurant by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Restaurant found"),
      @ApiResponse(responseCode = "404", description = "No restaurant with the given ID")
  })
  @RequestMapping(method = RequestMethod.GET, path = "/{restaurantId}")
  public ResponseEntity<GetRestaurantResponse> get(
      @Parameter(description = "Restaurant identifier") @PathVariable long restaurantId) {
    return restaurantService.findById(restaurantId)
            .map(r -> new ResponseEntity<>(makeGetRestaurantResponse(r), HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  private GetRestaurantResponse makeGetRestaurantResponse(Restaurant r) {
    return new GetRestaurantResponse(r.getId(), r.getName());
  }


}
