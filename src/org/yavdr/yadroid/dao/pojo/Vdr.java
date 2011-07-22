package org.yavdr.yadroid.dao.pojo;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


@DatabaseTable(tableName = "vdr")
public class Vdr {
    @DatabaseField(id = true)
    private String name;
    
    @DatabaseField
    private int restfulPort;

    @DatabaseField
	private String address;

	public Vdr() {
    
    }
    
	public Vdr(String hostName, String hostAddress, int port) {
		name = hostName;
		address = hostAddress;
		restfulPort = port;
	}


    public String getAddress() {
		return address;
	}

	public String getName() {
		return name;
	}

	public int getRestfulPort() {
		return restfulPort;
	}
}
