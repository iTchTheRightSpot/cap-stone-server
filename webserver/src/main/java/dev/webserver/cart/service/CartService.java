package dev.webserver.cart.service;

import dev.webserver.aws.S3Service;
import dev.webserver.cart.dto.CartDTO;
import dev.webserver.cart.entity.CartItem;
import dev.webserver.cart.entity.ShoppingSession;
import dev.webserver.cart.repository.CartItemRepo;
import dev.webserver.cart.repository.ShoppingSessionRepo;
import dev.webserver.cart.response.CartResponse;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.exception.CustomInvalidFormatException;
import dev.webserver.exception.CustomNotFoundException;
import dev.webserver.exception.OutOfStockException;
import dev.webserver.product.entity.Product;
import dev.webserver.product.entity.ProductSku;
import dev.webserver.product.service.ProductSkuService;
import dev.webserver.util.CustomUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final int expire = 2; // cart expiration

    @Setter @Getter
    @Value(value = "${cart.split}")
    private String split;
    @Setter @Getter
    @Value(value = "${aws.bucket}")
    private String BUCKET;
    @Setter @Getter
    @Value("${cart.cookie.name}")
    private String CARTCOOKIE;
    @Setter @Getter
    @Value(value = "${server.servlet.session.cookie.secure}")
    private boolean COOKIESECURE;
    @Setter @Getter
    @Value("${shopping.session.expiration.bound}")
    private long bound;

    private final ShoppingSessionRepo shoppingSessionRepo;
    private final CartItemRepo cartItemRepo;
    private final ProductSkuService productSKUService;
    private final S3Service s3Service;

    /**
     * Updates the expiration of a cookie if it is within the expiration period.
     * If the cookie is valid and is within {@link #bound}, cookie is updated and
     * sent back in the response.
     *
     * @param res    the HttpServletResponse object to add the updated cookie to
     * @param cookie the Cookie object to validate and update
     * @throws CustomInvalidFormatException if the cookie value is invalid or cannot
     * be parsed.
     */
    public void validateCookieExpiration(HttpServletResponse res, Cookie cookie) {
        try {
            String[] arr = cookie.getValue().split(split);

            Date now = CustomUtil.toUTC(new Date());
            long parsed = Long.parseLong(arr[1]);

            Date cookieDate = CustomUtil
                    .toUTC(Date.from(Instant.ofEpochSecond(parsed)));

            Duration between = Duration.between(now.toInstant(), cookieDate.toInstant());

            long hours = between.toHours();

            if (hours <= bound) {
                // update cookie expiry
                Instant expiration = Instant.now().plus(expire, DAYS);
                long maxAgeInSeconds = Instant.now().until(expiration, ChronoUnit.SECONDS);

                String value = arr[0] + CustomUtil
                        .toUTC(Date.from(expiration)).toInstant().getEpochSecond();

                this.shoppingSessionRepo
                        .updateShoppingSessionExpiry(arr[0], CustomUtil.toUTC(Date.from(expiration)));

                // cookie
                cookie.setValue(value);
                cookie.setPath("/");
                cookie.setMaxAge((int) maxAgeInSeconds);

                res.addCookie(cookie);
            }
        } catch (RuntimeException ex) {
            log.error("validateCookieExpiration method, {}", ex.getMessage());
            throw new CustomInvalidFormatException("invalid cookie");
        }
    }

    /**
     * Retrieves all {@link CartItem} objects asynchronously. These objects are all
     * of the {@link ProductSku} that contain in a users shopping cart.
     * <p>
     * If a cart cookie exists in the request, the method retrieves the cart items associated
     * with the cookie.If custom cookie exists, a new cookie is created and added to the
     * response.
     *
     * @param currency the currency for which cart items should be retrieved.
     * @param req the HttpServletRequest object to retrieve the cart cookie.
     * @param res the HttpServletResponse object to add the new cart cookie if needed.
     * @return a {@link CompletableFuture} containing a list of {@link CartResponse} objects
     * representing the {@link CartItem}.
     * @throws CustomInvalidFormatException if the cart cookie value is invalid or cannot be parsed.
     */
    public CompletableFuture<List<CartResponse>> cartItems(
            SarreCurrency currency,
            HttpServletRequest req,
            HttpServletResponse res
    ) {
        Cookie cookie = CustomUtil.cookie(req, CARTCOOKIE);

        if (cookie == null) {
            // cookie value
            Instant expiration = Instant.now().plus(expire, DAYS);
            long maxAgeInSeconds = Instant.now().until(expiration, ChronoUnit.SECONDS);
            String value = UUID.randomUUID() + split + expiration.getEpochSecond();

            // cookie
            Cookie c = new Cookie(CARTCOOKIE, value);
            c.setMaxAge((int) maxAgeInSeconds);
            c.setHttpOnly(true);
            c.setPath("/");
            c.setSecure(COOKIESECURE);

            res.addCookie(c);

            return CompletableFuture.completedFuture(List.of());
        }

        validateCookieExpiration(res, cookie);

        String[] arr = cookie.getValue().split(split);

        var futures = shoppingSessionRepo
                .cartItemsByCookieValue(currency, arr[0])
                .stream()
                .map(db -> (Supplier<CartResponse>) () -> new CartResponse(
                        db.getUuid(),
                        s3Service.preSignedUrl(BUCKET, db.getKey()),
                        db.getName(),
                        db.getPrice(),
                        db.getCurrency(),
                        db.getColour(),
                        db.getSize(),
                        db.getSku(),
                        db.getQty(),
                        db.getWeight(),
                        db.getWeightType()
                ))
                .toList();

        return CustomUtil.asynchronousTasks(futures, CartService.class)
                .thenApply(v -> v.stream().map(Supplier::get).toList());
    }

    /**
     * Adds a {@link ProductSku} to a user's shopping cart by creating or updating a
     * {@link ShoppingSession}.
     * <p>
     * Retrieves a unique cookie associated to a user's device from the {@link HttpServletRequest}.
     * This cookie is used to identify the user's {@link ShoppingSession}.
     * <p>
     * If the specified {@link ProductSku} does not exist, a {@link CustomNotFoundException}
     * is thrown. If the {@link Product} is out of stock
     * or the requested quantity exceeds available inventory, an {@link OutOfStockException}
     * is thrown.
     *
     * @param dto the {@link CartDTO} containing information about the {@link ProductSku}
     *            and quantity.
     * @param req the {@link HttpServletRequest} containing the unique cookie associated with
     *            the user's device.
     * @throws CustomNotFoundException      if the specified {@link ProductSku} does not exist.
     * @throws OutOfStockException          if the {@link ProductSku} is out of stock or the
     * requested quantity exceeds available inventory.
     * @throws CustomInvalidFormatException if the cookie is invalid.
     */
    public void create(CartDTO dto, HttpServletRequest req) {
        Cookie cookie = CustomUtil.cookie(req, CARTCOOKIE);

        if (cookie == null) {
            throw new CustomNotFoundException("No cookie found. Kindly refresh window");
        }

        var productSku = this.productSKUService.productSkuBySku(dto.sku());

        int qty = productSku.getInventory();

        if (qty <= 0 || dto.qty() > qty) {
            throw new OutOfStockException("Product or selected quantity is out of stock.");
        }

        String[] arr = cookie.getValue().split(split);

        Optional<ShoppingSession> session = shoppingSessionRepo.shoppingSessionByCookie(arr[0]);

        if (session.isEmpty()) {
            try {
                long parsed = Long.parseLong(arr[1]);
                createNewShoppingSession(
                        arr[0],
                        Date.from(Instant.ofEpochSecond(parsed)),
                        dto.qty(),
                        productSku
                );
            } catch (RuntimeException ex) {
                log.error("create method , {}", ex.getMessage());
                throw new CustomInvalidFormatException("invalid cookie");
            }
        } else {
            addToExistingShoppingSession(session.get(), dto.qty(), productSku);
        }
    }

    /**
     * Creates a new shopping session
     */
    private void createNewShoppingSession(String cookie, Date expiration, int qty, ProductSku sku) {
        var session = this.shoppingSessionRepo.save(
                new ShoppingSession(
                        cookie,
                        CustomUtil.toUTC(new Date()),
                        CustomUtil.toUTC(expiration),
                        new HashSet<>(),
                        new HashSet<>()
                )
        );

        this.cartItemRepo.save(new CartItem(qty, session, sku));
    }

    /**
     * Creates or updates a CartItem
     */
    private void addToExistingShoppingSession(ShoppingSession session, int qty, ProductSku sku) {
        var optional = cartItemRepo
                .cartItemByShoppingSessionIdAndProductSkuSku(
                        session.shoppingSessionId(),
                        sku.getSku()
                );

        if (optional.isEmpty()) {
            this.cartItemRepo.save(new CartItem(qty, session, sku));
        } else {
            this.cartItemRepo.updateCartItemQtyByCartId(optional.get().getCartId(), qty);
        }
    }

    /**
     * Deletes a {@link CartItem} from associated to a {@link ShoppingSession}.
     *
     * @param req the HttpServletRequest object containing a unique cookie for
     *            every device that visit out application.
     * @param sku unique {@link ProductSku}.
     * */
    public void deleteFromCart(HttpServletRequest req, String sku) {
        Cookie cookie = CustomUtil.cookie(req, CARTCOOKIE);

        if (cookie == null) {
            return;
        }

        String[] arr = cookie.getValue().split(split);

        this.cartItemRepo.deleteCartItemByCookieAndSku(arr[0], sku);
    }

}