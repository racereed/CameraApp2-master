package camera1.themaestrochef.com.cameraappfordogs.Billing;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.List;

public class QuerySKUAsync {
    BillingClient billingClient;

    public void querySkuDetailsAsync(final List<String> skuList, final SkuDetailsResponseListener skuListener) {
        // Create a runnable from the request to use inside the connection retry policy.
        Runnable queryRequest = new Runnable() {
            @Override
            public void run() {
                // Create the SkuDetailParams object
                SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                // Run the query asynchronously.
                billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                        skuListener.onSkuDetailsResponse(responseCode, skuDetailsList);
                    }

                });
            }
        };
    }
}
