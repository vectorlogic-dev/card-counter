package com.yourname.blackjackcounter.data.monetization

import android.app.Activity
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.yourname.blackjackcounter.domain.model.EntitlementTier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val Context.monetizationDataStore by preferencesDataStore(name = "monetization")

class PlayBillingMonetizationRepository(
    context: Context
) : MonetizationRepository, PurchasesUpdatedListener {

    private val mutableStatus = MutableStateFlow(MonetizationStatus())
    override val status = mutableStatus.asStateFlow()

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val cachedProUnlockedKey = booleanPreferencesKey("pro_unlocked")
    private val cachedFullUnlockedKey = booleanPreferencesKey("full_unlocked")
    private var proProductDetails: ProductDetails? = null
    private var fullProductDetails: ProductDetails? = null

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    init {
        scope.launch {
            val cachedValues = appContext.monetizationDataStore.data
                .map { prefs ->
                    Pair(
                        prefs[cachedProUnlockedKey] ?: false,
                        prefs[cachedFullUnlockedKey] ?: false
                    )
                }
                .first()

            val hasPro = cachedValues.first || cachedValues.second
            mutableStatus.update { current ->
                current.copy(
                    tier = if (hasPro) EntitlementTier.PRO else EntitlementTier.FREE,
                    hasFullUnlock = cachedValues.second
                )
            }
        }
        connect()
    }

    override fun refresh() {
        if (billingClient.isReady) {
            queryProductDetails()
            queryOwnedPurchases()
        } else {
            connect()
        }
    }

    override fun launchProPurchase(activity: Activity) {
        launchPurchaseFlow(
            activity = activity,
            details = proProductDetails
        )
    }

    override fun launchFullPurchase(activity: Activity) {
        launchPurchaseFlow(
            activity = activity,
            details = fullProductDetails
        )
    }

    override fun clearMessage() {
        mutableStatus.update { it.copy(message = null) }
    }

    override fun release() {
        billingClient.endConnection()
        scope.cancel()
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                handlePurchases(
                    purchases = purchases.orEmpty(),
                    fromPurchaseFlow = true
                )
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                mutableStatus.update { it.copy(isPurchaseInFlight = false) }
            }

            else -> {
                mutableStatus.update {
                    it.copy(
                        isPurchaseInFlight = false,
                        message = "Purchase failed (${billingResult.debugMessage.ifBlank { billingResult.responseCode.toString() }})."
                    )
                }
            }
        }
    }

    private fun connect() {
        if (billingClient.isReady) {
            refresh()
            return
        }

        billingClient.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    val ready = result.responseCode == BillingClient.BillingResponseCode.OK
                    mutableStatus.update {
                        it.copy(
                            isBillingReady = ready,
                            isPurchaseInFlight = false
                        )
                    }

                    if (ready) {
                        queryProductDetails()
                        queryOwnedPurchases()
                    } else {
                        setMessage(
                            "Billing unavailable (${result.debugMessage.ifBlank { result.responseCode.toString() }})."
                        )
                    }
                }

                override fun onBillingServiceDisconnected() {
                    mutableStatus.update { it.copy(isBillingReady = false) }
                }
            }
        )
    }

    private fun queryProductDetails() {
        val query = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(BillingProductIds.PRO_UNLOCK)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(BillingProductIds.FULL_UNLOCK)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()

        billingClient.queryProductDetailsAsync(query) { result, products ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                setMessage(
                    "Unable to fetch product details (${result.debugMessage.ifBlank { result.responseCode.toString() }})."
                )
                return@queryProductDetailsAsync
            }

            proProductDetails = products.firstOrNull { it.productId == BillingProductIds.PRO_UNLOCK }
            fullProductDetails = products.firstOrNull { it.productId == BillingProductIds.FULL_UNLOCK }

            val proPrice = proProductDetails?.oneTimePurchaseOfferDetails?.formattedPrice ?: "\$2.99"
            val fullPrice = fullProductDetails?.oneTimePurchaseOfferDetails?.formattedPrice ?: "\$4.99"
            mutableStatus.update {
                it.copy(
                    proDisplayPrice = proPrice,
                    fullDisplayPrice = fullPrice
                )
            }
        }
    }

    private fun queryOwnedPurchases() {
        val query = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(query) { result, purchases ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                setMessage(
                    "Unable to refresh purchases (${result.debugMessage.ifBlank { result.responseCode.toString() }})."
                )
                return@queryPurchasesAsync
            }
            handlePurchases(
                purchases = purchases,
                fromPurchaseFlow = false
            )
        }
    }

    private fun handlePurchases(
        purchases: List<Purchase>,
        fromPurchaseFlow: Boolean
    ) {
        var proOwned = false
        var fullOwned = false
        var hasPending = false

        purchases.forEach { purchase ->
            when (purchase.purchaseState) {
                Purchase.PurchaseState.PURCHASED -> {
                    if (purchase.products.contains(BillingProductIds.PRO_UNLOCK)) {
                        proOwned = true
                    }
                    if (purchase.products.contains(BillingProductIds.FULL_UNLOCK)) {
                        fullOwned = true
                        proOwned = true
                    }
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    }
                }

                Purchase.PurchaseState.PENDING -> {
                    if (
                        purchase.products.contains(BillingProductIds.PRO_UNLOCK) ||
                        purchase.products.contains(BillingProductIds.FULL_UNLOCK)
                    ) {
                        hasPending = true
                    }
                }

                else -> Unit
            }
        }

        updateEntitlements(
            hasPro = proOwned,
            hasFull = fullOwned
        )

        val successMessage = if (fromPurchaseFlow && fullOwned) {
            "Full unlock activated."
        } else if (fromPurchaseFlow && proOwned) {
            "Pro unlock activated."
        } else if (hasPending) {
            "Purchase is pending. Access unlocks when Google Play confirms it."
        } else {
            null
        }

        mutableStatus.update {
            it.copy(
                isPurchaseInFlight = false,
                message = successMessage ?: it.message
            )
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { result ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                setMessage(
                    "Could not acknowledge purchase (${result.debugMessage.ifBlank { result.responseCode.toString() }})."
                )
            }
        }
    }

    private fun updateEntitlements(
        hasPro: Boolean,
        hasFull: Boolean
    ) {
        mutableStatus.update {
            it.copy(
                tier = if (hasPro) EntitlementTier.PRO else EntitlementTier.FREE,
                hasFullUnlock = hasFull
            )
        }
        scope.launch {
            appContext.monetizationDataStore.edit { prefs ->
                prefs[cachedProUnlockedKey] = hasPro
                prefs[cachedFullUnlockedKey] = hasFull
            }
        }
    }

    private fun launchPurchaseFlow(
        activity: Activity,
        details: ProductDetails?
    ) {
        if (!mutableStatus.value.isBillingReady || details == null) {
            setMessage("Google Play Billing is not ready yet. Try again in a moment.")
            refresh()
            return
        }

        mutableStatus.update { it.copy(isPurchaseInFlight = true, message = null) }
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build()
                )
            )
            .build()

        val result = billingClient.launchBillingFlow(activity, params)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            mutableStatus.update {
                it.copy(
                    isPurchaseInFlight = false,
                    message = "Could not start purchase flow (${result.debugMessage.ifBlank { result.responseCode.toString() }})."
                )
            }
        }
    }

    private fun setMessage(message: String) {
        mutableStatus.update { it.copy(message = message) }
    }
}
