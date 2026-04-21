package net.chrisrichardson.ftgo.consumerservice.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.chrisrichardson.ftgo.common.errors.EntityNotFoundException;
import net.chrisrichardson.ftgo.common.errors.ErrorCode;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerRequest;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerResponse;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
  public CreateConsumerResponse create(@Valid @RequestBody CreateConsumerRequest request) {
    return new CreateConsumerResponse(consumerService.create(request.getName()).getId());
  }

  @Operation(summary = "Get a consumer by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Consumer found"),
      @ApiResponse(responseCode = "404", description = "No consumer with the given ID")
  })
  @RequestMapping(method= RequestMethod.GET,  path="/{consumerId}")
  public GetConsumerResponse get(
      @Parameter(description = "Consumer identifier", example = "42") @PathVariable long consumerId) {
    // Uses shared GlobalExceptionHandler: missing entity -> 404 with
    // FTGO-CON-001, no manual ResponseEntity branching required.
    return consumerService.findById(consumerId)
            .map(consumer -> new GetConsumerResponse(consumer.getName()))
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CONSUMER_NOT_FOUND,
                "Consumer not found: " + consumerId));
  }
}
