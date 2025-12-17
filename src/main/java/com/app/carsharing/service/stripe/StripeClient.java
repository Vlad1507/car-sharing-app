package com.app.carsharing.service.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StripeClient {
    private static final String BASE_URL = "https://api.stripe.com/v1";
    @Value("${stripe.secret.key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    public Session createSessionCheckout(
            Long amount,
            String currency,
            String successUrl,
            String canceledUrl,
            String rentalId
    ) throws StripeException {
        SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams
                .LineItem.PriceData.ProductData.builder()
                .setName("Car rental by id: " + rentalId)
                .build();
        SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData
                .builder()
                .setCurrency(currency)
                .setUnitAmount(amount)
                .setProductData(productData)
                .build();
        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(priceData)
                .build();
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCurrency(currency)
                .setSuccessUrl(successUrl)
                .setCancelUrl(canceledUrl)
                .addLineItem(lineItem)
                .putMetadata("rental_id", rentalId)
                .build();
        return Session.create(params);
    }
}
