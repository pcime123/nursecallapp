package com.sscctv.nursecallapp.pbx;

public class OpenApi {
    public class LoginApi {
        public String username;
        public String password;
        public String port;
        public String version;
        public String url;
        public String urltag;
    }

    public class DeviceInfo {
        public String devicename;
        public String sn;
        public String hardwarever;
        public String firmwarever;
        public String systemtime;
        public String uptime;
        public String extensionstatus;
    }

    public class ExtList {
        public String extnumber;
        public String status;
        public String type;
        public String username;
        public String agentid;
    }

    public class ExtOKList{
        public String extnumber;
        public String model;
        public String ward;
        public String room;
        public String bed;
    }

    public class ExtInfos {
        public String extnumber;
        public String username;
        public String status;
        public String type;
        public String port;
        public String callerid;
        public String registername;
        public String registerpassword;
        public String maxregistrations;
        public String loginpassword;
        public String email;
        public String moblie;
        public String language;
        public String hasvoicemail;
        public String vmsecret;
        public String enablevmtoemail;

        public String alwaysforward;
        public String atransferto;
        public String atransferext;
        public String atransferprefix;
        public String atransfernum;

        public String noanswerforward;
        public String ntransferto;
        public String ntransferext;
        public String ntransferprefix;
        public String ntransfernum;

        public String busyforward;
        public String btransferto;
        public String btransferext;
        public String btransferprefix;
        public String btransfernum;

        public String enablemobile;
        public String ringsimultaneous;
        public String mobileprefix;
        public String allowbeingmonitored;
        public String monitormode;
        public String ringtimeout;
        public String maxduration;
        public String dnd;
        public String callrestriction;
        public String agentid;
    }

    public class PagingGroupList {
        public String id;
        public String number;
        public String name;
    }

    public class PagingGroup {
        public String id;
        public String number;
        public String name;
        public String duplex;
        public String allowexten;
        public String allowextengroup;
        public String enablekeyhanup;
        public String multicastip;
    }

    public class PagingGroupAdd {
        public String number;
        public String name;
        public String duplex;
        public String allowexten;
        public String allowextengroup;
        public String enablekeyhanup;
        public String multicastip;
    }
}
