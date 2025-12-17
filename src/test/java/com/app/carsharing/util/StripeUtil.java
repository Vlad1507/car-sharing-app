package com.app.carsharing.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.stripe.model.checkout.Session;

public class StripeUtil {

    public static Session mockSession(String sessionUrl) {
        Session session = mock(Session.class);
        when(session.getUrl()).thenReturn(sessionUrl);
        when(session.getId()).thenReturn("cs_test_session12345");
        return session;
    }
}
