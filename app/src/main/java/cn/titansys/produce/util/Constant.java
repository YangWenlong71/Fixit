package cn.titansys.produce.util;

/**
 * 常量
 */
public class Constant {
    // request参数
    public static final int REQ_QR_CODE = 11002; // // 打开扫描界面请求码
    public static final int REQ_PERM_CAMERA = 11003; // 打开摄像头

    public static final String RootPath = "http://api.hexsy.titansys.cn/";

    //请求登录
    public static final String Prelogin = RootPath+"junction/hotel/prelogin";

    //注册设备
    public static final String DeviceReg = RootPath+"junction/device/register";

    //测试帐号
    public static final String hotelId = "00860000XX";
    public static final String registerCode = "";

    //正式服测试帐号
//    public static final String hotelId = "0086000071";
//    public static final String registerCode = "192909";
}
