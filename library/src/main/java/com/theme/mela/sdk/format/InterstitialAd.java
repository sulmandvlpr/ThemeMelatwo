package com.theme.mela.sdk.format;

import static com.theme.mela.sdk.util.Constant.ADCOLONY;
import static com.theme.mela.sdk.util.Constant.ADMOB;
import static com.theme.mela.sdk.util.Constant.AD_STATUS_ON;
import static com.theme.mela.sdk.util.Constant.APPLOVIN;
import static com.theme.mela.sdk.util.Constant.MOPUB;
import static com.theme.mela.sdk.util.Constant.NONE;



import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAdOptions;
import com.adcolony.sdk.AdColonyInterstitial;
import com.adcolony.sdk.AdColonyInterstitialListener;
import com.adcolony.sdk.AdColonyZone;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.theme.mela.sdk.util.Tools;


import java.util.concurrent.TimeUnit;

public class InterstitialAd {

    public static class Builder {

        private static final String TAG = "AdNetwork";
        private final Activity activity;
        private com.google.android.gms.ads.interstitial.InterstitialAd adMobInterstitialAd;
        private MaxInterstitialAd maxInterstitialAd;
        public MoPubInterstitial mInterstitial;
        private AdColonyInterstitial interstitialAdColony;
        private AdColonyInterstitialListener interstitialListener;
        private AdColonyAdOptions interstitialAdOptions;
        private static boolean isInterstitialLoaded;

        private int retryAttempt;
        private int counter = 1;

        private String adStatus = "";
        private String adNetwork = "";
        private String backupAdNetwork = "";
        private String adMobInterstitialId = "";
        private String appLovinInterstitialId = "";
        private String mopubInterstitialId = "";
        public static String adColonyInterstitialId = "";
        private int placementStatus = 1;
        private int interval = 3;

        private boolean legacyGDPR = false;

        public Builder(Activity activity) {
            this.activity = activity;
        }

        public Builder build() {
            loadInterstitialAd();
            return this;
        }

        public void show() {
            showInterstitialAd();
        }

        public Builder setAdStatus(String adStatus) {
            this.adStatus = adStatus;
            return this;
        }

        public Builder setAdNetwork(String adNetwork) {
            this.adNetwork = adNetwork;
            return this;
        }

        public Builder setBackupAdNetwork(String backupAdNetwork) {
            this.backupAdNetwork = backupAdNetwork;
            return this;
        }

        public Builder setAdMobInterstitialId(String adMobInterstitialId) {
            this.adMobInterstitialId = adMobInterstitialId;
            return this;
        }


        public Builder setAppLovinInterstitialId(String appLovinInterstitialId) {
            this.appLovinInterstitialId = appLovinInterstitialId;
            return this;
        }

        public Builder setMopubInterstitialId(String mopubInterstitialId) {
            this.mopubInterstitialId = mopubInterstitialId;
            return this;
        }

        public InterstitialAd.Builder setAdColonyInterstitialId(String adColony_InterstitialId) {
            adColonyInterstitialId = adColony_InterstitialId;
            return this;
        }

        public Builder setPlacementStatus(int placementStatus) {
            this.placementStatus = placementStatus;
            return this;
        }

        public Builder setInterval(int interval) {
            this.interval = interval;
            return this;
        }

        public Builder setLegacyGDPR(boolean legacyGDPR) {
            this.legacyGDPR = legacyGDPR;
            return this;
        }

