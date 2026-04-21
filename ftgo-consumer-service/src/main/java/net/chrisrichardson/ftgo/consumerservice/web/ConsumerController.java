package net.chrisrichardson.ftgo.consumerservice.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerRequest;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerResponse;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/consumers")
@Tag(name = "Consumers", description = "Register consumers and look up consumer profiles.")
public class ConsumerController {

  @Autowired
  private ConsumerService consumerService;

  @Operation(summary = "Register a new consumer")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Consumer created"),
      @ApiResponse(responseCode = "400", description = "Request validation failed")
  })
  @RequestMapping(method= RequestMethod.POST)
  public CreateConsumerResponse create(@RequestBody CreateConsumerRequest request) {
    return new CreateConsumerResponse(consumerService.create(request.getName()).getId());
  }

  @Operation(summary = "Get a consumer by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Consumer found"),
      @ApiResponse(responseCode = "404", description = "No consumer with the given ID")
  })
  @RequestMapping(method= RequestMethod.GET,  path="/{consumerId}")
  public ResponseEntity<GetConsumerResponse> get(
      @Parameter(description = "Consumer identifier", example = "42") @PathVariable long consumerId) {
    return consumerService.findById(consumerId)
            .map(consumer -> new ResponseEntity<>(new GetConsumerResponse(consumer.getName()), HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }
}
