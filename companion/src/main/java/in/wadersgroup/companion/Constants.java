package in.wadersgroup.companion;

/**
 * Constant values reused in this Project.
 */

/**
 * @author Romil
 */
public final class Constants {
    public static final int SUCCESS_RESULT = 0;

    public static final int FAILURE_RESULT = 1;

    public static final String PACKAGE_NAME = "in.wadersgroup.companionAndroid";

    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";

    public static final String RESULT_DATA_KEY = PACKAGE_NAME
            + ".RESULT_DATA_KEY";

    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME
            + ".LOCATION_DATA_EXTRA";

    static String staticURLBitches = "http://wadersgroup.in/companion_apis/";
    //static final String staticURLBitches = "http://172.16.2.174/companion/";

    // REST API for Registration
    static final String REG_SERVER_URL = staticURLBitches + "sign_up.php?email=";

    // REST API for getting Directions
    static final String DIRECTIONS_PROXY_URL = staticURLBitches + "coordinates.php?email=";

    // REST API for Login
    static final String LOGIN_SERVER_URL = staticURLBitches + "user_login.php?email=";

    // REST API for GCM ID Update
    static final String GCM_UPDATE_URL = staticURLBitches + "device_update.php?email=";

    // REST API to get all the Markers for Map
    static final String ALL_MARKERS_URL = staticURLBitches + "get_points.php";

    // REST API to get Associated Roads
    static final String ASSOC_MARKERS_URL = staticURLBitches + "get_assoc.php?end_point=";

    // REST API for Anonymous Location Updates
    static final String ANONYMOUS_LOCATION_UPDATE_URL = staticURLBitches + "active_updates.php?email=";

    // REST API for Road Update
    static final String ROAD_UPDATE_URL = staticURLBitches + "road_updates.php?email=";

    // REST API to fetch Blocked Roads
    static final String ROADBLOCK_URL = staticURLBitches + "blocked_roads.php?email=";

    // REST API to request for Roads in a region
    static final String ROAD_REQUEST_URL = staticURLBitches + "road_options.php?email=";

    // REST API for getting Safest Road
    static final String SAFE_ROAD_URL = staticURLBitches + "check_path.php?email=";

    // REST API for Confirming/Cancelling a Roadblock
    static final String CONFIRMATION_URL = staticURLBitches + "block_confirmation.php?email=";

    // REST API for Passive Location Updates
    static final String PASSIVE_UPDATES_URL = staticURLBitches + "passive_updates.php?email=";

    // Places API
    static final String PLACES_URL = staticURLBitches + "places.php?email=";

    // Go Live API
    static final String GO_LIVE_URL = staticURLBitches + "go_live.php?email=";

    // Periodic Notification API
    static final String USER_BLOCKS_URL = staticURLBitches + "user_blocks.php?email=";

    // Rating Update API
    static final String RATING_URL = staticURLBitches + "public_ratings.php?email=";

    // Promotion API
    static final String PROMO_URL = staticURLBitches + "ref_code.php?email=";

    // USER POINTS API
    static final String USER_ACCOUNT_URL = staticURLBitches + "user_account.php?email=";

    // Mobile Number Verification API
    static final String MOBILE_URL = staticURLBitches + "mobile_verify.php?email=";

    // Recharge Data API
    static final String RECHARGE_DATA_URL = staticURLBitches + "recharge_data.php?email=";

    // Recharge Data API
    static final String RECHARGE_URL = staticURLBitches + "recharge.php?email=";

    // Recharge Data API
    static final String RECHARGE_STATUS_URL = staticURLBitches + "recharge_status.php?email=";

    // Resend OTP API
    static final String RESEND_OTP_API = staticURLBitches + "resend_otp.php?email=";

    // Get Road Point API for Data Upload
    static final String ROAD_POINT_API = staticURLBitches + "road_point.php?email=";

    // Send all User Contacts
    static final String CONTACTS_API = staticURLBitches + "save_contacts.php";

    // Google Project Number
    //static final String GOOGLE_PROJ_ID = "554630400910";
    static final String GOOGLE_PROJ_ID = "574172570168";
    // Message Key
    static final String MSG_KEY = "m";
}