        public void loadInterstitialAd() {
            if (adStatus.equals(AD_STATUS_ON) && placementStatus != 0) {
                switch (adNetwork) {
                    case ADMOB:
                        com.google.android.gms.ads.interstitial.InterstitialAd.load(activity, adMobInterstitialId, Tools.getAdRequest(activity, legacyGDPR), new InterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull com.google.android.gms.ads.interstitial.InterstitialAd interstitialAd) {
                                adMobInterstitialAd = interstitialAd;
                                adMobInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        loadInterstitialAd();
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                                        Log.d(TAG, "The ad failed to show.");
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        adMobInterstitialAd = null;
                                        Log.d(TAG, "The ad was shown.");
                                    }
                                });
                                Log.i(TAG, "onAdLoaded");
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                Log.i(TAG, loadAdError.getMessage());
                                adMobInterstitialAd = null;
                                loadBackupInterstitialAd();
                                Log.d(TAG, "Failed load AdMob Interstitial Ad");
                            }
                        });
                        break;





                    case APPLOVIN:
                        maxInterstitialAd = new MaxInterstitialAd(appLovinInterstitialId, activity);
                        maxInterstitialAd.setListener(new MaxAdListener() {
                            @Override
                            public void onAdLoaded(MaxAd ad) {
                                retryAttempt = 0;
                                Log.d(TAG, "AppLovin Interstitial Ad loaded...");
                            }

                            @Override
                            public void onAdDisplayed(MaxAd ad) {
                            }

                            @Override
                            public void onAdHidden(MaxAd ad) {
                                maxInterstitialAd.loadAd();
                            }

                            @Override
                            public void onAdClicked(MaxAd ad) {

                            }

                            @Override
                            public void onAdLoadFailed(String adUnitId, MaxError error) {
                                retryAttempt++;
                                long delayMillis = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retryAttempt)));
                                new Handler().postDelayed(() -> maxInterstitialAd.loadAd(), delayMillis);
                                loadBackupInterstitialAd();
                                Log.d(TAG, "failed to load AppLovin Interstitial");
                            }

                            @Override
                            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                                maxInterstitialAd.loadAd();
                            }
                        });

                        // Load the first ad
                        maxInterstitialAd.loadAd();
                        break;

                    case MOPUB:
                        mInterstitial = new MoPubInterstitial(activity, mopubInterstitialId);
                        mInterstitial.setInterstitialAdListener(new MoPubInterstitial.InterstitialAdListener() {
                            @Override
                            public void onInterstitialLoaded(MoPubInterstitial moPubInterstitial) {
                                Log.d(TAG, "Mopub Interstitial Ad is ready");
                            }

                            @Override
                            public void onInterstitialFailed(MoPubInterstitial moPubInterstitial, MoPubErrorCode moPubErrorCode) {
                                Log.d(TAG, "failed to load Mopub Interstitial Ad");
                                loadBackupInterstitialAd();
                            }

                            @Override
                            public void onInterstitialShown(MoPubInterstitial moPubInterstitial) {

                            }

                            @Override
                            public void onInterstitialClicked(MoPubInterstitial moPubInterstitial) {

                            }

                            @Override
                            public void onInterstitialDismissed(MoPubInterstitial moPubInterstitial) {
                                mInterstitial.load();
                            }
                        });
                        mInterstitial.load();
                        break;

                    case ADCOLONY:
                        interstitialListener = new AdColonyInterstitialListener() {
                            @Override
                            public void onRequestFilled(AdColonyInterstitial adIn) {
                                interstitialAdColony = adIn;
                                isInterstitialLoaded = true;
                                loadBackupInterstitialAd();
                                Log.d(TAG, "onRequestFilled");
                            }

                            @Override
                            public void onRequestNotFilled(AdColonyZone zone) {
                                super.onRequestNotFilled(zone);
                                Log.d(TAG, "onRequestNotFilled");
                            }

                            @Override
                            public void onOpened(AdColonyInterstitial ad) {
                                super.onOpened(ad);
                                Log.d(TAG, "onOpened");
                            }

                            @Override
                            public void onClosed(AdColonyInterstitial ad) {
                                super.onClosed(ad);
                                interstitialAdColony = ad;
                                isInterstitialLoaded = true;
                                AdColony.requestInterstitial(adColonyInterstitialId, interstitialListener, interstitialAdOptions);
                            }

                            @Override
                            public void onClicked(AdColonyInterstitial ad) {
                                super.onClicked(ad);
                            }

                            @Override
                            public void onLeftApplication(AdColonyInterstitial ad) {
                                super.onLeftApplication(ad);
                            }

                            @Override
                            public void onExpiring(AdColonyInterstitial ad) {
                                super.onExpiring(ad);
                                AdColony.requestInterstitial(adColonyInterstitialId, interstitialListener, interstitialAdOptions);
                                Log.d(TAG, "onExpiring");
                            }
                        };
                        interstitialAdOptions = new AdColonyAdOptions();
                        AdColony.requestInterstitial(adColonyInterstitialId, interstitialListener, interstitialAdOptions);
                        break;
                }
            }
        }

        public void loadBackupInterstitialAd() {
            if (adStatus.equals(AD_STATUS_ON) && placementStatus != 0) {
                switch (backupAdNetwork) {
                    case ADMOB:
                        com.google.android.gms.ads.interstitial.InterstitialAd.load(activity, adMobInterstitialId, Tools.getAdRequest(activity, legacyGDPR), new InterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull com.google.android.gms.ads.interstitial.InterstitialAd interstitialAd) {
                                adMobInterstitialAd = interstitialAd;
                                adMobInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        loadInterstitialAd();
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                                        Log.d(TAG, "The ad failed to show.");
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        adMobInterstitialAd = null;
                                        Log.d(TAG, "The ad was shown.");
                                    }
                                });
                                Log.i(TAG, "onAdLoaded");
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                Log.i(TAG, loadAdError.getMessage());
                                adMobInterstitialAd = null;
                                Log.d(TAG, "Failed load AdMob Interstitial Ad");
                            }
                        });
                        break;




                    case APPLOVIN:
                        maxInterstitialAd = new MaxInterstitialAd(appLovinInterstitialId, activity);
                        maxInterstitialAd.setListener(new MaxAdListener() {
                            @Override
                            public void onAdLoaded(MaxAd ad) {
                                retryAttempt = 0;
                                Log.d(TAG, "AppLovin Interstitial Ad loaded...");
                            }

                            @Override
                            public void onAdDisplayed(MaxAd ad) {
                            }

                            @Override
                            public void onAdHidden(MaxAd ad) {
                                maxInterstitialAd.loadAd();
                            }

                            @Override
                            public void onAdClicked(MaxAd ad) {

                            }

                            @Override
                            public void onAdLoadFailed(String adUnitId, MaxError error) {
                                retryAttempt++;
                                long delayMillis = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retryAttempt)));
                                new Handler().postDelayed(() -> maxInterstitialAd.loadAd(), delayMillis);
                                Log.d(TAG, "failed to load AppLovin Interstitial");
                            }

                            @Override
                            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                                maxInterstitialAd.loadAd();
                            }
                        });

                        // Load the first ad
                        maxInterstitialAd.loadAd();
                        break;

                    case MOPUB:
                        mInterstitial = new MoPubInterstitial(activity, mopubInterstitialId);
                        mInterstitial.setInterstitialAdListener(new MoPubInterstitial.InterstitialAdListener() {
                            @Override
                            public void onInterstitialLoaded(MoPubInterstitial moPubInterstitial) {
                                Log.d(TAG, "Mopub Interstitial Ad is ready");
                            }

                            @Override
                            public void onInterstitialFailed(MoPubInterstitial moPubInterstitial, MoPubErrorCode moPubErrorCode) {
                                Log.d(TAG, "failed to load Mopub Interstitial Ad");
                            }

                            @Override
                            public void onInterstitialShown(MoPubInterstitial moPubInterstitial) {

                            }

                            @Override
                            public void onInterstitialClicked(MoPubInterstitial moPubInterstitial) {

                            }

                            @Override
                            public void onInterstitialDismissed(MoPubInterstitial moPubInterstitial) {
                                mInterstitial.load();
                            }
                        });
                        mInterstitial.load();
                        break;

                    case ADCOLONY:
                        interstitialListener = new AdColonyInterstitialListener() {
                            @Override
                            public void onRequestFilled(AdColonyInterstitial adIn) {
                                interstitialAdColony = adIn;
                                isInterstitialLoaded = true;
                                Log.d(TAG, "onRequestFilled");
                            }

                            @Override
                            public void onRequestNotFilled(AdColonyZone zone) {
                                super.onRequestNotFilled(zone);
                                Log.d(TAG, "onRequestNotFilled");
                            }

                            @Override
                            public void onOpened(AdColonyInterstitial ad) {
                                super.onOpened(ad);
                                Log.d(TAG, "onOpened");
                            }

                            @Override
                            public void onClosed(AdColonyInterstitial ad) {
                                super.onClosed(ad);
                                interstitialAdColony = ad;
                                isInterstitialLoaded = true;
                                AdColony.requestInterstitial(adColonyInterstitialId, interstitialListener, interstitialAdOptions);
                            }

                            @Override
                            public void onClicked(AdColonyInterstitial ad) {
                                super.onClicked(ad);
                            }

                            @Override
                            public void onLeftApplication(AdColonyInterstitial ad) {
                                super.onLeftApplication(ad);
                            }

                            @Override
                            public void onExpiring(AdColonyInterstitial ad) {
                                super.onExpiring(ad);
                                AdColony.requestInterstitial(adColonyInterstitialId, interstitialListener, interstitialAdOptions);
                                Log.d(TAG, "onExpiring");
                            }
                        };
                        interstitialAdOptions = new AdColonyAdOptions();
                        AdColony.requestInterstitial(adColonyInterstitialId, interstitialListener, interstitialAdOptions);
                        break;

                    case NONE:
                        //do nothing
                        break;
                }
            }
        }

        public void showInterstitialAd() {
            if (adStatus.equals(AD_STATUS_ON) && placementStatus != 0) {
                if (counter == interval) {
                    switch (adNetwork) {
                        case ADMOB:
                            if (adMobInterstitialAd != null) {
                                adMobInterstitialAd.show(activity);
                                Log.d(TAG, "admob interstitial not null");
                            } else {
                                showBackupInterstitialAd();
                                Log.d(TAG, "admob interstitial null");
                            }
                            break;




                        case APPLOVIN:
                            if (maxInterstitialAd.isReady()) {
                                Log.d(TAG, "ready : " + counter);
                                maxInterstitialAd.showAd();
                                Log.d(TAG, "show ad");
                            } else {
                                showBackupInterstitialAd();
                            }
                            break;

                        case MOPUB:
                            if (mInterstitial.isReady()) {
                                mInterstitial.show();
                            } else {
                                showBackupInterstitialAd();
                            }
                            Log.d(TAG, "show " + adNetwork + " Interstitial Id : " + mopubInterstitialId);
                            Log.d(TAG, "counter : " + counter);
                            break;
                        case ADCOLONY:
                            if (interstitialAdColony != null && isInterstitialLoaded) {
                                interstitialAdColony.show();
                                isInterstitialLoaded = false;
                            } else {
                                showBackupInterstitialAd();
                            }
                            Log.d(TAG, "show " + adNetwork + " Interstitial Id : " + adColonyInterstitialId);
                            Log.d(TAG, "counter : " + counter);
                            break;
                    }
                    counter = 1;
                } else {
                    counter++;
                }
                Log.d(TAG, "Current counter : " + counter);
            }
        }

        public void showBackupInterstitialAd() {
            if (adStatus.equals(AD_STATUS_ON) && placementStatus != 0) {
                Log.d(TAG, "Show Backup Interstitial Ad [" + backupAdNetwork.toUpperCase() + "]");
                switch (backupAdNetwork) {
                    case ADMOB:
                        if (adMobInterstitialAd != null) {
                            adMobInterstitialAd.show(activity);
                        }
                        break;



                    case APPLOVIN:
                        if (maxInterstitialAd.isReady()) {
                            maxInterstitialAd.showAd();
                            counter = 1;
                        }
                        break;

                    case MOPUB:
                        if (mInterstitial.isReady()) {
                            mInterstitial.show();
                        }
                        break;
                    case ADCOLONY:
                        if (interstitialAdColony != null && isInterstitialLoaded) {
                            interstitialAdColony.show();
                            isInterstitialLoaded = false;
                        }
                        break;

                    case NONE:
                        //do nothing
                        break;
                }
            }
        }

    }

}