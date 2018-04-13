package me.gavin.app;

/**
 * 这里是萌萌哒注释君
 *
 * @author gavin.xiong 2018/4/13
 */
public class Temp {

    private String phone;
    private boolean valid;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public String toString() {
        return "Temp{" +
                "phone='" + phone + '\'' +
                ", valid=" + valid +
                '}';
    }
}
