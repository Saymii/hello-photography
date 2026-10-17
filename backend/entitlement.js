/** Pure validation policy: responses come from Google Play, never from client claims. */
export function deriveEntitlement(subscription, products, now = Date.now()) {
 const allowed = new Set(products);
 const lines = subscription.lineItems || [];
 const eligible = lines.filter(line => allowed.has(line.productId) && Number.isFinite(Date.parse(line.expiryTime)));
 // Cancelled subscriptions retain access until expiry. Pending and on-hold do not.
 const stateAllowed = ['SUBSCRIPTION_STATE_ACTIVE', 'SUBSCRIPTION_STATE_IN_GRACE_PERIOD', 'SUBSCRIPTION_STATE_CANCELED'].includes(subscription.subscriptionState);
 const expiresAtEpochMillis = Math.max(0, ...eligible.map(line => Date.parse(line.expiryTime)));
 return { premium: stateAllowed && expiresAtEpochMillis > now, expiresAtEpochMillis };
}
