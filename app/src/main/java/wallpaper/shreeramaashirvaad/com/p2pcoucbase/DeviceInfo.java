package wallpaper.shreeramaashirvaad.com.p2pcoucbase;

public class DeviceInfo {

    String deviceip;
    String mobile;
    String username;
    String status;

    public DeviceInfo(String deviceip, String mobile, String username,String status) {
        this.deviceip = deviceip;
        this.mobile = mobile;
        this.username = username;
        this.status=status;
    }

    public String getDeviceip() {
        return deviceip;
    }

    public void setDeviceip(String deviceip) {
        this.deviceip = deviceip;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }



    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}