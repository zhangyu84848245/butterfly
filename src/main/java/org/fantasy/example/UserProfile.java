package org.fantasy.example;

import java.io.Serializable;
import java.util.Date;

public class UserProfile implements Serializable {
	private static final long serialVersionUID = 1L;
	private short age;
	private String phone;
	private byte sex;
	private Date birthday;
	private String address;
	public UserProfile() {
		
	}
	public short getAge() {
		return age;
	}
	public void setAge(short age) {
		this.age = age;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public byte getSex() {
		return sex;
	}
	public void setSex(byte sex) {
		this.sex = sex;
	}
	public Date getBirthday() {
		return birthday;
	}
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
}
