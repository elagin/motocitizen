package motocitizen.network.requests;

public enum Methods {
    AUTH("auth"),
    GET_LIST("list"),
    BAN("ban"),
    ONWAY("onway"),
    CANCEL_ONWAY("cancelOnWay"),
    INPLACE("inplace"),
    LEAVE("leave"),
    MESSAGE("message"),
    CREATE("createAcc"),
    REGISTER_GCM("registerGCM"),
    REGIONS("regions"),
    CHANGE_STATE("changeState");

    private final String code;

    Methods(String code) {
        this.code = code;
    }

    public String toCode() {
        return this.code;
    }
}
