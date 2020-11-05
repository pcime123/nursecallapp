package com.sscctv.nursecallapp.pbx;

public class EndPoint {
    public static final String PROTOCOL = "http://";

    public static final String LOGIN = "/api/v1.1.0/login";
    public static final String HEARTBEAT = "/api/v1.1.0/heartbeat";
    public static final String LOGOUT = "/api/v1.1.0/logout";

    public static final String QUERY_PBX_INFO = "/api/v1.1.0/deviceinfo/query";                 // Query PBX information

    public static final String QUERY_EXTENSION_LIST = "/api/v1.1.0/extensionlist/query";    // Get basic information of the PBX extension list
    public static final String QUERY_EXTENSION_SETTINGS = "/api/v1.1.0/extension/query";    // Get detailed information about an individual extension or multiple extensions.
    public static final String MODIFY_EXTENSION = "/api/v1.1.0/extension/update";                  // Configure the settings of an individual extension

    public static final String QUERY_PAGING_GROUP_LIST = "/api/v1.1.0/paginggrouplist/query";
    public static final String QUERY_PAGING_GROUP_SETTINGS = "/api/v1.1.0/paginggroup/query";
    public static final String ADD_PAGING_GROUP = "/api/v1.1.0/paginggroup/add";
    public static final String EDIT_PAGING_GROUP = "/api/v1.1.0/paginggroup/update";
    public static final String DELETE_PAGING_GROUP = "/api/v1.1.0/paginggroup/delete";
    public static final String MAKE_ANNOUNCEMENT = "/api/v1.1.0/extension/dial_number";
    public static final String START_PAGING_GROUP_MUSIC = "/api/v1.1.0/extension/dial_number";
    public static final String STOP_PAGING_GROUP_MUSIC = "/api/v1.1.0/paginggroup/hangup_music";
}
