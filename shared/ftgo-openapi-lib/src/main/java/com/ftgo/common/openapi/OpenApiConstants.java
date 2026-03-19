package com.ftgo.common.openapi;

/**
 * Shared constants for OpenAPI tag names and descriptions.
 * Services use these constants in @Tag annotations on controllers
 * to ensure consistent API documentation across the platform.
 */
public final class OpenApiConstants {

    private OpenApiConstants() {
        // Utility class
    }

    // --- Tag Names ---
    public static final String TAG_ORDERS = "Orders";
    public static final String TAG_CONSUMERS = "Consumers";
    public static final String TAG_RESTAURANTS = "Restaurants";
    public static final String TAG_COURIERS = "Couriers";

    // --- Tag Descriptions ---
    public static final String TAG_ORDERS_DESC = "Order lifecycle management — create, accept, prepare, pickup, deliver, cancel, revise";
    public static final String TAG_CONSUMERS_DESC = "Consumer registration and validation";
    public static final String TAG_RESTAURANTS_DESC = "Restaurant registration and menu management";
    public static final String TAG_COURIERS_DESC = "Courier registration and availability management";

    // --- Common Response Descriptions ---
    public static final String RESP_200 = "Successful operation";
    public static final String RESP_201 = "Resource created successfully";
    public static final String RESP_400 = "Invalid request parameters";
    public static final String RESP_404 = "Resource not found";
    public static final String RESP_409 = "Conflict — invalid state transition";
    public static final String RESP_500 = "Internal server error";
}
