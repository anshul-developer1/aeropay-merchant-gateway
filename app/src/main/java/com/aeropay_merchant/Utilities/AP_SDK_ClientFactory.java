package com.aeropay_merchant.Utilities;

import android.content.Context;

import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;

public class AP_SDK_ClientFactory {
    private static volatile AWSAppSyncClient client;

    public synchronized static AWSAppSyncClient getInstance(Context context) {
        if (client == null) {
            AWSConfiguration awsConfig = new AWSConfiguration(context);

            client = AWSAppSyncClient.builder()
                    .context(context)
                    .awsConfiguration(awsConfig)
                    .build();
        }
        return client;
    }
}