package com.vectorlogic.blackjackcounter.data.monetization

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingUnlockManager(
    context: Context
) : PurchasesUpdatedListener {

    private val billingClient: BillingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private val _ownedSystemIds = MutableStateFlow<Set<String>>(emptySet())
    val ownedSystemIds: StateFlow<Set<String>> = _ownedSystemIds.asStateFlow()

    private val _priceBySystemId = MutableStateFlow<Map<String, String>>(emptyMap())
    val priceBySystemId: StateFlow<Map<String, String>> = _priceBySystemId.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private var started = false
    private var productDetailsByProductId: Map<String, ProductDetails> = emptyMap()

    fun start() {
        if (started) {
            if (billingClient.isReady) {
                syncCatalogAndPurchases()
            }
            return
        }
        started = true
        connect()
    }

    fun shutdown() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
        started = false
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun launchPurchase(activity: Activity, systemId: String): Boolean {
        val productId = CountingSystemMonetization.productIdForSystem(systemId)
        if (productId == null) {
            setStatus("This system is already free.")
            return false
        }

        if (!billingClient.isReady) {
            connect()
            setStatus("Store is connecting. Try again in a moment.")
            return false
        }

        val productDetails = productDetailsByProductId[productId]
        if (productDetails == null) {
            queryProductDetails()
            setStatus("Store data not ready yet. Please try again.")
            return false
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val launchResult = billingClient.launchBillingFlow(activity, flowParams)
        if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
            setStatus("Could not start purchase (${launchResult.responseCode}).")
            return false
        }
        return true
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                applyPurchases(purchases.orEmpty())
                refreshPurchases()
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                setStatus("Purchase canceled.")
            }
            else -> {
                setStatus("Purchase failed (${billingResult.responseCode}).")
            }
        }
    }

    private fun connect() {
        if (billingClient.isReady) {
            syncCatalogAndPurchases()
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    syncCatalogAndPurchases()
                } else {
                    setStatus("Store unavailable (${billingResult.responseCode}).")
                }
            }

            override fun onBillingServiceDisconnected() {
                setStatus("Store disconnected. Reconnecting when needed.")
            }
        })
    }

    private fun syncCatalogAndPurchases() {
        queryProductDetails()
        refreshPurchases()
    }

    private fun queryProductDetails() {
        val products = CountingSystemMonetization.paidSystemProducts.map { unlock ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(unlock.productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        if (products.isEmpty()) {
            _priceBySystemId.value = emptyMap()
            return
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(products)
            .build()

        billingClient.queryProductDetailsAsync(params) { result, productDetailsList ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                setStatus("Could not load prices (${result.responseCode}).")
                return@queryProductDetailsAsync
            }

            productDetailsByProductId = productDetailsList.associateBy { it.productId }
            _priceBySystemId.value = productDetailsList.mapNotNull { details ->
                val systemId = CountingSystemMonetization.systemIdForProduct(details.productId)
                    ?: return@mapNotNull null
                val priceLabel = details.oneTimePurchaseOfferDetails?.formattedPrice
                    ?: return@mapNotNull null
                systemId to priceLabel
            }.toMap()
        }
    }

    private fun refreshPurchases() {
        val purchasesParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(purchasesParams) { result, purchases ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                setStatus("Could not refresh purchases (${result.responseCode}).")
                return@queryPurchasesAsync
            }
            applyPurchases(purchases)
        }
    }

    private fun applyPurchases(purchases: List<Purchase>) {
        val ownedSystems = purchases
            .asSequence()
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            .flatMap { purchase ->
                acknowledgeIfNeeded(purchase)
                purchase.products.asSequence()
            }
            .mapNotNull { productId -> CountingSystemMonetization.systemIdForProduct(productId) }
            .toSet()
        _ownedSystemIds.value = ownedSystems
    }

    private fun acknowledgeIfNeeded(purchase: Purchase) {
        if (purchase.isAcknowledged) return
        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(acknowledgeParams) { result ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                setStatus("Purchase acknowledgment failed (${result.responseCode}).")
            }
        }
    }

    private fun setStatus(message: String) {
        _statusMessage.value = message
    }
}
