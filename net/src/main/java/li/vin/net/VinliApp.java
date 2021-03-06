package li.vin.net;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import rx.Observable;

public final class VinliApp implements Diagnostics {
  private final Devices mDevices;
  private final Diagnostics mDiagnostics;
  private final Rules mRules;
  private final Events mEvents;
  private final Locations mLocations;
  private final Snapshots mSnapshots;
  private final Vehicles mVehicles;
  private final Subscriptions mSubscriptions;
  private final Users mUsers;

  private final Gson mGson;
  private final LinkLoader mLinkLoader;

  /*protected*/ VinliApp(@NonNull String accessToken) {
    final GsonBuilder gsonB = new GsonBuilder();

    final Client client = new OkClient();
    final RestAdapter.Log logger = new AndroidLog("VinliNet");

    Device.registerGson(gsonB);
    Rule.registerGson(gsonB);
    Event.registerGson(gsonB);
    Subscription.registerGson(gsonB);
    Vehicle.registerGson(gsonB);
    Message.registerGson(gsonB);
    Page.registerGson(gsonB);
    TimeSeries.registerGson(gsonB);
    ObjectRef.registerGson(gsonB);
    Location.registerGson(gsonB);
    Coordinate.registerGson(gsonB);
    Snapshot.registerGson(gsonB);
    Notification.registerGson(gsonB);
    User.registerGson(gsonB);

    mGson = gsonB.create();

    final GsonConverter gson = new GsonConverter(mGson);

    mLinkLoader = new LinkLoader(client, accessToken, gson);

    final RestAdapter.LogLevel logLevel = RestAdapter.LogLevel.FULL;

    final RequestInterceptor oauthInterceptor = new OauthInterceptor(accessToken);

    final RestAdapter platformAdapter = new RestAdapter.Builder()
        .setEndpoint(Endpoint.PLATFORM)
        .setLog(logger)
        .setLogLevel(logLevel)
        .setClient(client)
        .setConverter(gson)
        .setRequestInterceptor(oauthInterceptor)
        .build();

    mDevices = platformAdapter.create(Devices.class);
    mVehicles = platformAdapter.create(Vehicles.class);

    mDiagnostics = new RestAdapter.Builder()
        .setEndpoint(Endpoint.DIAGNOSTICS)
        .setLog(logger)
        .setLogLevel(logLevel)
        .setClient(client)
        .setConverter(gson)
        .setRequestInterceptor(oauthInterceptor)
        .build()
        .create(Diagnostics.class);

    mRules = new RestAdapter.Builder()
        .setEndpoint(Endpoint.RULES)
        .setLog(logger)
        .setLogLevel(logLevel)
        .setClient(client)
        .setConverter(gson)
        .setRequestInterceptor(oauthInterceptor)
        .build()
        .create(Rules.class);

    final RestAdapter eventsAdapter = new RestAdapter.Builder()
        .setEndpoint(Endpoint.EVENTS)
        .setLog(logger)
        .setLogLevel(logLevel)
        .setClient(client)
        .setConverter(gson)
        .setRequestInterceptor(oauthInterceptor)
        .build();

    mEvents = eventsAdapter.create(Events.class);
    mSubscriptions = eventsAdapter.create(Subscriptions.class);

    final RestAdapter telemAdapter = new RestAdapter.Builder()
        .setEndpoint(Endpoint.TELEMETRY)
        .setLog(logger)
        .setLogLevel(logLevel)
        .setClient(client)
        .setConverter(gson)
        .setRequestInterceptor(oauthInterceptor)
        .build();

    mLocations = telemAdapter.create(Locations.class);
    mSnapshots = telemAdapter.create(Snapshots.class);

    mUsers = new RestAdapter.Builder()
        .setEndpoint(Endpoint.AUTH)
        .setLog(logger)
        .setLogLevel(logLevel)
        .setClient(client)
        .setConverter(gson)
        .setRequestInterceptor(oauthInterceptor)
        .build()
        .create(Users.class);
  }

  public Observable<Page<Device>> devices() {
    return mDevices.devices(null, null);
  }

  /**
   * Pass null for default
   */
  public Observable<Page<Device>> devices(
      @Nullable Integer limit,
      @Nullable Integer offset) {
    return mDevices.devices(limit, offset);
  }

  public Observable<Device> device(@NonNull String deviceId) {
    return mDevices.device(deviceId).map(Wrapped.<Device>pluckItem());
  }

  @Override public Observable<Dtc> diagnoseDtcCode(String dtcCode) {
    return mDiagnostics.diagnoseDtcCode(dtcCode);
  }

  public Observable<User> currentUser() {
    return mUsers.currentUser().map(Wrapped.<User>pluckItem());
  }

  /*package*/ Vehicles vehicles() {
    return mVehicles;
  }

  /*package*/ Rules rules() {
    return mRules;
  }

  /*package*/ Events events() {
    return mEvents;
  }

  /*package*/ Locations locations() {
    return mLocations;
  }

  /*package*/ Snapshots snapshots() {
    return mSnapshots;
  }

  /*package*/ Subscriptions subscriptions() {
    return mSubscriptions;
  }

  /*package*/ LinkLoader linkLoader() {
    return mLinkLoader;
  }

  /*package*/ Gson gson() {
    return mGson;
  }

  private static final class OauthInterceptor implements RequestInterceptor {
    private static final String AUTH = "Authorization";
    private final String mBearer;

    public OauthInterceptor(String accessToken) {
      mBearer = "Bearer " + accessToken;
    }

    @Override public void intercept(RequestFacade request) {
      request.addHeader(AUTH, mBearer);
    }
  }
}
