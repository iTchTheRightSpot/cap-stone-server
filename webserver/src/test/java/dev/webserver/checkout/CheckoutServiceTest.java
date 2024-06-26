package dev.webserver.checkout;

import dev.webserver.AbstractUnitTest;
import dev.webserver.cart.entity.ShoppingSession;
import dev.webserver.cart.repository.CartItemRepo;
import dev.webserver.cart.repository.ShoppingSessionRepo;
import dev.webserver.payment.projection.RaceConditionCartPojo;
import dev.webserver.shipping.entity.ShipSetting;
import dev.webserver.shipping.service.ShippingService;
import dev.webserver.tax.Tax;
import dev.webserver.tax.TaxService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CheckoutServiceTest extends AbstractUnitTest {

    private CheckoutService checkoutService;

    @Mock
    private ShippingService shippingService;
    @Mock
    private TaxService taxService;
    @Mock
    private ShoppingSessionRepo sessionRepo;
    @Mock
    private CartItemRepo cartItemRepo;

    @BeforeEach
    void setUp() {
        checkoutService = new CheckoutService(
                shippingService,
                taxService,
                sessionRepo,
                cartItemRepo
        );
        checkoutService.setCARTCOOKIE("cartcookie");
        checkoutService.setSPLIT("%");
    }

    @Test
    void createCustomObjectForShoppingSession() {
        // given
        var session = new ShoppingSession();
        session.setShoppingSessionId(1L);
        ShipSetting ship = new ShipSetting();
        ship.setCountry("nigeria");
        Tax tax = new Tax(1L, "vat", 0.075);
        Cookie[] cookies = {new Cookie("cartcookie", "this is custom cookie")};
        HttpServletRequest req = mock(HttpServletRequest.class);
        var cartItems = items.apply(3, session);

        // when
        when(req.getCookies()).thenReturn(cookies);
        when(sessionRepo.shoppingSessionByCookie(anyString())).thenReturn(Optional.of(session));
        when(cartItemRepo.cartItemsByShoppingSessionId(anyLong())).thenReturn(cartItems);
        when(shippingService.shippingByCountryElseReturnDefault(anyString())).thenReturn(ship);
        when(taxService.taxById(anyLong())).thenReturn(tax);

        // method to test
        CustomObject obj = checkoutService
                .validateCurrentShoppingSession(req, "nigeria");

        // then
        Assertions.assertEquals(obj.session(), session);
        assertEquals(obj.cartItems(), cartItems);
        Assertions.assertEquals(obj.ship(), ship);
        Assertions.assertEquals(obj.tax(), tax);

        verify(sessionRepo, times(1)).shoppingSessionByCookie(anyString());
        verify(cartItemRepo, times(1)).cartItemsByShoppingSessionId(anyLong());
        verify(shippingService, times(1)).shippingByCountryElseReturnDefault(anyString());
        verify(taxService, times(1)).taxById(anyLong());
    }

    static final BiFunction<Integer, ShoppingSession, List<RaceConditionCartPojo>> items = (num, session) -> IntStream
            .range(0, num)
            .mapToObj(op -> new RaceConditionCartPojo() {

                @Override
                public Long getProductSkuId() {
                    return (long) num;
                }

                @Override
                public String getProductSkuSku() {
                    return "sku-" + num;
                }

                @Override
                public Integer getProductSkuInventory() {
                    return num;
                }

                @Override
                public String getProductSkuSize() {
                    return "size-" + num;
                }

                @Override
                public Long getCartItemId() {
                    return (long) num;
                }

                @Override
                public Integer getCartItemQty() {
                    return num * 2;
                }

                @Override
                public Long getShoppingSessionId() {
                    return session.shoppingSessionId();
                }
            })
            .collect(Collectors.toUnmodifiableList());

}